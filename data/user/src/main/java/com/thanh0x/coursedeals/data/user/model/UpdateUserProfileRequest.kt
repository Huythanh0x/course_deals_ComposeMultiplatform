package com.thanh0x.coursedeals.data.user.model

import com.google.gson.annotations.SerializedName

data class UpdateUserProfileRequest(
    @SerializedName("fullName")
    val fullName: String? = null,
    @SerializedName("avatarUrl")
    val avatarUrl: String? = null,
    @SerializedName("fcmToken")
    val fcmToken: String? = null,
)
