package com.thanh0x.coursedeals.data.source.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.thanh0x.coursedeals.domain.source.LocalSettingsDataSource
import com.thanh0x.coursedeals.core.common.Constant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalSettingsDataSourceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : LocalSettingsDataSource {

    private object PreferenceKey {
        val showLocalFetchTime = booleanPreferencesKey(Constant.PREFERENCES_SHOW_LOCAL_FETCH_TIME)
    }

    override fun getShowLocalFetchTime(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[PreferenceKey.showLocalFetchTime] ?: false
        }
    }

    override suspend fun saveShowLocalFetchTime(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKey.showLocalFetchTime] = show
        }
    }
}
