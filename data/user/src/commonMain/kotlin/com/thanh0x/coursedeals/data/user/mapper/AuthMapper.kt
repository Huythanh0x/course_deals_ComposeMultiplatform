package com.thanh0x.coursedeals.data.user.mapper

import com.thanh0x.coursedeals.core.network.openapi.model.AuthResponseDTO
import com.thanh0x.coursedeals.core.network.openapi.model.EmailLoginRequestDTO
import com.thanh0x.coursedeals.core.network.openapi.model.PreferenceDTO
import com.thanh0x.coursedeals.core.network.openapi.model.SocialLoginRequestDTO
import com.thanh0x.coursedeals.core.network.openapi.model.UserProfileDTO
import com.thanh0x.coursedeals.domain.user.AuthCredentials
import com.thanh0x.coursedeals.domain.user.TokenData
import com.thanh0x.coursedeals.domain.user.UserPreferences
import com.thanh0x.coursedeals.domain.user.UserProfile

fun AuthResponseDTO.toDomain(): TokenData {
    return TokenData(
        accessToken = accessToken.orEmpty(),
        refreshToken = refreshToken,
        tokenType = tokenType,
        isNewUser = isNewUser
    )
}

fun AuthCredentials.toDto(): EmailLoginRequestDTO {
    return EmailLoginRequestDTO(
        email = username,
        password = password
    )
}

fun String.toSocialProvider(): SocialLoginRequestDTO.Provider {
    return SocialLoginRequestDTO.Provider.entries.firstOrNull {
        it.value.equals(this, ignoreCase = true) || it.name.equals(this, ignoreCase = true)
    } ?: SocialLoginRequestDTO.Provider.GOOGLE
}

fun PreferenceDTO.toDomain() = UserPreferences(
    categories = categories.toList(),
    keywords = keywords.toList(),
    notificationsEnabled = notificationsEnabled,
)

fun UserPreferences.toDto() = PreferenceDTO(
    categories = categories.toSet(),
    keywords = keywords.toSet(),
    notificationsEnabled = notificationsEnabled,
)

fun UserProfileDTO.toDomain() = UserProfile(
    id = id,
    email = email,
    fullName = fullName,
    avatarUrl = avatarUrl,
    fcmToken = fcmToken,
    preferences = preferences?.toDomain(),
)

fun UserProfile.toDto() = UserProfileDTO(
    id = id,
    email = email,
    fullName = fullName,
    avatarUrl = avatarUrl,
    fcmToken = fcmToken,
    preferences = preferences?.toDto(),
)
