package com.thanh0x.coursedeals.data.model

data class TokenResponseData(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String
)
