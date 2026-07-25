package com.batdaulaptrinh.freeudemycoupons.data.source.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.batdaulaptrinh.freeudemycoupons.domain.source.LocalAuthenticationDataSource
import com.batdaulaptrinh.freeudemycoupons.util.Constant
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class LocalAuthenticationDataSourceImpl @Inject constructor(private val datastore: DataStore<Preferences>) :
    LocalAuthenticationDataSource {
    private object PreferenceKey {
        val userLoginToken = stringPreferencesKey(Constant.PREFERENCES_LOGIN_TOKEN)
        val refreshToken = stringPreferencesKey(Constant.PREFERENCES_REFRESH_TOKEN)
        val fingerprintToken = stringPreferencesKey(Constant.PREFERENCES_FINGERPRINT_TOKEN)
    }

    override suspend fun getLocalToken(): String? {
        return datastore.data.first()[PreferenceKey.userLoginToken]
    }

    override suspend fun saveLocalToken(newLoginToken: String) {
        datastore.edit { preferences ->
            preferences[PreferenceKey.userLoginToken] = newLoginToken
        }
    }

    override suspend fun clearLocalToken() {
        datastore.edit { preferences ->
            preferences.remove(PreferenceKey.userLoginToken)
        }
    }


    override suspend fun getCipherTextWrapper(): String? {
        return datastore.data.first()[PreferenceKey.fingerprintToken]
    }

    override suspend fun saveCipherTextWrapper(newLoginToken: String) {
        datastore.edit { preferences ->
            preferences[PreferenceKey.fingerprintToken] = newLoginToken
        }
    }

    override suspend fun clearCipherTextWrapper() {
        datastore.edit { preferences ->
            preferences.remove(PreferenceKey.fingerprintToken)
        }
    }


    override suspend fun getLocalRefreshToken(): String? {
        return datastore.data.first()[PreferenceKey.refreshToken]
    }

    override suspend fun saveLocalRefreshToken(newLoginToken: String) {
        datastore.edit { preferences ->
            preferences[PreferenceKey.refreshToken] = newLoginToken
        }
    }

    override suspend fun clearLocalRefreshToken() {
        datastore.edit { preferences ->
            preferences.remove(PreferenceKey.refreshToken)
        }
    }
}
