package com.thanh0x.coursedeals.data.user.repository

import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.domain.user.UserPreferences
import com.thanh0x.coursedeals.domain.user.UserProfile
import com.thanh0x.coursedeals.domain.user.source.LocalUserProfileDataSource
import com.thanh0x.coursedeals.domain.user.source.RemoteUserProfileDataSource
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UserProfileRepositoryImplTest {

    private lateinit var fakeLocalDataSource: FakeLocalUserProfileDataSource
    private lateinit var fakeRemoteDataSource: FakeRemoteUserProfileDataSource
    private lateinit var repository: UserProfileRepositoryImpl

    @Before
    fun setUp() {
        fakeLocalDataSource = FakeLocalUserProfileDataSource()
        fakeRemoteDataSource = FakeRemoteUserProfileDataSource()
        repository = UserProfileRepositoryImpl(fakeLocalDataSource, fakeRemoteDataSource)
    }

    @Test
    fun getUserProfile_whenRemoteSucceeds_cachesAndReturnsRemoteData() = runBlocking {
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
        val data = (result as AppResult.Success).data
        assertEquals("user@test.com", data.email)
        assertEquals("Test User", data.fullName)
        assertEquals("https://example.com/avatar.png", data.avatarUrl)

        // Verify cached locally
        assertEquals("Test User", fakeLocalDataSource.getUserName())
        assertEquals("user@test.com", fakeLocalDataSource.getUserEmail())
        assertEquals("https://example.com/avatar.png", fakeLocalDataSource.getUserAvatar())
    }

    @Test
    fun getUserProfile_whenRemoteFails_fallsBackToCachedData() = runBlocking {
        fakeLocalDataSource.saveUserName("Cached Name")
        fakeLocalDataSource.saveUserEmail("cached@test.com")
        fakeLocalDataSource.saveUserAvatar("https://example.com/cached.png")
        fakeRemoteDataSource.profileResult = AppResult.Error("Network error", 500)

        val result = repository.getUserProfile()

        assertTrue(result is AppResult.Success)
        val data = (result as AppResult.Success).data
        assertEquals("Cached Name", data.fullName)
        assertEquals("cached@test.com", data.email)
        assertEquals("https://example.com/cached.png", data.avatarUrl)
    }

    @Test
    fun updateUserProfile_cachesAndReturnsUpdatedData() = runBlocking {
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
        val data = (result as AppResult.Success).data
        assertEquals("Updated Name", data.fullName)
        assertEquals("Updated Name", fakeLocalDataSource.getUserName())
        assertEquals("https://example.com/new_avatar.png", fakeLocalDataSource.getUserAvatar())
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
    }
}
