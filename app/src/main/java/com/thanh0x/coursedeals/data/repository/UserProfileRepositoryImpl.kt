package com.thanh0x.coursedeals.data.repository

import com.thanh0x.coursedeals.domain.model.AppResult
import com.thanh0x.coursedeals.domain.model.UserPreferences
import com.thanh0x.coursedeals.domain.repository.UserProfileRepository
import com.thanh0x.coursedeals.domain.source.LocalUserProfileDataSource
import com.thanh0x.coursedeals.domain.source.RemoteUserProfileDataSource
import javax.inject.Inject

class UserProfileRepositoryImpl @Inject constructor(
    private val localUserProfileDataSource: LocalUserProfileDataSource,
    private val remoteUserProfileDataSource: RemoteUserProfileDataSource,
) : UserProfileRepository {
    override suspend fun getEnableDarkMode() = localUserProfileDataSource.getEnableDarkMode()

    override suspend fun getEnableFingerPrint() = localUserProfileDataSource.getEnableFingerPrint()

    override suspend fun saveEnableDarkMode(isDarkModeEnable: Boolean) =
        localUserProfileDataSource.saveEnableDarkMode(isDarkModeEnable)

    override suspend fun saveEnableFingerPrint(isFingerPrintEnable: Boolean) =
        localUserProfileDataSource.saveEnableFingerPrint(isFingerPrintEnable)

    override suspend fun syncPreferences(): AppResult<UserPreferences> {
        val remoteResult = remoteUserProfileDataSource.getPreferences()
        if (remoteResult is AppResult.Success) {
            val prefs = remoteResult.data
            localUserProfileDataSource.saveFavoriteCategories(prefs.categories.toSet())
            localUserProfileDataSource.saveFavoriteKeywords(prefs.keywords.toSet())
            localUserProfileDataSource.saveNotificationsEnabled(prefs.notificationsEnabled)
        }
        return remoteResult
    }

    override suspend fun getFavoriteCategories(): Set<String> {
        return localUserProfileDataSource.getFavoriteCategories()
    }

    override suspend fun saveFavoriteCategories(categories: Set<String>): AppResult<UserPreferences> {
        localUserProfileDataSource.saveFavoriteCategories(categories)
        return pushPreferencesToRemote()
    }

    override suspend fun getFavoriteKeywords(): Set<String> {
        return localUserProfileDataSource.getFavoriteKeywords()
    }

    override suspend fun saveFavoriteKeywords(keywords: Set<String>): AppResult<UserPreferences> {
        localUserProfileDataSource.saveFavoriteKeywords(keywords)
        return pushPreferencesToRemote()
    }

    override suspend fun getNotificationsEnabled(): Boolean {
        return localUserProfileDataSource.getNotificationsEnabled()
    }

    override suspend fun saveNotificationsEnabled(enabled: Boolean): AppResult<UserPreferences> {
        localUserProfileDataSource.saveNotificationsEnabled(enabled)
        return pushPreferencesToRemote()
    }

    private suspend fun pushPreferencesToRemote(): AppResult<UserPreferences> {
        val currentPrefs = UserPreferences(
            categories = localUserProfileDataSource.getFavoriteCategories().toList(),
            keywords = localUserProfileDataSource.getFavoriteKeywords().toList(),
            notificationsEnabled = localUserProfileDataSource.getNotificationsEnabled(),
        )
        return remoteUserProfileDataSource.updatePreferences(currentPrefs)
    }
}
