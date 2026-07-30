package com.thanh0x.coursedeals.domain.model

data class TokenData(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String
)
