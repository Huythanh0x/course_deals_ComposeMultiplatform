package com.batdaulaptrinh.freeudemycoupons.di

import com.batdaulaptrinh.freeudemycoupons.data.repository.CouponRepositoryImpl
import com.batdaulaptrinh.freeudemycoupons.data.repository.UserAuthenticationRepositoryImpl
import com.batdaulaptrinh.freeudemycoupons.data.source.datastore.LocalAuthenticationDataSourceImpl
import com.batdaulaptrinh.freeudemycoupons.data.source.local.LocalCouponDataSourceImpl
import com.batdaulaptrinh.freeudemycoupons.data.source.remote.RemoteAuthenticationDataSourceImpl
import com.batdaulaptrinh.freeudemycoupons.data.source.remote.RemoteCouponDataSourceImpl
import com.batdaulaptrinh.freeudemycoupons.domain.repository.CouponRepository
import com.batdaulaptrinh.freeudemycoupons.domain.repository.UserAuthenticationRepository
import com.batdaulaptrinh.freeudemycoupons.domain.source.LocalAuthenticationDataSource
import com.batdaulaptrinh.freeudemycoupons.domain.source.LocalCouponDataSource
import com.batdaulaptrinh.freeudemycoupons.domain.source.RemoteAuthenticationDataSource
import com.batdaulaptrinh.freeudemycoupons.domain.source.RemoteCouponDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindUserAuthenticationRepository(userAuthenticationRepositoryImpl: UserAuthenticationRepositoryImpl): UserAuthenticationRepository

    @Binds
    @Singleton
    abstract fun bindCouponRepository(couponRepositoryImpl: CouponRepositoryImpl): CouponRepository

    @Binds
    @Singleton
    abstract fun bindingRemoteAuthenticationDataSource(remoteAuthenticationDataSourceImpl: RemoteAuthenticationDataSourceImpl): RemoteAuthenticationDataSource

    @Binds
    @Singleton
    abstract fun bindLocalAuthenticationDataSource(localAuthenticationDataSourceImpl: LocalAuthenticationDataSourceImpl): LocalAuthenticationDataSource

    @Binds
    @Singleton
    abstract fun bindRemoteCouponDataSourceImpl(remoteCouponDataSourceImpl: RemoteCouponDataSourceImpl): RemoteCouponDataSource

    @Binds
    @Singleton
    abstract fun bindLocalCouponDataSourceImpl(localCouponDataSourceImpl: LocalCouponDataSourceImpl): LocalCouponDataSource
}
