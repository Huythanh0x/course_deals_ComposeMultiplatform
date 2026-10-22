package com.thanh0x.coursedeals.domain.user.usecase

import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.domain.user.UserPreferences
import com.thanh0x.coursedeals.domain.user.UserProfile
import com.thanh0x.coursedeals.domain.user.UserProfileRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetUserProfileUseCaseTest {

    @Test
    fun invoke_returnsProfileFromRepository() = runBlocking {
        val expectedProfile = UserProfile(
            id = 42,
            email = "tester@domain.com",
            fullName = "Tester Profile",
            avatarUrl = "https://domain.com/photo.png",
            preferences = UserPreferences(notificationsEnabled = true),
        )

        val fakeRepo = object : FakeUserProfileRepository() {
            override suspend fun getUserProfile(): AppResult<UserProfile> {
                return AppResult.Success(expectedProfile)
            }
        }

        val useCase = GetUserProfileUseCase(fakeRepo)
        val result = useCase()

        assertTrue(result is AppResult.Success)
        assertEquals(expectedProfile, (result as AppResult.Success).data)
    }

    private open class FakeUserProfileRepository : UserProfileRepository {
        override suspend fun getEnableDarkMode(): Boolean? = false
        override suspend fun saveEnableDarkMode(isDarkModeEnable: Boolean) {}
        override suspend fun getEnableFingerPrint(): Boolean? = false
        override suspend fun saveEnableFingerPrint(isFingerPrintEnable: Boolean) {}
        override suspend fun syncPreferences(): AppResult<UserPreferences> = AppResult.Success(UserPreferences())
        override suspend fun getFavoriteCategories(): Set<String> = emptySet()
        override suspend fun saveFavoriteCategories(categories: Set<String>): AppResult<UserPreferences> = AppResult.Success(UserPreferences())
        override suspend fun getFavoriteKeywords(): Set<String> = emptySet()
        override suspend fun saveFavoriteKeywords(keywords: Set<String>): AppResult<UserPreferences> = AppResult.Success(UserPreferences())
        override suspend fun getNotificationsEnabled(): Boolean = true
        override suspend fun saveNotificationsEnabled(enabled: Boolean): AppResult<UserPreferences> = AppResult.Success(UserPreferences())
        override suspend fun getUserProfile(): AppResult<UserProfile> = AppResult.Error("Not implemented")
        override suspend fun updateUserProfile(fullName: String?, avatarUrl: String?, fcmToken: String?): AppResult<UserProfile> = AppResult.Error("Not implemented")
        override suspend fun getCachedUserProfile(): UserProfile = UserProfile()
    }
}
