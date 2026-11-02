package com.thanh0x.coursedeals.data.user.source.remote

import com.thanh0x.coursedeals.core.network.NetworkResponse
import com.thanh0x.coursedeals.core.network.openapi.model.PreferenceDTO
import com.thanh0x.coursedeals.core.network.openapi.model.UpdateUserProfileRequestDTO
import com.thanh0x.coursedeals.core.network.openapi.model.UserProfileDTO

interface UserProfileService {
    suspend fun getPreferences(): NetworkResponse<PreferenceDTO>

    suspend fun updatePreferences(preferences: PreferenceDTO): NetworkResponse<PreferenceDTO>

    suspend fun getUserProfile(): NetworkResponse<UserProfileDTO>

    suspend fun updateUserProfile(request: UpdateUserProfileRequestDTO): NetworkResponse<UserProfileDTO>

    suspend fun deleteAccount(): NetworkResponse<Unit>

    suspend fun acceptTerms(): NetworkResponse<Unit>
}
