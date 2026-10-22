package com.thanh0x.coursedeals.data.user.source.remote

import com.thanh0x.coursedeals.data.user.model.PreferenceDto
import com.thanh0x.coursedeals.data.user.model.UpdateUserProfileRequest
import com.thanh0x.coursedeals.data.user.model.UserProfileDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface UserProfileService {
    @GET("api/v1/preferences")
    suspend fun getPreferences(): Response<PreferenceDto>

    @PUT("api/v1/preferences")
    suspend fun updatePreferences(@Body preferences: PreferenceDto): Response<PreferenceDto>

    @GET("api/v1/user/profile")
    suspend fun getUserProfile(): Response<UserProfileDto>

    @PUT("api/v1/user/profile")
    suspend fun updateUserProfile(@Body request: UpdateUserProfileRequest): Response<UserProfileDto>
}
