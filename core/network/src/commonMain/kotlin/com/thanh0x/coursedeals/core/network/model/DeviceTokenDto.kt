package com.thanh0x.coursedeals.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class DeviceTokenRequest(
    val fcmToken: String,
    val platform: String,
)

@Serializable
data class DeviceTokenResponse(
    val fcmToken: String,
    val platform: String,
    val userId: Long? = null,
    val registered: Boolean = true,
)
