package com.thanh0x.coursedeals.data.user.model

import com.thanh0x.coursedeals.domain.user.UserPreferences
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PreferenceDto(
    @SerialName("categories")
    val categories: List<String> = emptyList(),
    @SerialName("keywords")
    val keywords: List<String> = emptyList(),
    @SerialName("notificationsEnabled")
    val notificationsEnabled: Boolean = true,
    @SerialName("updatedAt")
    val updatedAt: Long = 0L,
)

fun PreferenceDto.toDomain() = UserPreferences(
    categories = categories,
    keywords = keywords,
    notificationsEnabled = notificationsEnabled,
    updatedAt = updatedAt,
)

fun UserPreferences.toDto() = PreferenceDto(
    categories = categories,
    keywords = keywords,
    notificationsEnabled = notificationsEnabled,
    updatedAt = updatedAt,
)
