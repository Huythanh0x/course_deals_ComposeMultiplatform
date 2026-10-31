package com.thanh0x.coursedeals.domain.user.source

import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.domain.user.UserPreferences
import com.thanh0x.coursedeals.domain.user.UserProfile

interface RemoteUserProfileDataSource {
    suspend fun getPreferences(): AppResult<UserPreferences>
    suspend fun updatePreferences(preferences: UserPreferences): AppResult<UserPreferences>
    suspend fun getUserProfile(): AppResult<UserProfile>
    suspend fun updateUserProfile(fullName: String?, avatarUrl: String?, fcmToken: String?): AppResult<UserProfile>
}
