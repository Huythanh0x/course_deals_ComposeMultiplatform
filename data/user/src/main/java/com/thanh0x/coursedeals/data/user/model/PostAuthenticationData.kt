package com.thanh0x.coursedeals.data.user.model

import com.google.gson.annotations.SerializedName

data class PostAuthenticationData(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("fcmToken")
    val fcmToken: String? = null
)
