package com.thanh0x.coursedeals.data.user.model

import com.google.gson.annotations.SerializedName

data class TokenResponseData(
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("refreshToken")
    val refreshToken: String? = null,
    @SerializedName("tokenType")
    val tokenType: String? = null,
    @SerializedName("expiresIn")
    val expiresIn: Long? = null
)
