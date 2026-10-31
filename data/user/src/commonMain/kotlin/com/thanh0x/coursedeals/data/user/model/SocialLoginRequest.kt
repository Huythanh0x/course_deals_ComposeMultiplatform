package com.thanh0x.coursedeals.data.user.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SocialLoginRequest(
    @SerialName("idToken")
    val idToken: String,
    @SerialName("provider")
    val provider: String,
    @SerialName("fcmToken")
    val fcmToken: String? = null
)
