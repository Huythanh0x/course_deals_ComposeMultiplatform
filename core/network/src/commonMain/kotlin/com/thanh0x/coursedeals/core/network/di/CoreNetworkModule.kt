package com.thanh0x.coursedeals.core.network.di

import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.core.common.Constant
import com.thanh0x.coursedeals.core.common.NetworkStatusCode
import com.thanh0x.coursedeals.domain.user.source.LocalAuthenticationDataSource
import com.thanh0x.coursedeals.domain.user.source.RemoteAuthenticationDataSource
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
private val PUBLIC_AUTH_PATHS = listOf(
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
    return try {
        val refreshToken = localAuthenticationDataSource.getLocalRefreshToken()
        if (refreshToken.isNullOrBlank()) {
            clearTokensSafely(localAuthenticationDataSource)
            null
        } else {
            when (val result = remoteAuthenticationDataSource.refreshToken(refreshToken)) {
                is AppResult.Success -> {
                    val newAccessToken = result.data.accessToken
                    localAuthenticationDataSource.saveLocalToken(newAccessToken)
                    result.data.refreshToken?.takeIf { it.isNotBlank() }?.let { newRefreshToken ->
                        localAuthenticationDataSource.saveLocalRefreshToken(newRefreshToken)
                    }
                    newAccessToken
                }
                else -> {
                    clearTokensSafely(localAuthenticationDataSource)
                    null
                }
            }
        }
    } catch (@Suppress("TooGenericExceptionCaught") exception: Exception) {
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
                    return@intercept execute(request)
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

val coreNetworkModule = module {
    single<HttpClient> {
        provideMultiplatformHttpClient(
            localAuthenticationDataSource = get(),
            remoteAuthenticationDataSourceProvider = { get() }
        )
    }
}
