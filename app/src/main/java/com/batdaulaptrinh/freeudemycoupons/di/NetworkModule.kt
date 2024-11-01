package com.batdaulaptrinh.freeudemycoupons.di

import com.batdaulaptrinh.freeudemycoupons.data.source.datastore.LocalAuthenticationDataSourceImpl
import com.batdaulaptrinh.freeudemycoupons.data.source.remote.CouponService
import com.batdaulaptrinh.freeudemycoupons.data.source.remote.RemotePagingCouponDataSourceImpl
import com.batdaulaptrinh.freeudemycoupons.data.source.remote.RemoteAuthenticationDataSourceImpl
import com.batdaulaptrinh.freeudemycoupons.data.source.remote.RemoteCouponDataSourceImpl
import com.batdaulaptrinh.freeudemycoupons.data.source.remote.UserAuthenticationService
import com.batdaulaptrinh.freeudemycoupons.util.Constant
import com.batdaulaptrinh.freeudemycoupons.util.NetworkStatusCode
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
                val request =
                    chain.request().newBuilder().addHeader(Constant.NETWORK_AUTHORIZATION_HEADER, "${Constant.NETWORK_BEARER_PREFIX} $token")
                        .build()
                val response = chain.proceed(request)
                if (response.code() == NetworkStatusCode.HTTP_CODE_UNAUTHORIZED) {
                    try {
                        localAuthenticationDataSourceImpl.saveLocalToken(token!!)
                        chain.proceed(
                            request.newBuilder().addHeader(Constant.NETWORK_AUTHORIZATION_HEADER, "${Constant.NETWORK_BEARER_PREFIX} $token")
                                .build()
                        )
                    } catch (exception: Exception) {
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
    fun providePagingCouponDataSource(couponService: CouponService) =
        RemotePagingCouponDataSourceImpl(couponService)

    @Singleton
    @Provides
    fun provideRemoteCouponDataSource(couponService: CouponService) =
        RemoteCouponDataSourceImpl(couponService)

    @Singleton
    @Provides
    fun provideRemoteAuthenticationDataSource(userAuthenticationService: UserAuthenticationService) =
        RemoteAuthenticationDataSourceImpl(userAuthenticationService)

    @Singleton
    @Provides
    fun provideAuthenticationService(retrofit: Retrofit): UserAuthenticationService =
        retrofit.create(UserAuthenticationService::class.java)

    @Singleton
    @Provides
    fun provideCouponService(retrofit: Retrofit): CouponService =
        retrofit.create(CouponService::class.java)

    @Singleton
    @Provides
    fun provideRetrofitInstance(
        okHttpClient: OkHttpClient, gsonConverterFactory: GsonConverterFactory
    ): Retrofit {
        return Retrofit.Builder().baseUrl(Constant.BASE_URL_API).client(okHttpClient)
            .addConverterFactory(gsonConverterFactory).build()
    }

    @Singleton
    @Provides
    fun provideHttpClient(authInterceptor: Interceptor): OkHttpClient {
        return OkHttpClient().newBuilder()
            .readTimeout(5000, TimeUnit.SECONDS)
            .connectTimeout(5000, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .build()
    }

    @Singleton
    @Provides
    fun provideGsonConverterFactory(): GsonConverterFactory {
        return GsonConverterFactory.create()
    }

}