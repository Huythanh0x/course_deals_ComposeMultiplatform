package com.thanh0x.coursedeals.di

import com.thanh0x.coursedeals.data.source.datastore.LocalAuthenticationDataSourceImpl
import com.thanh0x.coursedeals.data.source.remote.CouponService
import com.thanh0x.coursedeals.data.source.remote.RemoteAuthenticationDataSourceImpl
import com.thanh0x.coursedeals.data.source.remote.RemoteCouponDataSourceImpl
import com.thanh0x.coursedeals.data.source.remote.RemoteUserProfileDataSourceImpl
import com.thanh0x.coursedeals.data.source.remote.UserAuthenticationService
import com.thanh0x.coursedeals.data.source.remote.UserProfileService
import com.thanh0x.coursedeals.core.common.Constant
import com.thanh0x.coursedeals.core.common.NetworkStatusCode
import com.thanh0x.coursedeals.BuildConfig
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
object NetworkModule {
    @Singleton
    @Provides
    fun provideAuthInterceptor(
        localAuthenticationDataSourceImpl: LocalAuthenticationDataSourceImpl
    ): Interceptor {
        return Interceptor { chain ->
            return@Interceptor runBlocking {
                val token = localAuthenticationDataSourceImpl.getLocalToken()
                val request = chain.request().newBuilder()
                    .addHeader(
                        Constant.NETWORK_AUTHORIZATION_HEADER,
                        "${Constant.NETWORK_BEARER_PREFIX} $token"
                    )
                    .build()
                val response = chain.proceed(request)
                if (response.code == NetworkStatusCode.HTTP_CODE_UNAUTHORIZED) {
                    try {
                        localAuthenticationDataSourceImpl.saveLocalToken(token!!)
                        val retryRequest = request.newBuilder()
                            .addHeader(
                                Constant.NETWORK_AUTHORIZATION_HEADER,
                                "${Constant.NETWORK_BEARER_PREFIX} $token"
                            )
                            .build()
                        chain.proceed(retryRequest)
                    } catch (@Suppress("TooGenericExceptionCaught") exception: Exception) {
                        Timber.e(exception, "AuthInterceptor: Failed to refresh token")
                        localAuthenticationDataSourceImpl.saveLocalToken("")
                        response
                    }
                } else {
                    response
                }
            }
        }
    }

    @Singleton
    @Provides
    fun provideRemoteCouponDataSource(
        couponService: CouponService
    ) = RemoteCouponDataSourceImpl(couponService)

    @Singleton
    @Provides
    fun provideRemoteUserProfileDataSource(
        userProfileService: UserProfileService
    ) = RemoteUserProfileDataSourceImpl(userProfileService)

    @Singleton
    @Provides
    fun provideRemoteAuthenticationDataSource(
        userAuthenticationService: UserAuthenticationService
    ) = RemoteAuthenticationDataSourceImpl(userAuthenticationService)

    @Singleton
    @Provides
    fun provideAuthenticationService(retrofit: Retrofit): UserAuthenticationService =
        retrofit.create(UserAuthenticationService::class.java)

    @Singleton
    @Provides
    fun provideUserProfileService(retrofit: Retrofit): UserProfileService =
        retrofit.create(UserProfileService::class.java)

    @Singleton
    @Provides
    fun provideCouponService(retrofit: Retrofit): CouponService =
        retrofit.create(CouponService::class.java)

    @Singleton
    @Provides
    fun provideRetrofitInstance(
        okHttpClient: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory
    ): Retrofit {
        return Retrofit.Builder().baseUrl(BuildConfig.BASE_URL_API).client(okHttpClient)
            .addConverterFactory(gsonConverterFactory).build()
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
