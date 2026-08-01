package com.thanh0x.coursedeals.domain.model

data class UserPreferences(
    val categories: List<String> = emptyList(),
    val keywords: List<String> = emptyList(),
    val notificationsEnabled: Boolean = true,
    val updatedAt: Long = 0,
)
