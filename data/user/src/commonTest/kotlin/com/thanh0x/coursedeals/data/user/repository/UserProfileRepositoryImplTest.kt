package com.thanh0x.coursedeals.data.user.repository

import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.domain.user.UserPreferences
import com.thanh0x.coursedeals.domain.user.UserProfile
import com.thanh0x.coursedeals.domain.user.source.LocalAuthenticationDataSource
import com.thanh0x.coursedeals.domain.user.source.LocalUserProfileDataSource
import com.thanh0x.coursedeals.domain.user.source.RemoteUserProfileDataSource
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UserProfileRepositoryImplTest {

    private lateinit var fakeLocalDataSource: FakeLocalUserProfileDataSource
    private lateinit var fakeRemoteDataSource: FakeRemoteUserProfileDataSource
    private lateinit var fakeAuthDataSource: FakeLocalAuthenticationDataSource
    private lateinit var repository: UserProfileRepositoryImpl

    @BeforeTest
    fun setUp() {
        fakeLocalDataSource = FakeLocalUserProfileDataSource()
        fakeRemoteDataSource = FakeRemoteUserProfileDataSource()
        fakeAuthDataSource = FakeLocalAuthenticationDataSource()
        repository = UserProfileRepositoryImpl(
            fakeLocalDataSource,
            fakeRemoteDataSource,
            fakeAuthDataSource
        )
    }

    @Test
    fun getUserProfile_whenRemoteSucceeds_cachesAndReturnsRemoteData() = runTest {
        val remoteProfile = UserProfile(
            id = 1,
            email = "user@test.com",
            fullName = "Test User",
            avatarUrl = "https://example.com/avatar.png",
            preferences = UserPreferences(notificationsEnabled = true),
        )
        fakeRemoteDataSource.profileResult = AppResult.Success(remoteProfile)

        val result = repository.getUserProfile()

        assertTrue(result is AppResult.Success)
        val data = result.data
        assertEquals("user@test.com", data.email)
        assertEquals("Test User", data.fullName)
        assertEquals("https://example.com/avatar.png", data.avatarUrl)

        assertEquals("Test User", fakeLocalDataSource.getUserName())
        assertEquals("user@test.com", fakeLocalDataSource.getUserEmail())
        assertEquals("https://example.com/avatar.png", fakeLocalDataSource.getUserAvatar())
    }

    @Test
    fun getUserProfile_whenRemoteFails_fallsBackToCachedData() = runTest {
        fakeLocalDataSource.saveUserName("Cached Name")
        fakeLocalDataSource.saveUserEmail("cached@test.com")
        fakeLocalDataSource.saveUserAvatar("https://example.com/cached.png")
        fakeRemoteDataSource.profileResult = AppResult.Error("Network error", 500)

        val result = repository.getUserProfile()

        assertTrue(result is AppResult.Success)
        val data = result.data
        assertEquals("Cached Name", data.fullName)
        assertEquals("cached@test.com", data.email)
        assertEquals("https://example.com/cached.png", data.avatarUrl)
    }

    @Test
    fun updateUserProfile_cachesAndReturnsUpdatedData() = runTest {
        val updatedProfile = UserProfile(
            id = 1,
            email = "user@test.com",
            fullName = "Updated Name",
            avatarUrl = "https://example.com/new_avatar.png",
        )
        fakeRemoteDataSource.updateResult = AppResult.Success(updatedProfile)

        val result = repository.updateUserProfile(
            fullName = "Updated Name",
            avatarUrl = "https://example.com/new_avatar.png",
        )

        assertTrue(result is AppResult.Success)
        val data = result.data
        assertEquals("Updated Name", data.fullName)
        assertEquals("Updated Name", fakeLocalDataSource.getUserName())
        assertEquals("https://example.com/new_avatar.png", fakeLocalDataSource.getUserAvatar())
    }

    @Test
    fun clearCachedUserProfile_clearsLocalDataSourceUserProfile() = runTest {
        fakeLocalDataSource.saveUserName("Cached Name")
        fakeLocalDataSource.saveUserEmail("cached@test.com")
        fakeLocalDataSource.saveUserAvatar("https://example.com/avatar.png")

        repository.clearCachedUserProfile()

        assertNull(fakeLocalDataSource.getUserName())
        assertNull(fakeLocalDataSource.getUserEmail())
        assertNull(fakeLocalDataSource.getUserAvatar())
    }

    @Test
    fun deleteAccount_whenSuccess_clearsLocalTokensAndProfileData() = runTest {
        fakeAuthDataSource.saveLocalToken("token-123")
        fakeAuthDataSource.saveLocalRefreshToken("refresh-123")
        fakeAuthDataSource.saveCipherTextWrapper("cipher-123")
        fakeLocalDataSource.saveUserName("Cached Name")
        fakeLocalDataSource.saveUserEmail("cached@test.com")
        fakeRemoteDataSource.deleteAccountResult = AppResult.Success(Unit)

        val result = repository.deleteAccount()

        assertTrue(result is AppResult.Success)
        assertNull(fakeAuthDataSource.getLocalToken())
        assertNull(fakeAuthDataSource.getLocalRefreshToken())
        assertNull(fakeAuthDataSource.getCipherTextWrapper())
        assertNull(fakeLocalDataSource.getUserName())
        assertNull(fakeLocalDataSource.getUserEmail())
    }

    @Test
    fun deleteAccount_whenError_doesNotClearLocalTokens() = runTest {
        fakeAuthDataSource.saveLocalToken("token-123")
        fakeAuthDataSource.saveLocalRefreshToken("refresh-123")
        fakeRemoteDataSource.deleteAccountResult = AppResult.Error("Failed to delete", 500)

        val result = repository.deleteAccount()

        assertTrue(result is AppResult.Error)
        assertEquals("token-123", fakeAuthDataSource.getLocalToken())
        assertEquals("refresh-123", fakeAuthDataSource.getLocalRefreshToken())
    }

    private class FakeLocalUserProfileDataSource : LocalUserProfileDataSource {
        private var darkMode: Boolean? = null
        private var fingerprint: Boolean? = null
        private var categories = mutableSetOf<String>()
        private var keywords = mutableSetOf<String>()
        private var notifs = true
        private var updatedAt = 0L
        private var userName: String? = null
        private var userEmail: String? = null
        private var userAvatar: String? = null

        override suspend fun getEnableDarkMode() = darkMode
        override suspend fun saveEnableDarkMode(isDarkModeEnable: Boolean) { darkMode = isDarkModeEnable }
        override suspend fun getEnableFingerPrint() = fingerprint
        override suspend fun saveEnableFingerPrint(isFingerPrintEnable: Boolean) { fingerprint = isFingerPrintEnable }
        override suspend fun getFavoriteCategories() = categories
        override suspend fun saveFavoriteCategories(categories: Set<String>) { this.categories = categories.toMutableSet() }
        override suspend fun getFavoriteKeywords() = keywords
        override suspend fun saveFavoriteKeywords(keywords: Set<String>) { this.keywords = keywords.toMutableSet() }
        override suspend fun getNotificationsEnabled() = notifs
        override suspend fun saveNotificationsEnabled(enabled: Boolean) { notifs = enabled }
        override suspend fun getPreferencesUpdatedAt() = updatedAt
        override suspend fun savePreferencesUpdatedAt(timestamp: Long) { updatedAt = timestamp }
        override suspend fun getUserName() = userName
        override suspend fun saveUserName(name: String?) { userName = name }
        override suspend fun getUserEmail() = userEmail
        override suspend fun saveUserEmail(email: String?) { userEmail = email }
        override suspend fun getUserAvatar() = userAvatar
        override suspend fun saveUserAvatar(avatarUrl: String?) { userAvatar = avatarUrl }
        override suspend fun clearUserProfile() {
            userName = null
            userEmail = null
            userAvatar = null
        }
    }

    private class FakeRemoteUserProfileDataSource : RemoteUserProfileDataSource {
        var profileResult: AppResult<UserProfile> = AppResult.Error("Not configured")
        var updateResult: AppResult<UserProfile> = AppResult.Error("Not configured")

        override suspend fun getPreferences(): AppResult<UserPreferences> =
            AppResult.Success(UserPreferences())

        override suspend fun updatePreferences(preferences: UserPreferences): AppResult<UserPreferences> =
            AppResult.Success(preferences)

        override suspend fun getUserProfile(): AppResult<UserProfile> = profileResult

        override suspend fun updateUserProfile(
            fullName: String?,
            avatarUrl: String?,
            fcmToken: String?,
        ): AppResult<UserProfile> = updateResult

        var deleteAccountResult: AppResult<Unit> = AppResult.Error("Not configured")

        override suspend fun deleteAccount(): AppResult<Unit> = deleteAccountResult
    }

    private class FakeLocalAuthenticationDataSource : LocalAuthenticationDataSource {
        private var token: String? = null
        private var refreshToken: String? = null
        private var cipherText: String? = null

        override suspend fun getLocalToken(): String? = token
        override suspend fun saveLocalToken(newLoginToken: String) { token = newLoginToken }
        override suspend fun clearLocalToken() { token = null }
        override suspend fun getCipherTextWrapper(): String? = cipherText
        override suspend fun saveCipherTextWrapper(newLoginToken: String) { cipherText = newLoginToken }
        override suspend fun clearCipherTextWrapper() { cipherText = null }
        override suspend fun getLocalRefreshToken(): String? = refreshToken
        override suspend fun saveLocalRefreshToken(newLoginToken: String) { refreshToken = newLoginToken }
        override suspend fun clearLocalRefreshToken() { refreshToken = null }
    }
}
