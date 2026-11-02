package com.thanh0x.coursedeals.data.user

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.thanh0x.coursedeals.core.common.Constant
import com.thanh0x.coursedeals.core.network.NetworkResponse
import com.thanh0x.coursedeals.data.user.source.remote.AppConfigService
import com.thanh0x.coursedeals.domain.user.model.AppConfig
import com.thanh0x.coursedeals.domain.user.source.LocalAuthenticationDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class InMemoryPreferencesDataStore(
    initialPreferences: Preferences = emptyPreferences(),
) : DataStore<Preferences> {
    private val state = MutableStateFlow(initialPreferences)
    override val data: Flow<Preferences> = state.asStateFlow()

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        val updated = transform(state.value)
        state.value = updated
        return updated
    }

    fun currentPreferences(): Preferences = state.value
}

private class FakeAppConfigService : AppConfigService {
    val queuedResponses = ArrayDeque<NetworkResponse<String>>()
    var defaultResponse: NetworkResponse<String> = NetworkResponse.success(
        body = VALID_REMOTE_JSON,
        code = 200,
        message = "OK",
    )
    var callCount: Int = 0

    override suspend fun getAppConfigRawJson(): NetworkResponse<String> {
        callCount++
        return if (queuedResponses.isNotEmpty()) {
            queuedResponses.removeFirst()
        } else {
            defaultResponse
        }
    }

    companion object {
        val VALID_REMOTE_JSON =
            """
            {
              "minSupportedVersionCode": 105,
              "latestVersionCode": 106,
              "forceUpdateUrl": "https://play.google.com/store/apps/details?id=com.thanh0x.coursedeals",
              "featureFlags": {
                "clientVerificationEnabled": true,
                "communityReportsEnabled": true,
                "preferExternalBrowserCheckout": true,
                "enrollFlowReportThreshold": 0.45,
                "checkoutBannerVariant": "compact"
              }
            }
            """.trimIndent()
    }
}

private class FakeTokenDataSource(initialToken: String? = null) : LocalAuthenticationDataSource {
    private val tokenState = MutableStateFlow(initialToken)

    override fun observeLocalToken(): Flow<String?> = tokenState.asStateFlow()

    override suspend fun getLocalToken(): String? = tokenState.value

    override suspend fun saveLocalToken(newLoginToken: String) {
        tokenState.value = newLoginToken
    }

    override suspend fun clearLocalToken() {
        tokenState.value = null
    }

    override suspend fun getCipherTextWrapper(): String? = null
    override suspend fun saveCipherTextWrapper(newLoginToken: String) = Unit
    override suspend fun clearCipherTextWrapper() = Unit
    override suspend fun getLocalRefreshToken(): String? = null
    override suspend fun saveLocalRefreshToken(newLoginToken: String) = Unit
    override suspend fun clearLocalRefreshToken() = Unit
}

@OptIn(ExperimentalCoroutinesApi::class)
class FeatureFlagManagerImplTest {

    private val snapshotPrefKey = stringPreferencesKey(Constant.PREFERENCES_APP_CONFIG_SNAPSHOT)

    @Test
    fun init_loadsCachedSnapshotSynchronouslyBeforeRemoteRefreshCompletes() = runTest {
        val cachedJson =
            """
            {
              "minSupportedVersionCode": 108,
              "latestVersionCode": 110,
              "forceUpdateUrl": "https://coursedeals.thanh0x.com/update",
              "featureFlags": {
                "clientVerificationEnabled": true,
                "enrollFlowReportThreshold": 0.75,
                "customStringFlag": "cached_value"
              }
            }
            """.trimIndent()
        val dataStore = InMemoryPreferencesDataStore(
            mutablePreferencesOf(snapshotPrefKey to cachedJson),
        )
        val fakeService = FakeAppConfigService()
        val scope = TestScope(StandardTestDispatcher(testScheduler))

        val manager = FeatureFlagManagerImpl(
            dataStore = dataStore,
            appConfigService = fakeService,
            scope = scope,
            autoRefreshOnInit = false,
        )

        // Immediately available without advancing dispatcher
        assertEquals(108, manager.configFlow.value.minSupportedVersionCode)
        assertEquals(110, manager.configFlow.value.latestVersionCode)
        assertEquals("https://coursedeals.thanh0x.com/update", manager.configFlow.value.forceUpdateUrl)
        assertTrue(manager.boolean(AppConfig.FLAG_CLIENT_VERIFICATION_ENABLED, default = false))
        assertEquals(0.75, manager.double(AppConfig.FLAG_ENROLL_FLOW_REPORT_THRESHOLD, default = 0.3))
        assertEquals("cached_value", manager.string("customStringFlag", default = "fallback"))
    }

    @Test
    fun refresh_fetchesRemoteConfigAndPersistsSnapshotToDataStore() = runTest {
        val dataStore = InMemoryPreferencesDataStore()
        val fakeService = FakeAppConfigService()
        val scope = TestScope(StandardTestDispatcher(testScheduler))

        val manager = FeatureFlagManagerImpl(
            dataStore = dataStore,
            appConfigService = fakeService,
            scope = scope,
            autoRefreshOnInit = false,
        )

        val result = manager.refresh()
        assertTrue(result.isSuccess)
        assertEquals(1, fakeService.callCount)
        assertTrue(manager.boolean(AppConfig.FLAG_CLIENT_VERIFICATION_ENABLED, default = false))
        assertTrue(manager.boolean(AppConfig.FLAG_PREFER_EXTERNAL_BROWSER_CHECKOUT, default = false))
        assertEquals(0.45, manager.double(AppConfig.FLAG_ENROLL_FLOW_REPORT_THRESHOLD, default = 0.3))
        assertEquals("compact", manager.string("checkoutBannerVariant", default = "default"))

        val persistedJson = dataStore.currentPreferences()[snapshotPrefKey]
        assertEquals(FakeAppConfigService.VALID_REMOTE_JSON, persistedJson)
    }

    @Test
    fun refresh_retriesWithExponentialBackoff_5s_30s_2m_andRecoversOnThirdRetry() = runTest {
        val dataStore = InMemoryPreferencesDataStore()
        val fakeService = FakeAppConfigService().apply {
            queuedResponses.addLast(NetworkResponse.error(code = 503, message = "Service Unavailable"))
            queuedResponses.addLast(NetworkResponse.error(code = 502, message = "Bad Gateway"))
            queuedResponses.addLast(NetworkResponse.error(code = -1, message = "Network Offline"))
            queuedResponses.addLast(
                NetworkResponse.success(
                    body = FakeAppConfigService.VALID_REMOTE_JSON,
                    code = 200,
                    message = "OK",
                )
            )
        }
        val scope = TestScope(StandardTestDispatcher(testScheduler))

        val manager = FeatureFlagManagerImpl(
            dataStore = dataStore,
            appConfigService = fakeService,
            scope = scope,
            autoRefreshOnInit = true,
        )

        // Initial attempt at t = 0ms
        runCurrent()
        assertEquals(1, fakeService.callCount)

        // Before 5s backoff expires
        advanceTimeBy(4_999L)
        runCurrent()
        assertEquals(1, fakeService.callCount)

        // At t = 5s -> Retry #1
        advanceTimeBy(1L)
        runCurrent()
        assertEquals(2, fakeService.callCount)

        // Before 30s backoff expires
        advanceTimeBy(29_999L)
        runCurrent()
        assertEquals(2, fakeService.callCount)

        // At t = 35s (5s + 30s) -> Retry #2
        advanceTimeBy(1L)
        runCurrent()
        assertEquals(3, fakeService.callCount)

        // Before 120s backoff expires
        advanceTimeBy(119_999L)
        runCurrent()
        assertEquals(3, fakeService.callCount)

        // At t = 155s (5s + 30s + 120s) -> Retry #3 succeeds
        advanceTimeBy(1L)
        runCurrent()
        assertEquals(4, fakeService.callCount)
        assertTrue(manager.boolean(AppConfig.FLAG_CLIENT_VERIFICATION_ENABLED, default = false))
    }

    @Test
    fun authStateChange_triggersRefreshAutomatically() = runTest {
        val dataStore = InMemoryPreferencesDataStore()
        val fakeService = FakeAppConfigService()
        val fakeTokenDataSource = FakeTokenDataSource(initialToken = null)
        val scope = TestScope(StandardTestDispatcher(testScheduler))

        FeatureFlagManagerImpl(
            dataStore = dataStore,
            appConfigService = fakeService,
            localAuthenticationDataSource = fakeTokenDataSource,
            scope = scope,
            autoRefreshOnInit = true,
        )

        runCurrent()
        assertEquals(1, fakeService.callCount)

        // User logs in -> token changes from null to JWT
        fakeTokenDataSource.saveLocalToken("eyJhbGciOiJIUzI1NiJ9.user1")
        runCurrent()
        assertEquals(2, fakeService.callCount)

        // User logs out -> token cleared to null
        fakeTokenDataSource.clearLocalToken()
        runCurrent()
        assertEquals(3, fakeService.callCount)
    }

    @Test
    fun corruptedCachedJsonAndMissingKeys_fallBackToSafeDefaults() = runTest {
        val dataStore = InMemoryPreferencesDataStore(
            mutablePreferencesOf(snapshotPrefKey to "{corrupted-json-payload"),
        )
        val fakeService = FakeAppConfigService().apply {
            defaultResponse = NetworkResponse.error(code = -1, message = "Offline")
        }
        val scope = TestScope(StandardTestDispatcher(testScheduler))

        val manager = FeatureFlagManagerImpl(
            dataStore = dataStore,
            appConfigService = fakeService,
            scope = scope,
            autoRefreshOnInit = false,
        )

        assertEquals(105, manager.configFlow.value.minSupportedVersionCode)
        assertEquals(106, manager.configFlow.value.latestVersionCode)
        assertFalse(manager.boolean(AppConfig.FLAG_CLIENT_VERIFICATION_ENABLED, default = true))
        assertTrue(manager.boolean(AppConfig.FLAG_COMMUNITY_REPORTS_ENABLED, default = false))
        assertEquals(0.3, manager.double(AppConfig.FLAG_ENROLL_FLOW_REPORT_THRESHOLD, default = 0.9))

        // Missing keys and type mismatches return caller-supplied defaults
        assertTrue(manager.boolean("nonExistentBooleanFlag", default = true))
        assertFalse(manager.boolean(AppConfig.FLAG_ENROLL_FLOW_REPORT_THRESHOLD, default = false))
        assertEquals(0.85, manager.double("nonExistentDoubleFlag", default = 0.85))
        assertEquals(0.5, manager.double(AppConfig.FLAG_COMMUNITY_REPORTS_ENABLED, default = 0.5))
        assertEquals("fallback_str", manager.string("nonExistentStringFlag", default = "fallback_str"))
    }
}
