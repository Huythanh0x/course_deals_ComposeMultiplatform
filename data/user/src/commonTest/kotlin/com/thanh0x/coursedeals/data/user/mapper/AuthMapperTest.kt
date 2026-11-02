package com.thanh0x.coursedeals.data.user.mapper

import com.thanh0x.coursedeals.core.network.openapi.model.AuthResponseDTO
import com.thanh0x.coursedeals.core.network.openapi.model.PreferenceDTO
import com.thanh0x.coursedeals.core.network.openapi.model.SocialLoginRequestDTO
import com.thanh0x.coursedeals.core.network.openapi.model.UserProfileDTO
import com.thanh0x.coursedeals.domain.user.AuthCredentials
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthMapperTest {

    @Test
    fun authResponseDto_mapsToDomainTokenData() {
        val dto = AuthResponseDTO(
            expiresIn = "3600",
            isNewUser = true,
            accessToken = "jwt-access",
            refreshToken = "jwt-refresh",
            tokenType = "Bearer",
        )

        val domain = dto.toDomain()
        assertEquals("jwt-access", domain.accessToken)
        assertEquals("jwt-refresh", domain.refreshToken)
        assertEquals("Bearer", domain.tokenType)
        assertTrue(domain.isNewUser)
    }

    @Test
    fun authCredentials_mapsToEmailLoginRequestDto() {
        val credentials = AuthCredentials(
            username = "dev@coursedeals.com",
            password = "secret-password",
        )

        val dto = credentials.toDto()
        assertEquals("dev@coursedeals.com", dto.email)
        assertEquals("secret-password", dto.password)
    }

    @Test
    fun socialProviderString_mapsToGeneratedEnumCaseInsensitively() {
        assertEquals(SocialLoginRequestDTO.Provider.GOOGLE, "google".toSocialProvider())
        assertEquals(SocialLoginRequestDTO.Provider.APPLE, "APPLE".toSocialProvider())
        assertEquals(SocialLoginRequestDTO.Provider.PASSKEY, "Passkey".toSocialProvider())
        assertEquals(SocialLoginRequestDTO.Provider.GOOGLE, "unknown".toSocialProvider())
    }

    @Test
    fun preferenceAndUserProfileDto_roundTripWithDomainModels() {
        val prefDto = PreferenceDTO(
            categories = setOf("Development", "Design"),
            keywords = setOf("Kotlin", "KMP"),
            notificationsEnabled = true,
        )
        val profileDto = UserProfileDTO(
            id = 42,
            email = "user@coursedeals.com",
            fullName = "Course Deals User",
            avatarUrl = "https://example.com/avatar.png",
            fcmToken = "fcm-1",
            preferences = prefDto,
        )

        val domainProfile = profileDto.toDomain()
        assertEquals(42, domainProfile.id)
        assertEquals(listOf("Development", "Design"), domainProfile.preferences?.categories)
        assertEquals(listOf("Kotlin", "KMP"), domainProfile.preferences?.keywords)

        val roundTripDto = domainProfile.toDto()
        assertEquals(setOf("Development", "Design"), roundTripDto.preferences?.categories)
        assertEquals(setOf("Kotlin", "KMP"), roundTripDto.preferences?.keywords)
    }
}
