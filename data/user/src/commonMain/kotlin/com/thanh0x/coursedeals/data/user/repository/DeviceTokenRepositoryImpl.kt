package com.thanh0x.coursedeals.data.user.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.thanh0x.coursedeals.core.common.Constant
import com.thanh0x.coursedeals.core.network.api.DeviceTokenApi
import com.thanh0x.coursedeals.core.network.model.DeviceTokenRequest
import com.thanh0x.coursedeals.domain.user.DeviceTokenRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DeviceTokenRepositoryImpl(
    private val deviceTokenApi: DeviceTokenApi,
    private val datastore: DataStore<Preferences>,
) : DeviceTokenRepository {

    private object PreferenceKey {
        val deviceToken = stringPreferencesKey(Constant.PREFERENCES_DEVICE_TOKEN)
        val subscribedTopics = stringSetPreferencesKey(Constant.PREFERENCES_SUBSCRIBED_TOPICS)
    }

    override suspend fun getDeviceToken(): String? {
        return datastore.data.map { preferences ->
            preferences[PreferenceKey.deviceToken]
        }.first()
    }

    override suspend fun saveDeviceToken(token: String) {
        datastore.edit { preferences ->
            preferences[PreferenceKey.deviceToken] = token
        }
    }

    override suspend fun clearDeviceToken() {
        datastore.edit { preferences ->
            preferences.remove(PreferenceKey.deviceToken)
        }
    }

    override fun getSubscribedTopics(): Flow<Set<String>> {
        return datastore.data.map { preferences ->
            preferences[PreferenceKey.subscribedTopics] ?: emptySet()
        }
    }

    override suspend fun saveSubscribedTopics(topics: Set<String>) {
        datastore.edit { preferences ->
            preferences[PreferenceKey.subscribedTopics] = topics
        }
    }

    override suspend fun registerDeviceTokenRemote(token: String, platform: String): Result<Unit> {
        val response = deviceTokenApi.registerDeviceToken(
            DeviceTokenRequest(
                fcmToken = token,
                platform = platform,
            )
        )
        return if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(Exception(response.message.ifBlank { "Failed to register device token" }))
        }
    }

    override suspend fun deleteDeviceTokenRemote(token: String): Result<Unit> {
        val response = deviceTokenApi.deleteDeviceToken(token)
        return if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(Exception(response.message.ifBlank { "Failed to delete device token" }))
        }
    }
}
