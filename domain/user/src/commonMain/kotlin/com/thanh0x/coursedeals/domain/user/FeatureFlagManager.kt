package com.thanh0x.coursedeals.domain.user

import com.thanh0x.coursedeals.domain.user.model.AppConfig
import kotlinx.coroutines.flow.StateFlow

interface FeatureFlagManager {
    val configFlow: StateFlow<AppConfig>
    suspend fun refresh(): Result<AppConfig>
    fun boolean(key: String, default: Boolean): Boolean
    fun double(key: String, default: Double): Double
    fun string(key: String, default: String): String
}
