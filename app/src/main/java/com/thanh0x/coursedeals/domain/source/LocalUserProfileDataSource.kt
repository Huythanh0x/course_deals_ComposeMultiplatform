package com.thanh0x.coursedeals.domain.source

interface LocalUserProfileDataSource {
    suspend fun getEnableDarkMode(): Boolean?
    suspend fun saveEnableDarkMode(isDarkModeEnable: Boolean)

    suspend fun getEnableFingerPrint(): Boolean?
    suspend fun saveEnableFingerPrint(isFingerPrintEnable: Boolean)

    suspend fun getFavoriteCategories(): Set<String>
    suspend fun saveFavoriteCategories(categories: Set<String>)

    suspend fun getFavoriteKeywords(): Set<String>
    suspend fun saveFavoriteKeywords(keywords: Set<String>)

    suspend fun getNotificationsEnabled(): Boolean
    suspend fun saveNotificationsEnabled(enabled: Boolean)

    suspend fun getPreferencesUpdatedAt(): Long
    suspend fun savePreferencesUpdatedAt(timestamp: Long)
}
