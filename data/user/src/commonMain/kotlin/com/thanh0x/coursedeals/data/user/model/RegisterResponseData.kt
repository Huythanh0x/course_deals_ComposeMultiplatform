package com.thanh0x.coursedeals.data.user.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterResponseData(
    @SerialName("message")
    val message: String? = null,
    @SerialName("email")
    val email: String? = null,
    @SerialName("expiresInSeconds")
    val expiresInSeconds: Long? = null
)
