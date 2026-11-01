package com.thanh0x.coursedeals.feature.auth

import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.core.ui.UiEvent
import com.thanh0x.coursedeals.core.ui.util.NetworkUtil
import com.thanh0x.coursedeals.domain.user.AuthCredentials
import com.thanh0x.coursedeals.domain.user.TokenData
import com.thanh0x.coursedeals.domain.user.UserAuthenticationRepository
import com.thanh0x.coursedeals.domain.user.UserProfile
import com.thanh0x.coursedeals.domain.user.UserProfileRepository
import com.thanh0x.coursedeals.domain.user.UserPreferences
import com.thanh0x.coursedeals.domain.user.usecase.ClearCachedUserProfileUseCase
import com.thanh0x.coursedeals.domain.user.usecase.ClearLocalTokenUseCase
import com.thanh0x.coursedeals.domain.user.usecase.CryptographyManagerUseCase
import com.thanh0x.coursedeals.domain.user.usecase.LoadCipherTextWrapperUseCase
import com.thanh0x.coursedeals.domain.user.usecase.LoadDarkModeUseCase
import com.thanh0x.coursedeals.domain.user.usecase.LoadFingerprintUseCase
import com.thanh0x.coursedeals.domain.user.usecase.LoginUseCase
import com.thanh0x.coursedeals.domain.user.usecase.RegisterUseCase
import com.thanh0x.coursedeals.domain.user.usecase.RequestAccessTokenUseCase
import com.thanh0x.coursedeals.domain.user.usecase.SaveCipherTextWrapperUseCase
import com.thanh0x.coursedeals.domain.user.usecase.SaveDarkModeUseCase
import com.thanh0x.coursedeals.domain.user.usecase.SaveFingerprintUseCase
import com.thanh0x.coursedeals.domain.user.usecase.SaveJwtTokenUseCase
import com.thanh0x.coursedeals.domain.user.usecase.SaveRefreshTokenUseCase
import com.thanh0x.coursedeals.domain.user.usecase.SendOtpUseCase
import com.thanh0x.coursedeals.domain.user.usecase.SettingFingerprintUseCase
import com.thanh0x.coursedeals.domain.user.usecase.SettingUserProfileUseCase
import com.thanh0x.coursedeals.domain.user.usecase.SocialLoginUseCase
import com.thanh0x.coursedeals.domain.user.usecase.VerifyOtpUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeAuthRepo: FakeAuthRepository
    private lateinit var fakeUserProfileRepo: FakeUserProfileRepository
    private lateinit var viewModel: LoginViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeAuthRepo = FakeAuthRepository()
        fakeUserProfileRepo = FakeUserProfileRepository()

        val loginUseCase = LoginUseCase(fakeAuthRepo)
        val registerUseCase = RegisterUseCase(fakeAuthRepo)
        val verifyOtpUseCase = VerifyOtpUseCase(fakeAuthRepo)
        val sendOtpUseCase = SendOtpUseCase(fakeAuthRepo)
        val socialLoginUseCase = SocialLoginUseCase(fakeAuthRepo)
        val saveJwtTokenUseCase = SaveJwtTokenUseCase(fakeAuthRepo)
        val saveRefreshTokenUseCase = SaveRefreshTokenUseCase(fakeAuthRepo)
        val settingFingerprintUseCase = SettingFingerprintUseCase(
            loadCipherTextWrapperUseCase = LoadCipherTextWrapperUseCase(fakeAuthRepo),
            saveCipherTextWrapperUseCase = SaveCipherTextWrapperUseCase(fakeAuthRepo)
        )
        val cryptographyManagerUseCase = CryptographyManagerUseCase()
        val clearLocalTokenUseCase = ClearLocalTokenUseCase(fakeAuthRepo)
        val clearCachedUserProfileUseCase = ClearCachedUserProfileUseCase(fakeUserProfileRepo)
        val requestAccessTokenUseCase = RequestAccessTokenUseCase(fakeAuthRepo)
        val settingUserProfileUseCase = SettingUserProfileUseCase(
            saveDarkModeUseCase = SaveDarkModeUseCase(fakeUserProfileRepo),
            saveFingerPrintUseCase = SaveFingerprintUseCase(fakeUserProfileRepo),
            loadDarkModeUseCase = LoadDarkModeUseCase(fakeUserProfileRepo),
            loadFingerprintUseCase = LoadFingerprintUseCase(fakeUserProfileRepo)
        )
        val networkUtil = object : NetworkUtil() {
            override fun isInternetAvailable(): Boolean = true
        }

        viewModel = LoginViewModel(
            loginUseCase = loginUseCase,
            registerUseCase = registerUseCase,
            verifyOtpUseCase = verifyOtpUseCase,
            sendOtpUseCase = sendOtpUseCase,
            socialLoginUseCase = socialLoginUseCase,
            saveJwtTokenUseCase = saveJwtTokenUseCase,
            saveRefreshTokenUseCase = saveRefreshTokenUseCase,
            settingFingerprintUseCase = settingFingerprintUseCase,
            cryptographyManagerUseCase = cryptographyManagerUseCase,
            clearLocalTokenUseCase = clearLocalTokenUseCase,
            clearCachedUserProfileUseCase = clearCachedUserProfileUseCase,
            requestAccessTokenUseCase = requestAccessTokenUseCase,
            settingUserProfileUseCase = settingUserProfileUseCase,
            networkUtil = networkUtil,
            ioDispatcher = testDispatcher
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun sendOtp_withValidEmail_setsPendingEmailAndTransitionsToVerifyOtp() = runTest {
        viewModel.sendOtp("dev-user@coursedeals.com")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(AuthMode.VERIFY_OTP, state.authMode)
        assertEquals("dev-user@coursedeals.com", state.pendingEmail)
        assertNotNull(state.infoMessage)
        assertNull(state.error)
        assertEquals("dev-user@coursedeals.com", fakeAuthRepo.lastSentOtpEmail)
    }

    @Test
    fun sendOtp_withInvalidEmail_setsErrorStateWithoutCallingRepo() = runTest {
        viewModel.sendOtp("invalid-email")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(AuthMode.LOGIN, state.authMode)
        assertEquals("Please enter a valid email address", state.error)
        assertNull(fakeAuthRepo.lastSentOtpEmail)
    }

    @Test
    fun verifyOtp_existingUser_savesTokensAndNavigatesToMain() = runTest {
        viewModel.sendOtp("existing@coursedeals.com")
        advanceUntilIdle()

        fakeAuthRepo.verifyOtpResult = AppResult.Success(
            TokenData(
                accessToken = "valid-access-token",
                refreshToken = "valid-refresh-token",
                isNewUser = false
            )
        )

        var navigatedDestination: String? = null
        val job = launch {
            viewModel.uiEvent.collect { event ->
                if (event is UiEvent.Navigate) {
                    navigatedDestination = event.destination
                }
            }
        }

        viewModel.verifyOtp("000000")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.showTermsDialog)
        assertNull(state.pendingTokenData)
        assertEquals("valid-access-token", fakeAuthRepo.savedAccessToken)
        assertEquals("valid-refresh-token", fakeAuthRepo.savedRefreshToken)
        assertEquals("Main", navigatedDestination)
        job.cancel()
    }

    @Test
    fun verifyOtp_newUser_promptsTermsAndPrivacyDialogWithoutNavigating() = runTest {
        viewModel.sendOtp("new@coursedeals.com")
        advanceUntilIdle()

        val tokenData = TokenData(
            accessToken = "new-user-access-token",
            refreshToken = "new-user-refresh-token",
            isNewUser = true
        )
        fakeAuthRepo.verifyOtpResult = AppResult.Success(tokenData)

        var navigatedDestination: String? = null
        val job = launch {
            viewModel.uiEvent.collect { event ->
                if (event is UiEvent.Navigate) {
                    navigatedDestination = event.destination
                }
            }
        }

        viewModel.verifyOtp("000000")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.showTermsDialog)
        assertEquals(tokenData, state.pendingTokenData)
        assertNull(fakeAuthRepo.savedAccessToken)
        assertNull(navigatedDestination)
        job.cancel()
    }

    @Test
    fun acceptTerms_savesPendingTokensAndNavigatesToMain() = runTest {
        viewModel.sendOtp("new@coursedeals.com")
        advanceUntilIdle()

        val tokenData = TokenData(
            accessToken = "new-user-access-token",
            refreshToken = "new-user-refresh-token",
            isNewUser = true
        )
        fakeAuthRepo.verifyOtpResult = AppResult.Success(tokenData)

        viewModel.verifyOtp("000000")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.showTermsDialog)

        var navigatedDestination: String? = null
        val job = launch {
            viewModel.uiEvent.collect { event ->
                if (event is UiEvent.Navigate) {
                    navigatedDestination = event.destination
                }
            }
        }

        viewModel.acceptTerms()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.showTermsDialog)
        assertNull(state.pendingTokenData)
        assertEquals("new-user-access-token", fakeAuthRepo.savedAccessToken)
        assertEquals("Main", navigatedDestination)
        job.cancel()
    }

    @Test
    fun declineTerms_clearsPendingTokensAndDisplaysError() = runTest {
        viewModel.sendOtp("new@coursedeals.com")
        advanceUntilIdle()

        val tokenData = TokenData(
            accessToken = "new-user-access-token",
            refreshToken = "new-user-refresh-token",
            isNewUser = true
        )
        fakeAuthRepo.verifyOtpResult = AppResult.Success(tokenData)

        viewModel.verifyOtp("000000")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.showTermsDialog)

        viewModel.declineTerms()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.showTermsDialog)
        assertNull(state.pendingTokenData)
        assertNull(fakeAuthRepo.savedAccessToken)
        assertNotNull(state.error)
    }

    @Test
    fun backToLogin_resetsAuthModeToLogin() = runTest {
        viewModel.sendOtp("dev@coursedeals.com")
        advanceUntilIdle()
        assertEquals(AuthMode.VERIFY_OTP, viewModel.uiState.value.authMode)

        viewModel.backToLogin()
        advanceUntilIdle()
        assertEquals(AuthMode.LOGIN, viewModel.uiState.value.authMode)
    }

    private class FakeAuthRepository : UserAuthenticationRepository {
        var lastSentOtpEmail: String? = null
        var lastVerifiedEmail: String? = null
        var lastVerifiedOtp: String? = null
        var savedAccessToken: String? = null
        var savedRefreshToken: String? = null

        var sendOtpResult: AppResult<String> = AppResult.Success("OTP sent")
        var verifyOtpResult: AppResult<TokenData> = AppResult.Success(TokenData(accessToken = "mock-access"))

        override suspend fun sendOtp(email: String): AppResult<String> {
            lastSentOtpEmail = email
            return sendOtpResult
        }

        override suspend fun verifyOtp(email: String, otp: String): AppResult<TokenData> {
            lastVerifiedEmail = email
            lastVerifiedOtp = otp
            return verifyOtpResult
        }

        override suspend fun saveLocalToken(token: String) { savedAccessToken = token }
        override suspend fun saveLocalRefreshToken(refreshToken: String) { savedRefreshToken = refreshToken }
        override suspend fun getLocalToken(): String? = savedAccessToken
        override suspend fun clearLocalToken() { savedAccessToken = null }
        override suspend fun getLocalRefreshToken(): String? = savedRefreshToken
        override suspend fun clearLocalRefreshToken() { savedRefreshToken = null }

        override suspend fun login(credentials: AuthCredentials): AppResult<TokenData> =
            AppResult.Success(TokenData(accessToken = "mock-login"))

        override suspend fun register(email: String, password: String, fullName: String?): AppResult<String> =
            AppResult.Success("Registered")

        override suspend fun refreshToken(refreshToken: String): AppResult<TokenData> =
            AppResult.Success(TokenData(accessToken = "mock-refreshed"))

        override suspend fun checkIfTokenExpired(): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun getCipherTextWrapper(): String? = null
        override suspend fun saveCipherTextWrapper(fingerprintToken: String) {}
        override suspend fun clearCipherTextWrapper() {}
        override suspend fun requestFingerprintToken(): AppResult<TokenData> =
            AppResult.Success(TokenData(accessToken = "mock-fp"))

        override suspend fun requestAccessToken(): AppResult<TokenData> =
            AppResult.Success(TokenData(accessToken = "mock-access"))

        override suspend fun socialLogin(provider: String, idToken: String): AppResult<TokenData> =
            AppResult.Success(TokenData(accessToken = "mock-social"))
    }

    private class FakeUserProfileRepository : UserProfileRepository {
        override suspend fun getCachedUserProfile(): UserProfile = UserProfile()
        override suspend fun clearCachedUserProfile() {}
        override suspend fun saveEnableDarkMode(isDarkModeEnable: Boolean) {}
        override suspend fun getEnableDarkMode(): Boolean? = false
        override suspend fun saveEnableFingerPrint(isFingerPrintEnable: Boolean) {}
        override suspend fun getEnableFingerPrint(): Boolean? = false
        override suspend fun getFavoriteCategories(): Set<String> = emptySet()
        override suspend fun saveFavoriteCategories(categories: Set<String>): AppResult<UserPreferences> =
            AppResult.Success(UserPreferences())
        override suspend fun getFavoriteKeywords(): Set<String> = emptySet()
        override suspend fun saveFavoriteKeywords(keywords: Set<String>): AppResult<UserPreferences> =
            AppResult.Success(UserPreferences())
        override suspend fun getNotificationsEnabled(): Boolean = true
        override suspend fun saveNotificationsEnabled(enabled: Boolean): AppResult<UserPreferences> =
            AppResult.Success(UserPreferences())
        override suspend fun getUserProfile(): AppResult<UserProfile> = AppResult.Success(UserProfile())
        override suspend fun updateUserProfile(
            fullName: String?,
            avatarUrl: String?,
            fcmToken: String?
        ): AppResult<UserProfile> = AppResult.Success(UserProfile())
        override suspend fun syncPreferences(): AppResult<UserPreferences> = AppResult.Success(UserPreferences())
    }
}
