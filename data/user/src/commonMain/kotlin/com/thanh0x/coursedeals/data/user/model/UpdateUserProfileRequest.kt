package com.thanh0x.coursedeals.data.user.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserProfileRequest(
    @SerialName("fullName")
    val fullName: String? = null,
    @SerialName("avatarUrl")
    val avatarUrl: String? = null,
    @SerialName("fcmToken")
    val fcmToken: String? = null,
)
