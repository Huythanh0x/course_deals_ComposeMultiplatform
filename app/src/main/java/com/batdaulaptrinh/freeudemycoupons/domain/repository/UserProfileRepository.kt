package com.batdaulaptrinh.freeudemycoupons.domain.repository

interface UserProfileRepository {
    suspend fun getEnableDarkMode(): Boolean?
    suspend fun getEnableFingerPrint(): Boolean?
    suspend fun saveEnableDarkMode(isDarkModeEnable: Boolean)
    suspend fun saveEnableFingerPrint(isFingerPrintEnable: Boolean)
}
