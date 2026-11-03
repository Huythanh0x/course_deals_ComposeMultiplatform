package com.thanh0x.coursedeals.fcm

import com.thanh0x.coursedeals.domain.user.DeviceTokenRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AndroidPushNotificationManagerTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeDeviceTokenRepository
    private lateinit var manager: AndroidPushNotificationManager

    @Before
    fun setUp() {
        fakeRepository = FakeDeviceTokenRepository()
    }

    @Test
    fun init_withExistingToken_setsCurrentToken() = runTest(testDispatcher) {
        fakeRepository.token = "cached_token_123"
        manager = AndroidPushNotificationManager(
            deviceTokenRepository = fakeRepository,
            firebaseMessagingProvider = { error("Firebase should not be invoked when token exists") },
            ioDispatcher = testDispatcher,
        )
        testScheduler.advanceUntilIdle()

        assertEquals("cached_token_123", manager.currentToken.value)
    }

    @Test
    fun registerDeviceToken_savesAndRegistersRemote() = runTest(testDispatcher) {
        manager = AndroidPushNotificationManager(
            deviceTokenRepository = fakeRepository,
            firebaseMessagingProvider = { error("No fcm") },
            ioDispatcher = testDispatcher,
        )
        testScheduler.advanceUntilIdle()

        val result = manager.registerDeviceToken("new_fcm_token")
        assertTrue(result.isSuccess)
        assertEquals("new_fcm_token", fakeRepository.token)
        assertEquals("new_fcm_token", fakeRepository.lastRegisteredToken)
        assertEquals("ANDROID", fakeRepository.lastRegisteredPlatform)
        assertEquals("new_fcm_token", manager.currentToken.value)
    }

    @Test
    fun subscribeAndUnsubscribeTopic_updatesRepository() = runTest(testDispatcher) {
        manager = AndroidPushNotificationManager(
            deviceTokenRepository = fakeRepository,
            firebaseMessagingProvider = { error("No fcm in unit tests") },
            ioDispatcher = testDispatcher,
        )
        testScheduler.advanceUntilIdle()

        manager.subscribeToTopic("deals_all")
        testScheduler.advanceUntilIdle()
        assertTrue(manager.getSubscribedTopics().first().contains("deals_all"))

        manager.subscribeToTopic("deals_development")
        testScheduler.advanceUntilIdle()
        assertTrue(manager.getSubscribedTopics().first().contains("deals_development"))

        manager.unsubscribeFromTopic("deals_all")
        testScheduler.advanceUntilIdle()
        assertTrue(!manager.getSubscribedTopics().first().contains("deals_all"))
        assertTrue(manager.getSubscribedTopics().first().contains("deals_development"))
    }

    private class FakeDeviceTokenRepository : DeviceTokenRepository {
        var token: String? = null
        val topicsFlow = MutableStateFlow<Set<String>>(emptySet())
        var lastRegisteredToken: String? = null
        var lastRegisteredPlatform: String? = null
        var lastDeletedToken: String? = null

        override suspend fun getDeviceToken(): String? = token

        override suspend fun saveDeviceToken(token: String) {
            this.token = token
        }

        override suspend fun clearDeviceToken() {
            token = null
        }

        override fun getSubscribedTopics(): Flow<Set<String>> = topicsFlow

        override suspend fun saveSubscribedTopics(topics: Set<String>) {
            topicsFlow.value = topics
        }

        override suspend fun registerDeviceTokenRemote(token: String, platform: String): Result<Unit> {
            lastRegisteredToken = token
            lastRegisteredPlatform = platform
            return Result.success(Unit)
        }

        override suspend fun deleteDeviceTokenRemote(token: String): Result<Unit> {
            lastDeletedToken = token
            return Result.success(Unit)
        }
    }
}
