package com.thanh0x.coursedeals.data.user.repository

import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.domain.user.UserPreferences
import com.thanh0x.coursedeals.domain.user.UserProfileRepository
import com.thanh0x.coursedeals.domain.user.source.LocalUserProfileDataSource
import com.thanh0x.coursedeals.domain.user.source.RemoteUserProfileDataSource
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
        val localUpdatedAt = localUserProfileDataSource.getPreferencesUpdatedAt()

        if (remoteResult is AppResult.Success) {
            val remotePrefs = remoteResult.data
            if (remotePrefs.updatedAt >= localUpdatedAt) {
                // Remote is newer or equal, update local
                updateLocalStore(remotePrefs)
            } else {
                // Local is newer, push to remote
                return pushPreferencesToRemote()
            }
        }
        return remoteResult
    }

    private suspend fun updateLocalStore(prefs: UserPreferences) {
        localUserProfileDataSource.saveFavoriteCategories(prefs.categories.toSet())
        localUserProfileDataSource.saveFavoriteKeywords(prefs.keywords.toSet())
        localUserProfileDataSource.saveNotificationsEnabled(prefs.notificationsEnabled)
        localUserProfileDataSource.savePreferencesUpdatedAt(prefs.updatedAt)
    }

    override suspend fun getFavoriteCategories(): Set<String> {
        return localUserProfileDataSource.getFavoriteCategories()
    }

    override suspend fun saveFavoriteCategories(categories: Set<String>): AppResult<UserPreferences> {
        updateLocalTimestamp()
        localUserProfileDataSource.saveFavoriteCategories(categories)
        return pushPreferencesToRemote()
    }

    override suspend fun getFavoriteKeywords(): Set<String> {
        return localUserProfileDataSource.getFavoriteKeywords()
    }

    override suspend fun saveFavoriteKeywords(keywords: Set<String>): AppResult<UserPreferences> {
        updateLocalTimestamp()
        localUserProfileDataSource.saveFavoriteKeywords(keywords)
        return pushPreferencesToRemote()
    }

    override suspend fun getNotificationsEnabled(): Boolean {
        return localUserProfileDataSource.getNotificationsEnabled()
    }

    override suspend fun saveNotificationsEnabled(enabled: Boolean): AppResult<UserPreferences> {
        updateLocalTimestamp()
        localUserProfileDataSource.saveNotificationsEnabled(enabled)
        return pushPreferencesToRemote()
    }

    private suspend fun updateLocalTimestamp() {
        localUserProfileDataSource.savePreferencesUpdatedAt(System.currentTimeMillis())
    }

    private suspend fun pushPreferencesToRemote(): AppResult<UserPreferences> {
        val currentPrefs = UserPreferences(
            categories = localUserProfileDataSource.getFavoriteCategories().toList(),
            keywords = localUserProfileDataSource.getFavoriteKeywords().toList(),
            notificationsEnabled = localUserProfileDataSource.getNotificationsEnabled(),
            updatedAt = localUserProfileDataSource.getPreferencesUpdatedAt(),
        )
        return remoteUserProfileDataSource.updatePreferences(currentPrefs)
    }
}
