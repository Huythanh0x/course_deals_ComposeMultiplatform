package com.thanh0x.coursedeals.data.user.source.remote

import com.thanh0x.coursedeals.core.network.NetworkResponse
import com.thanh0x.coursedeals.data.user.model.PreferenceDto
import com.thanh0x.coursedeals.data.user.model.UpdateUserProfileRequest
import com.thanh0x.coursedeals.data.user.model.UserProfileDto

interface UserProfileService {
    suspend fun getPreferences(): NetworkResponse<PreferenceDto>

    suspend fun updatePreferences(preferences: PreferenceDto): NetworkResponse<PreferenceDto>

    suspend fun getUserProfile(): NetworkResponse<UserProfileDto>

    suspend fun updateUserProfile(request: UpdateUserProfileRequest): NetworkResponse<UserProfileDto>

    suspend fun deleteAccount(): NetworkResponse<Unit>
}
