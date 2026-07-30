package com.thanh0x.coursedeals.ui.login

data class LoginUiState(
    val isLoading: Boolean = false,
    val isFingerprintEnabled: Boolean = false,
    val error: String? = null
)
