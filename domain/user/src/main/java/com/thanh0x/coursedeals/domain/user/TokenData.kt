package com.thanh0x.coursedeals.domain.user

data class TokenData(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String
)
