package com.thanh0x.coursedeals.domain.repository

import com.thanh0x.coursedeals.domain.model.AppResult
import com.thanh0x.coursedeals.domain.model.UserPreferences

interface UserProfileRepository {
    suspend fun getEnableDarkMode(): Boolean?
    suspend fun saveEnableDarkMode(isDarkModeEnable: Boolean)

    suspend fun getEnableFingerPrint(): Boolean?
    suspend fun saveEnableFingerPrint(isFingerPrintEnable: Boolean)

    suspend fun syncPreferences(): AppResult<UserPreferences>
    suspend fun getFavoriteCategories(): Set<String>
    suspend fun saveFavoriteCategories(categories: Set<String>): AppResult<UserPreferences>

    suspend fun getFavoriteKeywords(): Set<String>
    suspend fun saveFavoriteKeywords(keywords: Set<String>): AppResult<UserPreferences>

    suspend fun getNotificationsEnabled(): Boolean
    suspend fun saveNotificationsEnabled(enabled: Boolean): AppResult<UserPreferences>
}
