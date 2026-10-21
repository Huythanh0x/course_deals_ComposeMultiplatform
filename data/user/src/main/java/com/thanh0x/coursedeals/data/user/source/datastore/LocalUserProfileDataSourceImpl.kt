package com.thanh0x.coursedeals.data.user.source.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.thanh0x.coursedeals.domain.user.source.LocalUserProfileDataSource
import com.thanh0x.coursedeals.core.common.Constant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalUserProfileDataSourceImpl @Inject constructor(private val datastore: DataStore<Preferences>) :
    LocalUserProfileDataSource {
    private object PreferenceKey {
        val enableDarkMode = booleanPreferencesKey(Constant.PREFERENCES_ENABLE_DARK_MODE)
        val enableFingerPrint = booleanPreferencesKey(Constant.PREFERENCES_ENABLE_FINGERPRINT)
        val favCategories = stringSetPreferencesKey(Constant.PREFERENCES_FAV_CATEGORIES)
        val favKeywords = stringSetPreferencesKey(Constant.PREFERENCES_FAV_KEYWORDS)
        val notificationsEnabled = booleanPreferencesKey(Constant.PREFERENCES_NOTIFICATIONS_ENABLED)
        val updatedAt = longPreferencesKey(Constant.PREFERENCES_UPDATED_AT)
        val userName = stringPreferencesKey(Constant.PREFERENCES_USER_NAME)
        val userEmail = stringPreferencesKey(Constant.PREFERENCES_USER_EMAIL)
        val userAvatar = stringPreferencesKey(Constant.PREFERENCES_USER_AVATAR)
    }

    override suspend fun saveEnableDarkMode(isDarkModeEnable: Boolean) {
        datastore.edit { preferences ->
            preferences[PreferenceKey.enableDarkMode] = isDarkModeEnable
        }
    }

    override suspend fun getEnableDarkMode(): Boolean? {
        return datastore.data.first()[PreferenceKey.enableDarkMode]
    }

    override suspend fun saveEnableFingerPrint(isFingerPrintEnable: Boolean) {
        datastore.edit { preferences ->
            preferences[PreferenceKey.enableFingerPrint] = isFingerPrintEnable
        }
    }

    override suspend fun getEnableFingerPrint(): Boolean? {
        return datastore.data.first()[PreferenceKey.enableFingerPrint]
    }

    override suspend fun getFavoriteCategories(): Set<String> {
        return datastore.data.map { preferences ->
            preferences[PreferenceKey.favCategories] ?: emptySet()
        }.first()
    }

    override suspend fun saveFavoriteCategories(categories: Set<String>) {
        datastore.edit { preferences ->
            preferences[PreferenceKey.favCategories] = categories
        }
    }

    override suspend fun getFavoriteKeywords(): Set<String> {
        return datastore.data.map { preferences ->
            preferences[PreferenceKey.favKeywords] ?: emptySet()
        }.first()
    }

    override suspend fun saveFavoriteKeywords(keywords: Set<String>) {
        datastore.edit { preferences ->
            preferences[PreferenceKey.favKeywords] = keywords
        }
    }

    override suspend fun getNotificationsEnabled(): Boolean {
        return datastore.data.map { preferences ->
            preferences[PreferenceKey.notificationsEnabled] ?: true
        }.first()
    }

    override suspend fun saveNotificationsEnabled(enabled: Boolean) {
        datastore.edit { preferences ->
            preferences[PreferenceKey.notificationsEnabled] = enabled
        }
    }

    override suspend fun getPreferencesUpdatedAt(): Long {
        return datastore.data.map { preferences ->
            preferences[PreferenceKey.updatedAt] ?: 0L
        }.first()
    }

    override suspend fun savePreferencesUpdatedAt(timestamp: Long) {
        datastore.edit { preferences ->
            preferences[PreferenceKey.updatedAt] = timestamp
        }
    }

    override suspend fun getUserName(): String? {
        return datastore.data.map { preferences ->
            preferences[PreferenceKey.userName]
        }.first()
    }

    override suspend fun saveUserName(name: String?) {
        datastore.edit { preferences ->
            if (name != null) {
                preferences[PreferenceKey.userName] = name
            } else {
                preferences.remove(PreferenceKey.userName)
            }
        }
    }

    override suspend fun getUserEmail(): String? {
        return datastore.data.map { preferences ->
            preferences[PreferenceKey.userEmail]
        }.first()
    }

    override suspend fun saveUserEmail(email: String?) {
        datastore.edit { preferences ->
            if (email != null) {
                preferences[PreferenceKey.userEmail] = email
            } else {
                preferences.remove(PreferenceKey.userEmail)
            }
        }
    }

    override suspend fun getUserAvatar(): String? {
        return datastore.data.map { preferences ->
            preferences[PreferenceKey.userAvatar]
        }.first()
    }

    override suspend fun saveUserAvatar(avatarUrl: String?) {
        datastore.edit { preferences ->
            if (avatarUrl != null) {
                preferences[PreferenceKey.userAvatar] = avatarUrl
            } else {
                preferences.remove(PreferenceKey.userAvatar)
            }
        }
    }
}
