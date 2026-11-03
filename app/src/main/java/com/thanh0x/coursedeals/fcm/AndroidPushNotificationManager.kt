package com.thanh0x.coursedeals.fcm

import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.thanh0x.coursedeals.core.common.notification.PushNotificationManager
import com.thanh0x.coursedeals.domain.user.DeviceTokenRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AndroidPushNotificationManager(
    private val deviceTokenRepository: DeviceTokenRepository,
    private val firebaseMessagingProvider: () -> FirebaseMessaging = { FirebaseMessaging.getInstance() },
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : PushNotificationManager {

    private val scope = CoroutineScope(ioDispatcher + SupervisorJob())
    private val _currentToken = MutableStateFlow<String?>(null)
    override val currentToken: StateFlow<String?> = _currentToken.asStateFlow()

    init {
        scope.launch {
            val savedToken = deviceTokenRepository.getDeviceToken()
            if (!savedToken.isNullOrBlank()) {
                _currentToken.value = savedToken
            } else {
                fetchAndRegisterFcmToken()
            }
        }
    }

    private suspend fun fetchAndRegisterFcmToken() {
        runCatching {
            val token = withContext(ioDispatcher) {
                firebaseMessagingProvider().token.awaitTask()
            }
            if (!token.isNullOrBlank()) {
                registerDeviceToken(token)
            }
        }
    }

    override suspend fun registerDeviceToken(token: String): Result<Unit> {
        _currentToken.value = token
        deviceTokenRepository.saveDeviceToken(token)
        return deviceTokenRepository.registerDeviceTokenRemote(token, "ANDROID")
    }

    override suspend fun subscribeToTopic(topic: String): Result<Unit> = withContext(ioDispatcher) {
        val currentTopics = deviceTokenRepository.getSubscribedTopics().first().toMutableSet()
        currentTopics.add(topic)
        deviceTokenRepository.saveSubscribedTopics(currentTopics)

        runCatching {
            firebaseMessagingProvider().subscribeToTopic(topic).awaitTask()
            Unit
        }
    }

    override suspend fun unsubscribeFromTopic(topic: String): Result<Unit> = withContext(ioDispatcher) {
        val currentTopics = deviceTokenRepository.getSubscribedTopics().first().toMutableSet()
        currentTopics.remove(topic)
        deviceTokenRepository.saveSubscribedTopics(currentTopics)

        runCatching {
            firebaseMessagingProvider().unsubscribeFromTopic(topic).awaitTask()
            Unit
        }
    }

    override fun getSubscribedTopics(): Flow<Set<String>> {
        return deviceTokenRepository.getSubscribedTopics()
    }

    private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { cont ->
        addOnSuccessListener { result ->
            if (cont.isActive) cont.resume(result)
        }
        addOnFailureListener { exception ->
            if (cont.isActive) cont.resumeWithException(exception)
        }
        addOnCanceledListener {
            if (cont.isActive) cont.cancel()
        }
    }
}
