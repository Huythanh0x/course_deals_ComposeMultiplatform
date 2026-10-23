package com.thanh0x.coursedeals.feature.auth

enum class AuthMode {
    LOGIN,
    REGISTER,
    VERIFY_OTP
}

data class LoginUiState(
    val isLoading: Boolean = false,
    val isFingerprintEnabled: Boolean = false,
    val authMode: AuthMode = AuthMode.LOGIN,
    val pendingEmail: String = "",
    val infoMessage: String? = null,
    val error: String? = null
)
