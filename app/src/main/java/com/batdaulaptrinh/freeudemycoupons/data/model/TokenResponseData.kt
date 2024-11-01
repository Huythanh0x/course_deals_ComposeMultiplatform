package com.batdaulaptrinh.freeudemycoupons.data.model

data class TokenResponseData(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String
)
