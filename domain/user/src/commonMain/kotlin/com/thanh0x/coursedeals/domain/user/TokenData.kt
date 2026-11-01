package com.thanh0x.coursedeals.domain.user

data class TokenData(
    val accessToken: String,
    val refreshToken: String? = null,
    val tokenType: String? = null,
    val isNewUser: Boolean = false
)
