package com.thanh0x.coursedeals.data.user.model

import com.google.gson.annotations.SerializedName

data class VerifyOtpRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("otp")
    val otp: String,
    @SerializedName("fcmToken")
    val fcmToken: String? = null
)
