package com.thanh0x.coursedeals.data.user.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerifyOtpRequest(
    @SerialName("email")
    val email: String,
    @SerialName("otp")
    val otp: String,
    @SerialName("fcmToken")
    val fcmToken: String? = null
)
