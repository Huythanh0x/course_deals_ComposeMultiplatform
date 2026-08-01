package com.thanh0x.coursedeals.data.user.model

import com.google.gson.annotations.SerializedName

data class SocialLoginRequest(
    @SerializedName("idToken")
    val idToken: String,
    @SerializedName("provider")
    val provider: String,
    @SerializedName("fcmToken")
    val fcmToken: String? = null
)
