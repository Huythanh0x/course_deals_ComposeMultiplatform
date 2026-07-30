package com.thanh0x.coursedeals.ui.core.profile

data class ProfileUiState(
    val isDarkModeEnabled: Boolean = false,
    val isFingerprintEnabled: Boolean = false,
    val isTokenExpired: Boolean = false
)
