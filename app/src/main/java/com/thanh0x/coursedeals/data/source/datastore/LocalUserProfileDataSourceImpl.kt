package com.thanh0x.coursedeals.data.source.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.thanh0x.coursedeals.domain.source.LocalUserProfileDataSource
import com.thanh0x.coursedeals.util.Constant
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class LocalUserProfileDataSourceImpl @Inject constructor(private val datastore: DataStore<Preferences>) :
    LocalUserProfileDataSource {
    private object PreferenceKey {
        val enableDarkMode = booleanPreferencesKey(Constant.PREFERENCES_ENABLE_DARK_MODE)
        val enableFingerPrint = booleanPreferencesKey(Constant.PREFERENCES_ENABLE_FINGERPRINT)
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
}
