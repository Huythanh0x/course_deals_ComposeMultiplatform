package com.thanh0x.coursedeals.data.source.remote

import com.thanh0x.coursedeals.data.model.toDomain
import com.thanh0x.coursedeals.data.model.toDto
import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.domain.user.UserPreferences
import com.thanh0x.coursedeals.domain.source.RemoteUserProfileDataSource
import timber.log.Timber
import javax.inject.Inject

class RemoteUserProfileDataSourceImpl @Inject constructor(
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
}
