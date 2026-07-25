package com.thanh0x.coursedeals.data.repository

import com.thanh0x.coursedeals.data.source.datastore.LocalUserProfileDataSourceImpl
import com.thanh0x.coursedeals.domain.repository.UserProfileRepository
import javax.inject.Inject

class UserProfileRepositoryImpl @Inject constructor(private val localUserProfileDataSourceImpl: LocalUserProfileDataSourceImpl) :
    UserProfileRepository {
    override suspend fun getEnableDarkMode() = localUserProfileDataSourceImpl.getEnableDarkMode()

    override suspend fun getEnableFingerPrint() = localUserProfileDataSourceImpl.getEnableFingerPrint()

    override suspend fun saveEnableDarkMode(isDarkModeEnable: Boolean) =
        localUserProfileDataSourceImpl.saveEnableDarkMode(isDarkModeEnable)

    override suspend fun saveEnableFingerPrint(isFingerPrintEnable: Boolean) =
        localUserProfileDataSourceImpl.saveEnableFingerPrint(isFingerPrintEnable)
}
