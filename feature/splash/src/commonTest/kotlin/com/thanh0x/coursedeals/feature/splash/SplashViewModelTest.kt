package com.thanh0x.coursedeals.feature.splash

import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.domain.user.AuthCredentials
import com.thanh0x.coursedeals.domain.user.FeatureFlagManager
import com.thanh0x.coursedeals.domain.user.TokenData
import com.thanh0x.coursedeals.domain.user.UserAuthenticationRepository
import com.thanh0x.coursedeals.domain.user.model.AppConfig
import com.thanh0x.coursedeals.domain.user.usecase.GetLocalTokenUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeSplashFeatureFlagManager(
    initialConfig: AppConfig = AppConfig(),
) : FeatureFlagManager {
    private val _configFlow = MutableStateFlow(initialConfig)
    override val configFlow: StateFlow<AppConfig> = _configFlow.asStateFlow()

    var refreshDelayMs: Long = 0L
    var refreshResult: Result<AppConfig> = Result.success(initialConfig)

    fun setConfig(config: AppConfig) {
        _configFlow.value = config
        refreshResult = Result.success(config)
    }

    override suspend fun refresh(): Result<AppConfig> {
        if (refreshDelayMs > 0L) {
            delay(refreshDelayMs)
        }
        refreshResult.getOrNull()?.let { _configFlow.value = it }
        return refreshResult
    }

    override fun boolean(key: String, default: Boolean): Boolean = default
    override fun double(key: String, default: Double): Double = default
    override fun string(key: String, default: String): String = default
}

private class FakeSplashAuthRepository(
    var localToken: String? = null,
) : UserAuthenticationRepository {
    override suspend fun getLocalToken(): String? = localToken
    override suspend fun saveLocalToken(token: String) {
        localToken = token
    }
    override suspend fun clearLocalToken() {
        localToken = null
    }
    override suspend fun getCipherTextWrapper(): String? = null
    override suspend fun saveCipherTextWrapper(fingerprintToken: String) = Unit
    override suspend fun clearCipherTextWrapper() = Unit
    override suspend fun getLocalRefreshToken(): String? = null
    override suspend fun saveLocalRefreshToken(refreshToken: String) = Unit
    override suspend fun clearLocalRefreshToken() = Unit
    override suspend fun login(credentials: AuthCredentials): AppResult<TokenData> =
        AppResult.Error(message = "Unused")
    override suspend fun register(email: String, password: String, fullName: String?): AppResult<String> =
        AppResult.Error(message = "Unused")
    override suspend fun verifyOtp(email: String, otp: String): AppResult<TokenData> =
        AppResult.Error(message = "Unused")
    override suspend fun sendOtp(email: String): AppResult<String> =
        AppResult.Error(message = "Unused")
    override suspend fun socialLogin(provider: String, idToken: String): AppResult<TokenData> =
        AppResult.Error(message = "Unused")
    override suspend fun refreshToken(refreshToken: String): AppResult<TokenData> =
        AppResult.Error(message = "Unused")
    override suspend fun checkIfTokenExpired(): AppResult<Unit> =
        AppResult.Error(message = "Unused")
    override suspend fun requestFingerprintToken(): AppResult<TokenData> =
        AppResult.Error(message = "Unused")
    override suspend fun requestAccessToken(): AppResult<TokenData> =
        AppResult.Error(message = "Unused")
}

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeFeatureFlagManager: FakeSplashFeatureFlagManager
    private lateinit var fakeAuthRepository: FakeSplashAuthRepository

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeFeatureFlagManager = FakeSplashFeatureFlagManager()
        fakeAuthRepository = FakeSplashAuthRepository()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun outdatedVersion_belowMinSupported_triggersUnskippableForceUpdateGate() = runTest(testDispatcher) {
        fakeFeatureFlagManager.setConfig(
            AppConfig(
                minSupportedVersionCode = 105,
                latestVersionCode = 106,
                forceUpdateUrl = "https://play.google.com/store/apps/details?id=com.thanh0x.coursedeals",
            )
        )
        fakeAuthRepository.localToken = "valid-jwt-token"

        val viewModel = SplashViewModel(
            featureFlagManager = fakeFeatureFlagManager,
            getLocalTokenUseCase = GetLocalTokenUseCase(fakeAuthRepository),
            currentVersionCodeProvider = { 100 },
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isForceUpdateRequired)
        assertNull(state.navigationTarget)
        assertEquals(
            "https://play.google.com/store/apps/details?id=com.thanh0x.coursedeals",
            state.forceUpdateUrl,
        )
    }

    @Test
    fun supportedVersion_withLocalToken_navigatesToMainAndMarksUpdateRecommended() = runTest(testDispatcher) {
        fakeFeatureFlagManager.setConfig(
            AppConfig(
                minSupportedVersionCode = 105,
                latestVersionCode = 106,
            )
        )
        fakeAuthRepository.localToken = "existing-jwt-token"

        val viewModel = SplashViewModel(
            featureFlagManager = fakeFeatureFlagManager,
            getLocalTokenUseCase = GetLocalTokenUseCase(fakeAuthRepository),
            currentVersionCodeProvider = { 105 },
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isForceUpdateRequired)
        assertTrue(state.isUpdateRecommended)
        assertEquals(SplashNavigationTarget.MAIN, state.navigationTarget)
    }

    @Test
    fun supportedVersion_withoutLocalToken_navigatesToLogin() = runTest(testDispatcher) {
        fakeFeatureFlagManager.setConfig(
            AppConfig(
                minSupportedVersionCode = 105,
                latestVersionCode = 106,
            )
        )
        fakeAuthRepository.localToken = null

        val viewModel = SplashViewModel(
            featureFlagManager = fakeFeatureFlagManager,
            getLocalTokenUseCase = GetLocalTokenUseCase(fakeAuthRepository),
            currentVersionCodeProvider = { 106 },
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isForceUpdateRequired)
        assertFalse(state.isUpdateRecommended)
        assertEquals(SplashNavigationTarget.LOGIN, state.navigationTarget)
    }

    @Test
    fun offlineOrSlowNetwork_timesOutBoundedWaitAndProceedsUsingCachedSnapshot() = runTest(testDispatcher) {
        fakeFeatureFlagManager.refreshDelayMs = 60_000L
        fakeFeatureFlagManager.refreshResult = Result.failure(IllegalStateException("Network timeout"))
        fakeAuthRepository.localToken = null

        val viewModel = SplashViewModel(
            featureFlagManager = fakeFeatureFlagManager,
            getLocalTokenUseCase = GetLocalTokenUseCase(fakeAuthRepository),
            currentVersionCodeProvider = { 105 },
            configSyncTimeoutMs = 1_500L,
        )

        advanceTimeBy(1_500L)
        runCurrent()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isForceUpdateRequired)
        assertEquals(SplashNavigationTarget.LOGIN, state.navigationTarget)
    }
}
