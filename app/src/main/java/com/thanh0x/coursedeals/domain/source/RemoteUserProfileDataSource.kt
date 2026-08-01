package com.thanh0x.coursedeals.domain.source

import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.domain.user.UserPreferences

interface RemoteUserProfileDataSource {
    suspend fun getPreferences(): AppResult<UserPreferences>
    suspend fun updatePreferences(preferences: UserPreferences): AppResult<UserPreferences>
}
