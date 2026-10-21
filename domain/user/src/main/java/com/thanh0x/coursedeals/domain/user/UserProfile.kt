package com.thanh0x.coursedeals.domain.user

data class UserProfile(
    val id: Int = 0,
    val email: String? = null,
    val fullName: String? = null,
    val avatarUrl: String? = null,
    val fcmToken: String? = null,
    val preferences: UserPreferences? = null,
)
