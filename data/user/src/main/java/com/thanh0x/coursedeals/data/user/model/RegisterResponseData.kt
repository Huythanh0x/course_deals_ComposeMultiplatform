package com.thanh0x.coursedeals.data.user.model

import com.google.gson.annotations.SerializedName

data class RegisterResponseData(
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("expiresInSeconds")
    val expiresInSeconds: Long? = null
)
