package com.thanh0x.coursedeals.domain.source

import kotlinx.coroutines.flow.Flow

interface LocalSettingsDataSource {
    fun getShowLocalFetchTime(): Flow<Boolean>
    suspend fun saveShowLocalFetchTime(show: Boolean)
}
