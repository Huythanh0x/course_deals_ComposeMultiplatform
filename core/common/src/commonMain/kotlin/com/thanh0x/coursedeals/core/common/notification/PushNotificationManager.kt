package com.thanh0x.coursedeals.core.common.notification

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface PushNotificationManager {
    val currentToken: StateFlow<String?>
    suspend fun registerDeviceToken(token: String): Result<Unit>
    suspend fun subscribeToTopic(topic: String): Result<Unit>
    suspend fun unsubscribeFromTopic(topic: String): Result<Unit>
    fun getSubscribedTopics(): Flow<Set<String>>
}
