package com.thanh0x.coursedeals.feature.auth

data class LoginUiState(
    val isLoading: Boolean = false,
    val isFingerprintEnabled: Boolean = false,
    val error: String? = null
)
