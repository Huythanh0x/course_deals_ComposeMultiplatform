package com.thanh0x.coursedeals.feature.profile

import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.core.ui.util.NetworkUtil
import com.thanh0x.coursedeals.domain.user.AuthCredentials
import com.thanh0x.coursedeals.domain.user.TokenData
import com.thanh0x.coursedeals.domain.user.UserAuthenticationRepository
import com.thanh0x.coursedeals.domain.user.UserProfile
import com.thanh0x.coursedeals.domain.user.UserProfileRepository
import com.thanh0x.coursedeals.domain.user.UserPreferences
import com.thanh0x.coursedeals.domain.user.usecase.CheckIfTokenExpiredUseCase
import com.thanh0x.coursedeals.domain.user.usecase.ClearLocalTokenUseCase
import com.thanh0x.coursedeals.domain.user.usecase.CryptographyManagerUseCase
import com.thanh0x.coursedeals.domain.user.usecase.DeleteAccountUseCase
import com.thanh0x.coursedeals.domain.user.usecase.GetLocalTokenUseCase
import com.thanh0x.coursedeals.domain.user.usecase.GetUserProfileUseCase
import com.thanh0x.coursedeals.domain.user.usecase.LoadCipherTextWrapperUseCase
import com.thanh0x.coursedeals.domain.user.usecase.RequestFingerprintTokenUseCase
import com.thanh0x.coursedeals.domain.user.usecase.SaveCipherTextWrapperUseCase
import com.thanh0x.coursedeals.domain.user.usecase.SaveJwtTokenUseCase
import com.thanh0x.coursedeals.domain.user.usecase.SettingFingerprintUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeUserProfileRepository: FakeUserProfileRepository
    private lateinit var fakeAuthRepository: FakeUserAuthenticationRepository
    private lateinit var fakeNetworkUtil: FakeNetworkUtil
    private lateinit var viewModel: ProfileViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeUserProfileRepository = FakeUserProfileRepository()
        fakeAuthRepository = FakeUserAuthenticationRepository()
        fakeNetworkUtil = FakeNetworkUtil()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(token: String?): ProfileViewModel {
        fakeAuthRepository.localToken = token
        val getLocalTokenUseCase = GetLocalTokenUseCase(fakeAuthRepository)
        val getUserProfileUseCase = GetUserProfileUseCase(fakeUserProfileRepository)
        val saveJwtTokenUseCase = SaveJwtTokenUseCase(fakeAuthRepository)
        val cryptographyManagerUseCase = CryptographyManagerUseCase()
        val settingFingerprintUseCase = SettingFingerprintUseCase(
            loadCipherTextWrapperUseCase = LoadCipherTextWrapperUseCase(fakeAuthRepository),
            saveCipherTextWrapperUseCase = SaveCipherTextWrapperUseCase(fakeAuthRepository),
        )
        val requestFingerprintTokenUseCase = RequestFingerprintTokenUseCase(fakeAuthRepository)
        val clearLocalTokenUseCase = ClearLocalTokenUseCase(fakeAuthRepository)
        val checkIfTokenExpiredUseCase = CheckIfTokenExpiredUseCase(fakeAuthRepository)
        val deleteAccountUseCase = DeleteAccountUseCase(fakeUserProfileRepository)

        return ProfileViewModel(
            userProfileRepository = fakeUserProfileRepository,
            getUserProfileUseCase = getUserProfileUseCase,
            saveJwtTokenUseCase = saveJwtTokenUseCase,
            cryptographyManagerUseCase = cryptographyManagerUseCase,
            settingFingerprintUseCase = settingFingerprintUseCase,
            requestFingerprintTokenUseCase = requestFingerprintTokenUseCase,
            clearLocalTokenUseCase = clearLocalTokenUseCase,
            checkIfTokenExpiredUseCase = checkIfTokenExpiredUseCase,
            getLocalTokenUseCase = getLocalTokenUseCase,
            deleteAccountUseCase = deleteAccountUseCase,
            networkUtil = fakeNetworkUtil,
            ioDispatcher = testDispatcher,
        )
    }

    @Test
    fun init_whenGuestMode_setsGuestFlags() = runTest {
        viewModel = createViewModel(token = null)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isGuestMode)
        assertEquals("", state.displayName)
        assertEquals("", state.email)
        assertFalse(state.isFingerprintEnabled)
    }

    @Test
    fun init_whenAuthenticated_populatesProfile() = runTest {
        fakeUserProfileRepository.cachedProfile = UserProfile(
            fullName = "Jane Doe",
            email = "jane@example.com",
            preferences = UserPreferences(categories = listOf("Design"), keywords = listOf("UI/UX"))
        )

        viewModel = createViewModel(token = "valid_token")
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isGuestMode)
        assertEquals("Jane Doe", state.displayName)
        assertEquals("jane@example.com", state.email)
    }

    @Test
    fun updateFavoriteCategories_updatesStateAndPersists() = runTest {
        viewModel = createViewModel(token = "valid_token")
        testScheduler.advanceUntilIdle()
        val newCats = listOf("Development", "Design")
        viewModel.updateFavoriteCategories(newCats)
        testScheduler.advanceUntilIdle()

        assertEquals(newCats, viewModel.uiState.value.favCategories)
        assertEquals(newCats.toSet(), fakeUserProfileRepository.savedCategories)
    }

    @Test
    fun addAndRemoveFavoriteKeyword_updatesStateAndPersists() = runTest {
        viewModel = createViewModel(token = "valid_token")
        testScheduler.advanceUntilIdle()
        viewModel.addFavoriteKeyword("Kotlin")
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.favKeywords.contains("Kotlin"))
        assertTrue(fakeUserProfileRepository.savedKeywords.contains("Kotlin"))

        viewModel.removeFavoriteKeyword("Kotlin")
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.favKeywords.contains("Kotlin"))
        assertFalse(fakeUserProfileRepository.savedKeywords.contains("Kotlin"))
    }

    @Test
    fun saveIsDarkModeEnable_updatesStateAndPersists() = runTest {
        viewModel = createViewModel(token = "valid_token")
        testScheduler.advanceUntilIdle()
        viewModel.saveIsDarkModeEnable(true)
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isDarkModeEnabled)
        assertEquals(true, fakeUserProfileRepository.savedDarkMode)
    }

    @Test
    fun deleteAccount_success_clearsSessionAndTransitionsToGuest() = runTest {
        fakeUserProfileRepository.cachedProfile = UserProfile(
            fullName = "John Doe",
            email = "john@example.com"
        )
        viewModel = createViewModel(token = "valid_token")
        testScheduler.advanceUntilIdle()

        viewModel.showDeleteAccountDialog()
        assertTrue(viewModel.uiState.value.isDeleteAccountDialogOpen)

        fakeUserProfileRepository.deleteAccountResult = AppResult.Success(Unit)
        viewModel.deleteAccount()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isDeletingAccount)
        assertFalse(state.isDeleteAccountDialogOpen)
        assertTrue(state.isGuestMode)
        assertEquals("", state.displayName)
        assertEquals("", state.email)
        assertNull(fakeAuthRepository.localToken)
    }

    @Test
    fun deleteAccount_failure_retainsStateAndSetsErrorMessage() = runTest {
        fakeUserProfileRepository.cachedProfile = UserProfile(
            fullName = "John Doe",
            email = "john@example.com"
        )
        viewModel = createViewModel(token = "valid_token")
        testScheduler.advanceUntilIdle()

        fakeUserProfileRepository.deleteAccountResult = AppResult.Error("Network error", 500)
        viewModel.deleteAccount()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isDeletingAccount)
        assertFalse(state.isGuestMode)
        assertEquals("John Doe", state.displayName)
        assertEquals("Network error", state.errorMessage)
        assertEquals("valid_token", fakeAuthRepository.localToken)
    }

    private class FakeUserAuthenticationRepository : UserAuthenticationRepository {
        var localToken: String? = null
        var cipherText: String? = null

        override suspend fun login(credentials: AuthCredentials): AppResult<TokenData> = AppResult.Error("Not configured")
        override suspend fun register(email: String, password: String, fullName: String?): AppResult<String> = AppResult.Error("Not configured")
        override suspend fun verifyOtp(email: String, otp: String): AppResult<TokenData> = AppResult.Error("Not configured")
        override suspend fun refreshToken(refreshToken: String): AppResult<TokenData> = AppResult.Error("Not configured")
        override suspend fun getLocalToken(): String? = localToken
        override suspend fun saveLocalToken(token: String) { localToken = token }
        override suspend fun clearLocalToken() { localToken = null }
        override suspend fun checkIfTokenExpired(): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun getCipherTextWrapper(): String? = cipherText
        override suspend fun saveCipherTextWrapper(fingerprintToken: String) { cipherText = fingerprintToken }
        override suspend fun clearCipherTextWrapper() { cipherText = null }
        override suspend fun getLocalRefreshToken(): String? = null
        override suspend fun saveLocalRefreshToken(refreshToken: String) {}
        override suspend fun clearLocalRefreshToken() {}
        override suspend fun requestFingerprintToken(): AppResult<TokenData> = AppResult.Error("Not configured")
        override suspend fun requestAccessToken(): AppResult<TokenData> = AppResult.Error("Not configured")
        override suspend fun socialLogin(provider: String, idToken: String): AppResult<TokenData> = AppResult.Error("Not configured")
    }

    private class FakeUserProfileRepository : UserProfileRepository {
        var cachedProfile: UserProfile = UserProfile()
        var savedCategories: Set<String> = emptySet()
        var savedKeywords: Set<String> = emptySet()
        var savedDarkMode: Boolean? = null
        var savedFingerprint: Boolean? = null
        var savedNotifications: Boolean = true

        override suspend fun getUserProfile(): AppResult<UserProfile> = AppResult.Success(cachedProfile)
        override suspend fun updateUserProfile(fullName: String?, avatarUrl: String?, fcmToken: String?): AppResult<UserProfile> = AppResult.Success(cachedProfile)
        override suspend fun getCachedUserProfile(): UserProfile = cachedProfile
        override suspend fun clearCachedUserProfile() { cachedProfile = UserProfile() }
        var deleteAccountResult: AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun deleteAccount(): AppResult<Unit> = deleteAccountResult
        override suspend fun syncPreferences(): AppResult<UserPreferences> = AppResult.Success(cachedProfile.preferences ?: UserPreferences())
        override suspend fun getFavoriteCategories(): Set<String> = savedCategories
        override suspend fun saveFavoriteCategories(categories: Set<String>): AppResult<UserPreferences> {
            savedCategories = categories
            return AppResult.Success(UserPreferences(categories = categories.toList()))
        }
        override suspend fun getFavoriteKeywords(): Set<String> = savedKeywords
        override suspend fun saveFavoriteKeywords(keywords: Set<String>): AppResult<UserPreferences> {
            savedKeywords = keywords
            return AppResult.Success(UserPreferences(keywords = keywords.toList()))
        }
        override suspend fun getEnableDarkMode(): Boolean? = savedDarkMode
        override suspend fun saveEnableDarkMode(isDarkModeEnable: Boolean) { savedDarkMode = isDarkModeEnable }
        override suspend fun getEnableFingerPrint(): Boolean? = savedFingerprint
        override suspend fun saveEnableFingerPrint(isFingerPrintEnable: Boolean) { savedFingerprint = isFingerPrintEnable }
        override suspend fun getNotificationsEnabled(): Boolean = savedNotifications
        override suspend fun saveNotificationsEnabled(enabled: Boolean): AppResult<UserPreferences> {
            savedNotifications = enabled
            return AppResult.Success(UserPreferences(notificationsEnabled = enabled))
        }
    }

    private class FakeNetworkUtil : NetworkUtil(null) {
        var isAvailable: Boolean = true
        override fun isInternetAvailable(): Boolean = isAvailable
    }
}
