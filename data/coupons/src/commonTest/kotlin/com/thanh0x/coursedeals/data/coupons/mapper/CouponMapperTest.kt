package com.thanh0x.coursedeals.data.coupons.mapper

import com.thanh0x.coursedeals.core.network.openapi.model.CouponDetailDTO
import com.thanh0x.coursedeals.core.network.openapi.model.CouponSummaryDTO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CouponMapperTest {

    @Test
    fun couponDetailDto_mapsToDomainAndEntityCorrectly() {
        val dto = CouponDetailDTO(
            courseId = 1001,
            contentLength = 180,
            isNew = true,
            rating = 4.5f,
            reviews = 320,
            students = 12000,
            usesRemaining = 42,
            author = "Jane Doe",
            category = "Development",
            couponCode = "KMP2026",
            couponUrl = "https://www.udemy.com/course/kmp/?couponCode=KMP2026",
            createdAt = 1785660439L,
            description = "Full KMP course",
            expiredTime = 1785810480L,
            heading = "Build multiplatform apps",
            language = "English",
            level = CouponDetailDTO.Level.INTERMEDIATE,
            previewImage = "https://example.com/preview.jpg",
            previewVideo = "https://example.com/preview.mp4",
            subCategory = "Mobile Development",
            title = "Kotlin Multiplatform Masterclass",
            updatedAt = 1785660500L,
        )

        val domain = dto.toDomain()
        assertEquals(1001, domain.courseId)
        assertEquals("INTERMEDIATE", domain.level)
        assertEquals(4.5, domain.rating ?: 0.0, 0.001)
        assertEquals("KMP2026", domain.couponCode)
        assertEquals(42, domain.usesRemaining)

        val entity = dto.toEntity()
        assertEquals(1001, entity.courseId)
        assertEquals("INTERMEDIATE", entity.level)
        assertEquals(4.5, entity.rating ?: 0.0, 0.001)
        assertEquals("Kotlin Multiplatform Masterclass", entity.title)
    }

    @Test
    fun couponSummaryDto_mapsToDomainAndEntityCorrectly() {
        val summaryDto = CouponSummaryDTO(
            courseId = 2002,
            isNew = false,
            rating = 4.0f,
            reviews = 50,
            students = 500,
            author = "John Smith",
            category = "IT & Software",
            createdAt = 1785660439L,
            expiredTime = 1785810480L,
            language = "English",
            level = CouponSummaryDTO.Level.ALL_LEVELS,
            previewImage = "https://example.com/thumb.jpg",
            title = "Spring Boot OpenAPI",
        )

        val entity = summaryDto.toEntity()
        assertEquals(2002, entity.courseId)
        assertEquals("ALL_LEVELS", entity.level)
        assertEquals(4.0, entity.rating ?: 0.0, 0.001)
        assertEquals("Spring Boot OpenAPI", entity.title)
        assertNull(entity.couponCode)
        assertNull(entity.description)

        val domain = summaryDto.toDomain()
        assertEquals(2002, domain.courseId)
        assertEquals("ALL_LEVELS", domain.level)
        assertNull(domain.couponUrl)
    }
}
