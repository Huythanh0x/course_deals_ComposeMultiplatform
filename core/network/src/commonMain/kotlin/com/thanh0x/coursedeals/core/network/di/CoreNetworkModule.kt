package com.thanh0x.coursedeals.core.network.di

import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.core.common.Constant
import com.thanh0x.coursedeals.core.common.NetworkStatusCode
import com.thanh0x.coursedeals.core.logging.TracingReporter
import com.thanh0x.coursedeals.domain.user.source.LocalAuthenticationDataSource
import com.thanh0x.coursedeals.domain.user.source.RemoteAuthenticationDataSource
import com.thanh0x.coursedeals.core.network.api.DeviceTokenApi
import com.thanh0x.coursedeals.core.network.api.DeviceTokenApiImpl
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.plugin
import io.ktor.client.request.header
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import org.koin.dsl.module

private const val HEADER_REFRESH_ATTEMPTED = "X-Refresh-Attempted"
private const val SERVER_ERROR_MIN_CODE = 500
private const val SERVER_ERROR_MAX_CODE = 599
private val PUBLIC_AUTH_PATHS = listOf(
    "/api/v1/app/config",
    "/api/v1/auth/login",
    "/api/v1/auth/register",
    "/api/v1/auth/verify-otp",
    "/api/v1/auth/refresh",
    "/api/v1/auth/social/login"
)
private val refreshMutex = Mutex()

expect fun createPlatformHttpClient(block: HttpClientConfig<*>.() -> Unit): HttpClient
expect fun platformBaseUrl(): String

private suspend fun tryRefreshAccessToken(
    localAuthenticationDataSource: LocalAuthenticationDataSource,
    remoteAuthenticationDataSource: RemoteAuthenticationDataSource
): String? {
    val refreshToken = runCatching {
        localAuthenticationDataSource.getLocalRefreshToken()
    }.getOrNull()
    if (refreshToken.isNullOrBlank()) {
        clearTokensSafely(localAuthenticationDataSource)
        return null
    }

    val result = runCatching {
        remoteAuthenticationDataSource.refreshToken(refreshToken)
    }.getOrElse {
        clearTokensSafely(localAuthenticationDataSource)
        return null
    }

    if (result !is AppResult.Success) {
        clearTokensSafely(localAuthenticationDataSource)
        return null
    }

    return runCatching {
        val newAccessToken = result.data.accessToken
        localAuthenticationDataSource.saveLocalToken(newAccessToken)
        val newRefreshToken = result.data.refreshToken
        if (!newRefreshToken.isNullOrBlank()) {
            localAuthenticationDataSource.saveLocalRefreshToken(newRefreshToken)
        }
        newAccessToken
    }.getOrElse {
        clearTokensSafely(localAuthenticationDataSource)
        null
    }
}

private suspend fun clearTokensSafely(
    localAuthenticationDataSource: LocalAuthenticationDataSource
) {
    try {
        localAuthenticationDataSource.clearLocalToken()
        localAuthenticationDataSource.clearLocalRefreshToken()
    } catch (@Suppress("TooGenericExceptionCaught") ignored: Exception) {
    }
}

fun provideMultiplatformHttpClient(
    localAuthenticationDataSource: LocalAuthenticationDataSource,
    remoteAuthenticationDataSourceProvider: () -> RemoteAuthenticationDataSource
): HttpClient {
    val client = createPlatformHttpClient {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    prettyPrint = false
                }
            )
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    println("KtorHttp: $message")
                }
            }
            level = LogLevel.BODY
        }
        defaultRequest {
            url(platformBaseUrl())
        }
    }

    client.plugin(HttpSend).intercept { request ->
        val encodedPath = request.url.encodedPath
        val isPublicAuthPath = PUBLIC_AUTH_PATHS.any { encodedPath.contains(it) }

        if (!isPublicAuthPath && request.headers[Constant.NETWORK_AUTHORIZATION_HEADER] == null) {
            val token = localAuthenticationDataSource.getLocalToken()
            if (!token.isNullOrBlank()) {
                request.header(
                    Constant.NETWORK_AUTHORIZATION_HEADER,
                    "${Constant.NETWORK_BEARER_PREFIX} $token"
                )
            }
        }

        val originalCall = execute(request)
        val statusCode = originalCall.response.status.value
        reportIfServerError(
            method = request.method.value,
            encodedPath = encodedPath,
            statusCode = statusCode,
        )
        if (statusCode == NetworkStatusCode.HTTP_CODE_UNAUTHORIZED && !isPublicAuthPath) {
            if (request.headers[HEADER_REFRESH_ATTEMPTED] == null) {
                val newAccessToken = refreshMutex.withLock {
                    tryRefreshAccessToken(
                        localAuthenticationDataSource = localAuthenticationDataSource,
                        remoteAuthenticationDataSource = remoteAuthenticationDataSourceProvider()
                    )
                }
                if (!newAccessToken.isNullOrBlank()) {
                    request.headers.remove(Constant.NETWORK_AUTHORIZATION_HEADER)
                    request.header(
                        Constant.NETWORK_AUTHORIZATION_HEADER,
                        "${Constant.NETWORK_BEARER_PREFIX} $newAccessToken"
                    )
                    request.header(HEADER_REFRESH_ATTEMPTED, "true")
                    val retriedCall = execute(request)
                    reportIfServerError(
                        method = request.method.value,
                        encodedPath = encodedPath,
                        statusCode = retriedCall.response.status.value,
                    )
                    return@intercept retriedCall
                }
            } else {
                clearTokensSafely(localAuthenticationDataSource)
            }
        } else if (statusCode == NetworkStatusCode.HTTP_CODE_FORBIDDEN) {
            clearTokensSafely(localAuthenticationDataSource)
        }
        originalCall
    }

    return client
}

internal fun reportIfServerError(method: String, encodedPath: String, statusCode: Int) {
    if (statusCode in SERVER_ERROR_MIN_CODE..SERVER_ERROR_MAX_CODE) {
        TracingReporter.captureHttpServerError(
            method = method,
            path = encodedPath,
            statusCode = statusCode,
        )
    }
}

val coreNetworkModule = module {
    single<HttpClient> {
        provideMultiplatformHttpClient(
            localAuthenticationDataSource = get(),
            remoteAuthenticationDataSourceProvider = { get() }
        )
    }
    single<DeviceTokenApi> { DeviceTokenApiImpl(httpClient = get()) }
}
