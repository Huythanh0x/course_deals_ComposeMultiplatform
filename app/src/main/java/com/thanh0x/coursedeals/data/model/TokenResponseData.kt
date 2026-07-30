package com.thanh0x.coursedeals.data.model

import com.google.gson.annotations.SerializedName

data class TokenResponseData(
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("refreshToken")
    val refreshToken: String,
    @SerializedName("tokenType")
    val tokenType: String
)
