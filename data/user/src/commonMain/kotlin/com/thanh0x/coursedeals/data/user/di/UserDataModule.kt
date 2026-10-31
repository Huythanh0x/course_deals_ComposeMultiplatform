package com.thanh0x.coursedeals.data.user.di

import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.thanh0x.coursedeals.core.common.Constant
import com.thanh0x.coursedeals.data.user.repository.UserAuthenticationRepositoryImpl
import com.thanh0x.coursedeals.data.user.repository.UserProfileRepositoryImpl
import com.thanh0x.coursedeals.data.user.source.datastore.LocalAuthenticationDataSourceImpl
import com.thanh0x.coursedeals.data.user.source.datastore.LocalSettingsDataSourceImpl
import com.thanh0x.coursedeals.data.user.source.datastore.LocalUserProfileDataSourceImpl
import com.thanh0x.coursedeals.data.user.source.remote.RemoteAuthenticationDataSourceImpl
import com.thanh0x.coursedeals.data.user.source.remote.RemoteUserProfileDataSourceImpl
import com.thanh0x.coursedeals.data.user.source.remote.UserAuthenticationService
import com.thanh0x.coursedeals.data.user.source.remote.UserAuthenticationServiceImpl
import com.thanh0x.coursedeals.data.user.source.remote.UserProfileService
import com.thanh0x.coursedeals.data.user.source.remote.UserProfileServiceImpl
import com.thanh0x.coursedeals.domain.user.UserAuthenticationRepository
import com.thanh0x.coursedeals.domain.user.UserProfileRepository
import com.thanh0x.coursedeals.domain.user.source.LocalAuthenticationDataSource
import com.thanh0x.coursedeals.domain.user.source.LocalSettingsDataSource
import com.thanh0x.coursedeals.domain.user.source.LocalUserProfileDataSource
import com.thanh0x.coursedeals.domain.user.source.RemoteAuthenticationDataSource
import com.thanh0x.coursedeals.domain.user.source.RemoteUserProfileDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import okio.Path.Companion.toPath
import org.koin.dsl.module

expect fun platformDataStoreFilePath(fileName: String): String

val userDataModule = module {
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.createWithPath(
            corruptionHandler = ReplaceFileCorruptionHandler(produceNewData = { emptyPreferences() }),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { platformDataStoreFilePath("${Constant.DATA_STORE_NAME}.preferences_pb").toPath() }
        )
    }

    single<UserAuthenticationService> { UserAuthenticationServiceImpl(httpClient = get()) }
    single<UserProfileService> { UserProfileServiceImpl(httpClient = get()) }

    single<LocalAuthenticationDataSource> { LocalAuthenticationDataSourceImpl(datastore = get()) }
    single<LocalUserProfileDataSource> { LocalUserProfileDataSourceImpl(datastore = get()) }
    single<LocalSettingsDataSource> { LocalSettingsDataSourceImpl(dataStore = get()) }

    single<RemoteAuthenticationDataSource> { RemoteAuthenticationDataSourceImpl(userAuthenticationService = get()) }
    single<RemoteUserProfileDataSource> { RemoteUserProfileDataSourceImpl(userProfileService = get()) }

    single<UserAuthenticationRepository> {
        UserAuthenticationRepositoryImpl(
            localAuthenticationDataSource = get(),
            remoteAuthenticationDataSource = get()
        )
    }

    single<UserProfileRepository> {
        UserProfileRepositoryImpl(
            localUserProfileDataSource = get(),
            remoteUserProfileDataSource = get()
        )
    }
}
