package com.thanh0x.coursedeals.data.user.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PostAuthenticationData(
    @SerialName("email")
    val email: String,
    @SerialName("password")
    val password: String,
    @SerialName("fcmToken")
    val fcmToken: String? = null
)
