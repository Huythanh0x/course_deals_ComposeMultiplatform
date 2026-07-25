package com.batdaulaptrinh.freeudemycoupons.data.repository

import com.batdaulaptrinh.freeudemycoupons.data.source.datastore.LocalUserProfileDataSourceImpl
import com.batdaulaptrinh.freeudemycoupons.domain.repository.UserProfileRepository
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
