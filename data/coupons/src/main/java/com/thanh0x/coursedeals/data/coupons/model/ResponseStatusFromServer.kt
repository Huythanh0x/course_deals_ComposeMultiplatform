package com.thanh0x.coursedeals.data.coupons.model

import com.google.gson.annotations.SerializedName

data class ResponseStatusFromServer(
    @SerializedName("status")
    val status: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("route")
    val route: String
)
