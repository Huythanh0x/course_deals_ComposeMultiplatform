package com.thanh0x.coursedeals.data.source.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.thanh0x.coursedeals.domain.source.LocalUserProfileDataSource
import com.thanh0x.coursedeals.util.Constant
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
}
