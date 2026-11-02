package com.thanh0x.coursedeals.shared

import com.thanh0x.coursedeals.core.common.notification.PushNotificationManager
import com.thanh0x.coursedeals.domain.user.DeviceTokenRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class IosPushNotificationManager(
    private val deviceTokenRepository: DeviceTokenRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : PushNotificationManager {

    private val _currentToken = MutableStateFlow<String?>(null)
    override val currentToken: StateFlow<String?> = _currentToken.asStateFlow()

    init {
        coroutineScope.launch {
            _currentToken.value = deviceTokenRepository.getDeviceToken()
        }
    }

    override suspend fun registerDeviceToken(token: String): Result<Unit> {
        _currentToken.value = token
        deviceTokenRepository.saveDeviceToken(token)
        return deviceTokenRepository.registerDeviceTokenRemote(token, "IOS")
    }

    override suspend fun subscribeToTopic(topic: String): Result<Unit> {
        return runCatching {
            val currentTopics = deviceTokenRepository.getSubscribedTopics().first().toMutableSet()
            currentTopics.add(topic)
            deviceTokenRepository.saveSubscribedTopics(currentTopics)
        }
    }

    override suspend fun unsubscribeFromTopic(topic: String): Result<Unit> {
        return runCatching {
            val currentTopics = deviceTokenRepository.getSubscribedTopics().first().toMutableSet()
            currentTopics.remove(topic)
            deviceTokenRepository.saveSubscribedTopics(currentTopics)
        }
    }

    override fun getSubscribedTopics(): Flow<Set<String>> {
        return deviceTokenRepository.getSubscribedTopics()
    }
}
