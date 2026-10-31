package com.thanh0x.coursedeals.data.user.model

import com.thanh0x.coursedeals.domain.user.UserProfile
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfileDto(
    @SerialName("id")
    val id: Int = 0,
    @SerialName("email")
    val email: String? = null,
    @SerialName("fullName")
    val fullName: String? = null,
    @SerialName("avatarUrl")
    val avatarUrl: String? = null,
    @SerialName("fcmToken")
    val fcmToken: String? = null,
    @SerialName("preferences")
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
