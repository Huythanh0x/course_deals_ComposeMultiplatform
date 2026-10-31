package com.thanh0x.coursedeals.data.coupons.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseStatusFromServer(
    @SerialName("status")
    val status: Int,
    @SerialName("message")
    val message: String,
    @SerialName("route")
    val route: String
)
