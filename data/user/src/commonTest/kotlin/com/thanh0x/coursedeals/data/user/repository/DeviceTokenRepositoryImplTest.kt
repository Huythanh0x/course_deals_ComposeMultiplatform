package com.thanh0x.coursedeals.data.user.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.thanh0x.coursedeals.core.network.NetworkResponse
import com.thanh0x.coursedeals.core.network.api.DeviceTokenApi
import com.thanh0x.coursedeals.core.network.model.DeviceTokenRequest
import com.thanh0x.coursedeals.core.network.model.DeviceTokenResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DeviceTokenRepositoryImplTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeApi: FakeDeviceTokenApi
    private lateinit var repository: DeviceTokenRepositoryImpl

    @BeforeTest
    fun setUp() {
        fakeApi = FakeDeviceTokenApi()
        val tempPath = "build/tmp/test_datastore_${kotlin.random.Random.nextLong()}.preferences_pb"
        val dataStore = PreferenceDataStoreFactory.createWithPath(
            scope = CoroutineScope(testDispatcher),
            produceFile = { tempPath.toPath() },
        )
        repository = DeviceTokenRepositoryImpl(fakeApi, dataStore)
    }

    @Test
    fun saveAndGetDeviceToken() = runTest(testDispatcher) {
        assertNull(repository.getDeviceToken())
        repository.saveDeviceToken("token_123")
        assertEquals("token_123", repository.getDeviceToken())
        repository.clearDeviceToken()
        assertNull(repository.getDeviceToken())
    }

    @Test
    fun saveAndGetSubscribedTopics() = runTest(testDispatcher) {
        val topics = setOf("deals_all", "deals_development")
        repository.saveSubscribedTopics(topics)
        assertEquals(topics, repository.getSubscribedTopics().first())
    }

    @Test
    fun registerDeviceTokenRemote_success() = runTest(testDispatcher) {
        fakeApi.shouldSucceed = true
        val result = repository.registerDeviceTokenRemote("test_token", "ANDROID")
        assertTrue(result.isSuccess)
        assertEquals("test_token", fakeApi.lastRequest?.fcmToken)
        assertEquals("ANDROID", fakeApi.lastRequest?.platform)
    }

    @Test
    fun registerDeviceTokenRemote_failure() = runTest(testDispatcher) {
        fakeApi.shouldSucceed = false
        val result = repository.registerDeviceTokenRemote("test_token", "ANDROID")
        assertTrue(result.isFailure)
    }

    @Test
    fun deleteDeviceTokenRemote_success() = runTest(testDispatcher) {
        fakeApi.shouldSucceed = true
        val result = repository.deleteDeviceTokenRemote("test_token")
        assertTrue(result.isSuccess)
        assertEquals("test_token", fakeApi.lastDeletedToken)
    }

    private class FakeDeviceTokenApi : DeviceTokenApi {
        var shouldSucceed = true
        var lastRequest: DeviceTokenRequest? = null
        var lastDeletedToken: String? = null

        override suspend fun registerDeviceToken(request: DeviceTokenRequest): NetworkResponse<DeviceTokenResponse> {
            lastRequest = request
            return if (shouldSucceed) {
                NetworkResponse.success(DeviceTokenResponse(request.fcmToken, request.platform, 1L, true), 200)
            } else {
                NetworkResponse.error(500, "Server Error")
            }
        }

        override suspend fun deleteDeviceToken(token: String): NetworkResponse<Unit> {
            lastDeletedToken = token
            return if (shouldSucceed) {
                NetworkResponse.success(Unit, 200)
            } else {
                NetworkResponse.error(500, "Server Error")
            }
        }
    }
}
