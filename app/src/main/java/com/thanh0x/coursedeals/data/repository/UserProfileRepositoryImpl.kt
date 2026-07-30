package com.thanh0x.coursedeals.data.repository

import com.thanh0x.coursedeals.domain.repository.UserProfileRepository
import com.thanh0x.coursedeals.domain.source.LocalUserProfileDataSource
import javax.inject.Inject

class UserProfileRepositoryImpl @Inject constructor(
    private val localUserProfileDataSource: LocalUserProfileDataSource
) : UserProfileRepository {
    override suspend fun getEnableDarkMode() = localUserProfileDataSource.getEnableDarkMode()

    override suspend fun getEnableFingerPrint() = localUserProfileDataSource.getEnableFingerPrint()

    override suspend fun saveEnableDarkMode(isDarkModeEnable: Boolean) =
        localUserProfileDataSource.saveEnableDarkMode(isDarkModeEnable)

    override suspend fun saveEnableFingerPrint(isFingerPrintEnable: Boolean) =
        localUserProfileDataSource.saveEnableFingerPrint(isFingerPrintEnable)
}
