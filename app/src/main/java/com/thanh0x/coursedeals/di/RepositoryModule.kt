package com.thanh0x.coursedeals.di

import com.thanh0x.coursedeals.data.repository.CouponRepositoryImpl
import com.thanh0x.coursedeals.data.repository.UserAuthenticationRepositoryImpl
import com.thanh0x.coursedeals.data.repository.UserProfileRepositoryImpl
import com.thanh0x.coursedeals.data.source.datastore.LocalAuthenticationDataSourceImpl
import com.thanh0x.coursedeals.data.source.datastore.LocalUserProfileDataSourceImpl
import com.thanh0x.coursedeals.data.source.local.LocalCouponDataSourceImpl
import com.thanh0x.coursedeals.data.source.remote.RemoteAuthenticationDataSourceImpl
import com.thanh0x.coursedeals.data.source.remote.RemoteCouponDataSourceImpl
import com.thanh0x.coursedeals.domain.repository.CouponRepository
import com.thanh0x.coursedeals.domain.repository.UserAuthenticationRepository
import com.thanh0x.coursedeals.domain.repository.UserProfileRepository
import com.thanh0x.coursedeals.domain.source.LocalAuthenticationDataSource
import com.thanh0x.coursedeals.domain.source.LocalCouponDataSource
import com.thanh0x.coursedeals.domain.source.LocalUserProfileDataSource
import com.thanh0x.coursedeals.domain.source.RemoteAuthenticationDataSource
import com.thanh0x.coursedeals.domain.source.RemoteCouponDataSource
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
    abstract fun bindUserAuthenticationRepository(
        impl: UserAuthenticationRepositoryImpl
    ): UserAuthenticationRepository

    @Binds
    @Singleton
    abstract fun bindCouponRepository(
        impl: CouponRepositoryImpl
    ): CouponRepository

    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(
        impl: UserProfileRepositoryImpl
    ): UserProfileRepository

    @Binds
    @Singleton
    abstract fun bindRemoteAuthenticationDataSource(
        impl: RemoteAuthenticationDataSourceImpl
    ): RemoteAuthenticationDataSource

    @Binds
    @Singleton
    abstract fun bindLocalAuthenticationDataSource(
        impl: LocalAuthenticationDataSourceImpl
    ): LocalAuthenticationDataSource

    @Binds
    @Singleton
    abstract fun bindLocalUserProfileDataSource(
        impl: LocalUserProfileDataSourceImpl
    ): LocalUserProfileDataSource

    @Binds
    @Singleton
    abstract fun bindRemoteCouponDataSource(
        impl: RemoteCouponDataSourceImpl
    ): RemoteCouponDataSource

    @Binds
    @Singleton
    abstract fun bindLocalCouponDataSource(
        impl: LocalCouponDataSourceImpl
    ): LocalCouponDataSource
}
