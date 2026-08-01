package com.thanh0x.coursedeals.domain.source

import com.thanh0x.coursedeals.domain.model.AppResult
import com.thanh0x.coursedeals.domain.model.UserPreferences

interface RemoteUserProfileDataSource {
    suspend fun getPreferences(): AppResult<UserPreferences>
    suspend fun updatePreferences(preferences: UserPreferences): AppResult<UserPreferences>
}
