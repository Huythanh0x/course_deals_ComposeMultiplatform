package com.thanh0x.coursedeals.domain.user.source

import kotlinx.coroutines.flow.Flow

interface LocalSettingsDataSource {
    fun getShowLocalFetchTime(): Flow<Boolean>
    suspend fun saveShowLocalFetchTime(show: Boolean)
}
