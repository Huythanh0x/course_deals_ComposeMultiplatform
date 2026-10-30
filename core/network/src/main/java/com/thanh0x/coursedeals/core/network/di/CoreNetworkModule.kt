package com.thanh0x.coursedeals.core.network.di

import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.core.common.Constant
import com.thanh0x.coursedeals.core.common.NetworkStatusCode
import com.thanh0x.coursedeals.core.network.BuildConfig
import com.thanh0x.coursedeals.domain.user.source.LocalAuthenticationDataSource
import com.thanh0x.coursedeals.domain.user.source.RemoteAuthenticationDataSource
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreNetworkModule {

    private const val HEADER_REFRESH_ATTEMPTED = "X-Refresh-Attempted"
    private val PUBLIC_AUTH_PATHS = listOf(
        "/api/v1/auth/login",
        "/api/v1/auth/register",
        "/api/v1/auth/verify-otp",
        "/api/v1/auth/refresh",
        "/api/v1/auth/social/login"
    )
    private val refreshLock = Any()

    @Singleton
    @Provides
    fun provideAuthInterceptor(
        localAuthenticationDataSource: LocalAuthenticationDataSource,
        remoteAuthenticationDataSource: Lazy<RemoteAuthenticationDataSource>
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
                                remoteAuthenticationDataSource = remoteAuthenticationDataSource.get()
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

    @Singleton
    @Provides
    fun provideRetrofitInstance(
        okHttpClient: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL_API)
            .client(okHttpClient)
            .addConverterFactory(gsonConverterFactory)
            .build()
    }

    @Singleton
    @Provides
    fun provideHttpClient(authInterceptor: Interceptor): OkHttpClient {
        return OkHttpClient().newBuilder()
            .readTimeout(Constant.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .connectTimeout(Constant.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .build()
    }

    @Singleton
    @Provides
    fun provideGsonConverterFactory(): GsonConverterFactory {
        return GsonConverterFactory.create()
    }
}
