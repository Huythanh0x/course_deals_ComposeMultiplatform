package com.thanh0x.coursedeals.ui.core.profile

data class ProfileUiState(
    val isDarkModeEnabled: Boolean = false,
    val isFingerprintEnabled: Boolean = false,
    val isTokenExpired: Boolean = false,
    val favCategories: List<String> = emptyList(),
    val favKeywords: List<String> = emptyList(),
    val isNotificationsEnabled: Boolean = true,
    val isSyncing: Boolean = false,
)
