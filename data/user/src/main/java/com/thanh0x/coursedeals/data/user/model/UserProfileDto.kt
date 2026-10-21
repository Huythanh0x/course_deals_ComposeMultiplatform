package com.thanh0x.coursedeals.data.user.model

import com.google.gson.annotations.SerializedName
import com.thanh0x.coursedeals.domain.user.UserProfile

data class UserProfileDto(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("fullName")
    val fullName: String? = null,
    @SerializedName("avatarUrl")
    val avatarUrl: String? = null,
    @SerializedName("fcmToken")
    val fcmToken: String? = null,
    @SerializedName("preferences")
    val preferences: PreferenceDto? = null,
)

fun UserProfileDto.toDomain() = UserProfile(
    id = id,
    email = email,
    fullName = fullName,
    avatarUrl = avatarUrl,
    fcmToken = fcmToken,
    preferences = preferences?.toDomain(),
)

fun UserProfile.toDto() = UserProfileDto(
    id = id,
    email = email,
    fullName = fullName,
    avatarUrl = avatarUrl,
    fcmToken = fcmToken,
    preferences = preferences?.toDto(),
)
