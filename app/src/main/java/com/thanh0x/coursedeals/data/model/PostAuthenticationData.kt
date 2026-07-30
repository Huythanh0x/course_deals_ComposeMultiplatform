package com.thanh0x.coursedeals.data.model

import com.google.gson.annotations.SerializedName

data class PostAuthenticationData(
    @SerializedName("username")
    val username: String,
    @SerializedName("password")
    val password: String
)
