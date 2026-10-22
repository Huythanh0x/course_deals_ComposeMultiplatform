package com.thanh0x.coursedeals.feature.profile

data class ProfileUiState(
    val displayName: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val isLoading: Boolean = false,
    val isDarkModeEnabled: Boolean = false,
    val isFingerprintEnabled: Boolean = false,
    val isTokenExpired: Boolean = false,
    val favCategories: List<String> = emptyList(),
    val favKeywords: List<String> = emptyList(),
    val isNotificationsEnabled: Boolean = true,
    val isSyncing: Boolean = false,
    val errorMessage: String? = null,
)
