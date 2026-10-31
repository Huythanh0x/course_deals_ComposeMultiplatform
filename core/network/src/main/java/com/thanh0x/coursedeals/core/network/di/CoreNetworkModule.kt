package com.thanh0x.coursedeals.core.network.di

import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.core.common.Constant
import com.thanh0x.coursedeals.core.common.NetworkStatusCode
import com.thanh0x.coursedeals.core.network.BuildConfig
import com.thanh0x.coursedeals.domain.user.source.LocalAuthenticationDataSource
import com.thanh0x.coursedeals.domain.user.source.RemoteAuthenticationDataSource
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import org.koin.dsl.module
import timber.log.Timber
import java.util.concurrent.TimeUnit

private const val HEADER_REFRESH_ATTEMPTED = "X-Refresh-Attempted"
private val PUBLIC_AUTH_PATHS = listOf(
    "/api/v1/auth/login",
    "/api/v1/auth/register",
    "/api/v1/auth/verify-otp",
    "/api/v1/auth/refresh",
    "/api/v1/auth/social/login"
)
private val refreshLock = Any()

fun provideAuthInterceptor(
    localAuthenticationDataSource: LocalAuthenticationDataSource,
    remoteAuthenticationDataSourceProvider: () -> RemoteAuthenticationDataSource
): Interceptor {
    return Interceptor { chain ->
        val originalRequest = chain.request()
        val encodedPath = originalRequest.url.encodedPath
        val isPublicAuthPath = PUBLIC_AUTH_PATHS.any { encodedPath.contains(it) }

        val requestBuilder = originalRequest.newBuilder()
        if (!isPublicAuthPath && originalRequest.header(Constant.NETWORK_AUTHORIZATION_HEADER) == null) {
            val token = runBlocking { localAuthenticationDataSource.getLocalToken() }
            if (!token.isNullOrBlank()) {
                requestBuilder.header(
                    Constant.NETWORK_AUTHORIZATION_HEADER,
                    "${Constant.NETWORK_BEARER_PREFIX} $token"
                )
            }
        }

        val response = chain.proceed(requestBuilder.build())
        if (response.code == NetworkStatusCode.HTTP_CODE_UNAUTHORIZED && !isPublicAuthPath) {
            if (originalRequest.header(HEADER_REFRESH_ATTEMPTED) == null) {
                val newAccessToken = synchronized(refreshLock) {
                    runBlocking {
                        tryRefreshAccessToken(
                            localAuthenticationDataSource = localAuthenticationDataSource,
                            remoteAuthenticationDataSource = remoteAuthenticationDataSourceProvider()
                        )
                    }
                }
                if (!newAccessToken.isNullOrBlank()) {
                    response.close()
                    val retryRequest = originalRequest.newBuilder()
                        .header(
                            Constant.NETWORK_AUTHORIZATION_HEADER,
                            "${Constant.NETWORK_BEARER_PREFIX} $newAccessToken"
                        )
                        .header(HEADER_REFRESH_ATTEMPTED, "true")
                        .build()
                    return@Interceptor chain.proceed(retryRequest)
                }
            } else {
                runBlocking {
                    clearTokensSafely(localAuthenticationDataSource)
                }
            }
        } else if (response.code == NetworkStatusCode.HTTP_CODE_FORBIDDEN) {
            runBlocking {
                clearTokensSafely(localAuthenticationDataSource)
            }
        }
        response
    }
}

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
        Timber.e(exception, "AuthInterceptor: Failed to refresh expired token")
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
    } catch (@Suppress("TooGenericExceptionCaught") exception: Exception) {
        Timber.e(exception, "AuthInterceptor: Failed to clear expired tokens")
    }
}

fun provideHttpClient(authInterceptor: Interceptor): HttpClient {
    return HttpClient(OkHttp) {
        engine {
            addInterceptor(authInterceptor)
            config {
                readTimeout(Constant.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                connectTimeout(Constant.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            }
        }
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
                    Timber.d(message)
                }
            }
            level = LogLevel.BODY
        }
        defaultRequest {
            url(BuildConfig.BASE_URL_API)
        }
    }
}

val coreNetworkModule = module {
    single<Interceptor> {
        provideAuthInterceptor(
            localAuthenticationDataSource = get(),
            remoteAuthenticationDataSourceProvider = { get() }
        )
    }
    single<HttpClient> {
        provideHttpClient(authInterceptor = get())
    }
}
