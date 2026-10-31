package com.thanh0x.coursedeals.data.user.source.remote

import com.thanh0x.coursedeals.data.user.model.UpdateUserProfileRequest
import com.thanh0x.coursedeals.data.user.model.toDomain
import com.thanh0x.coursedeals.data.user.model.toDto
import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.domain.user.UserPreferences
import com.thanh0x.coursedeals.domain.user.UserProfile
import com.thanh0x.coursedeals.domain.user.source.RemoteUserProfileDataSource
import timber.log.Timber

class RemoteUserProfileDataSourceImpl(
    private val userProfileService: UserProfileService,
) : RemoteUserProfileDataSource {

    @Suppress("TooGenericExceptionCaught")
    override suspend fun getPreferences(): AppResult<UserPreferences> {
        return try {
            val response = userProfileService.getPreferences()
            val body = response.body()
            if ((response.isSuccessful) && (body != null)) {
                AppResult.Success(body.toDomain())
            } else {
                AppResult.Error(response.message(), response.code())
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching preferences")
            AppResult.Error(e.localizedMessage ?: "Unknown Error")
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun updatePreferences(preferences: UserPreferences): AppResult<UserPreferences> {
        return try {
            val response = userProfileService.updatePreferences(preferences.toDto())
            val body = response.body()
            if ((response.isSuccessful) && (body != null)) {
                AppResult.Success(body.toDomain())
            } else {
                AppResult.Error(response.message(), response.code())
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating preferences")
            AppResult.Error(e.localizedMessage ?: "Unknown Error")
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun getUserProfile(): AppResult<UserProfile> {
        return try {
            val response = userProfileService.getUserProfile()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                AppResult.Success(body.toDomain())
            } else {
                AppResult.Error(response.message(), response.code())
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching user profile")
            AppResult.Error(e.localizedMessage ?: "Unknown Error")
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun updateUserProfile(
        fullName: String?,
        avatarUrl: String?,
        fcmToken: String?,
    ): AppResult<UserProfile> {
        return try {
            val request = UpdateUserProfileRequest(
                fullName = fullName,
                avatarUrl = avatarUrl,
                fcmToken = fcmToken,
            )
            val response = userProfileService.updateUserProfile(request)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                AppResult.Success(body.toDomain())
            } else {
                AppResult.Error(response.message(), response.code())
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating user profile")
            AppResult.Error(e.localizedMessage ?: "Unknown Error")
        }
    }
}
