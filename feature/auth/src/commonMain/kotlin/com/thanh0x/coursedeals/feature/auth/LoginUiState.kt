package com.thanh0x.coursedeals.feature.auth

import com.thanh0x.coursedeals.core.common.Constant
import com.thanh0x.coursedeals.domain.user.TokenData

enum class AuthMode {
    LOGIN,
    REGISTER,
    VERIFY_OTP
}

data class LoginUiState(
    val isLoading: Boolean = false,
    val isFingerprintEnabled: Boolean = false,
    val isFingerprintFeatureEnabled: Boolean = Constant.FEATURE_FINGERPRINT,
    val authMode: AuthMode = AuthMode.LOGIN,
    val pendingEmail: String = "",
    val pendingTokenData: TokenData? = null,
    val showTermsDialog: Boolean = false,
    val infoMessage: String? = null,
    val error: String? = null
)
