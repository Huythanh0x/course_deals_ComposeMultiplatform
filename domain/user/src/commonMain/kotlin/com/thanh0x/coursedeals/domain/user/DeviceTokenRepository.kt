package com.thanh0x.coursedeals.domain.user

import kotlinx.coroutines.flow.Flow

interface DeviceTokenRepository {
    suspend fun getDeviceToken(): String?
    suspend fun saveDeviceToken(token: String)
    suspend fun clearDeviceToken()
    fun getSubscribedTopics(): Flow<Set<String>>
    suspend fun saveSubscribedTopics(topics: Set<String>)
    suspend fun registerDeviceTokenRemote(token: String, platform: String): Result<Unit>
    suspend fun deleteDeviceTokenRemote(token: String): Result<Unit>
}
