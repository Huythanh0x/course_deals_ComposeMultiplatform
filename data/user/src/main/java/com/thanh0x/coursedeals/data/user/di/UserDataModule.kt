package com.thanh0x.coursedeals.data.user.di

import com.thanh0x.coursedeals.data.user.repository.UserAuthenticationRepositoryImpl
import com.thanh0x.coursedeals.data.user.repository.UserProfileRepositoryImpl
import com.thanh0x.coursedeals.data.user.source.datastore.LocalAuthenticationDataSourceImpl
import com.thanh0x.coursedeals.data.user.source.datastore.LocalSettingsDataSourceImpl
import com.thanh0x.coursedeals.data.user.source.datastore.LocalUserProfileDataSourceImpl
import com.thanh0x.coursedeals.data.user.source.remote.RemoteAuthenticationDataSourceImpl
import com.thanh0x.coursedeals.data.user.source.remote.RemoteUserProfileDataSourceImpl
import com.thanh0x.coursedeals.data.user.source.remote.UserAuthenticationService
import com.thanh0x.coursedeals.data.user.source.remote.UserProfileService
import com.thanh0x.coursedeals.domain.user.UserAuthenticationRepository
import com.thanh0x.coursedeals.domain.user.UserProfileRepository
import com.thanh0x.coursedeals.domain.user.source.LocalAuthenticationDataSource
import com.thanh0x.coursedeals.domain.user.source.LocalSettingsDataSource
import com.thanh0x.coursedeals.domain.user.source.LocalUserProfileDataSource
import com.thanh0x.coursedeals.domain.user.source.RemoteAuthenticationDataSource
import com.thanh0x.coursedeals.domain.user.source.RemoteUserProfileDataSource
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UserDataModule {

    @Binds
    @Singleton
    abstract fun bindUserAuthenticationRepository(
        impl: UserAuthenticationRepositoryImpl
    ): UserAuthenticationRepository

    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(
        impl: UserProfileRepositoryImpl
    ): UserProfileRepository

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
    abstract fun bindLocalSettingsDataSource(
        impl: LocalSettingsDataSourceImpl
    ): LocalSettingsDataSource

    @Binds
    @Singleton
    abstract fun bindRemoteAuthenticationDataSource(
        impl: RemoteAuthenticationDataSourceImpl
    ): RemoteAuthenticationDataSource

    @Binds
    @Singleton
    abstract fun bindRemoteUserProfileDataSource(
        impl: RemoteUserProfileDataSourceImpl
    ): RemoteUserProfileDataSource

    companion object {
        @Provides
        @Singleton
        fun provideUserAuthenticationService(retrofit: Retrofit): UserAuthenticationService =
            retrofit.create(UserAuthenticationService::class.java)

        @Provides
        @Singleton
        fun provideUserProfileService(retrofit: Retrofit): UserProfileService =
            retrofit.create(UserProfileService::class.java)
    }
}
