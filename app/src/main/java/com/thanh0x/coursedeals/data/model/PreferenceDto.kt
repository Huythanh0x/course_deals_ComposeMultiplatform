package com.thanh0x.coursedeals.data.model

import com.google.gson.annotations.SerializedName
import com.thanh0x.coursedeals.domain.model.UserPreferences

data class PreferenceDto(
    @SerializedName("categories")
    val categories: List<String>,
    @SerializedName("keywords")
    val keywords: List<String>,
    @SerializedName("notificationsEnabled")
    val notificationsEnabled: Boolean,
    @SerializedName("updatedAt")
    val updatedAt: Long,
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
