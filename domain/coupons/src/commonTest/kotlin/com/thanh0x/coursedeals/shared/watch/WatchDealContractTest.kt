package com.thanh0x.coursedeals.shared.watch

import com.thanh0x.coursedeals.domain.coupons.Coupon
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class WatchDealContractTest {

    @Test
    fun `mapHotDealsApiJsonToWatchDeals parses backend hot deals envelope and applies bookmarks`() {
        val rawJson = """
            {
              "courses": [
                {
                  "courseId": 701,
                  "title": "Mastering Kotlin Multiplatform for Apple Watch",
                  "author": "Course Deals Team",
                  "category": "Mobile Development",
                  "couponCode": "WATCH100",
                  "targetUrl": "https://udemy.com/course/kmp-watch?couponCode=WATCH100",
                  "expiredTime": 1999999999,
                  "rating": 4.9,
                  "reviews": 400,
                  "students": 24500
                }
              ]
            }
        """.trimIndent()

        val deals = WatchDealJsonCodec.mapHotDealsApiJsonToWatchDeals(
            apiJson = rawJson,
            bookmarkedIds = setOf(701),
        )

        assertEquals(1, deals.size)
        val first = deals.first()
        assertEquals(701, first.id)
        assertEquals("Mastering Kotlin Multiplatform for Apple Watch", first.title)
        assertEquals("★ 4.9", first.formattedRating)
        assertEquals("25k students", first.formattedStudents)
        assertTrue(first.isBookmarked)
        assertEquals("coursedeals://courses/701", first.deepLinkUrlString)
    }

    @Test
    fun `Coupon toWatchDealDto maps shared domain Coupon directly to WatchDealDto`() {
        val coupon = Coupon(
            courseId = 808,
            author = "KMP Instructor",
            category = "Software Engineering",
            contentLength = 120,
            couponCode = "KMP808",
            couponUrl = "https://coursedeals.com/deal/808",
            description = "Shared model test",
            expiredTime = 1_800_000_000L,
            heading = "Build once, share domain everywhere",
            language = "English",
            level = "All Levels",
            previewImage = "https://coursera.org/img.png",
            previewVideo = null,
            rating = 4.85,
            reviews = 320,
            students = 1_200_000,
            subCategory = "Multiplatform",
            title = "Coursera KMP Architecture",
            usesRemaining = 25,
            isNew = true,
        )

        val watchDeal = coupon.toWatchDealDto(isBookmarked = true)
        assertEquals(808, watchDeal.id)
        assertEquals(WatchCoursePlatform.COURSERA, watchDeal.platform)
        assertEquals("★ 4.9", watchDeal.formattedRating)
        assertEquals("1.2M students", watchDeal.formattedStudents)
        assertTrue(watchDeal.isBookmarked)
    }

    @Test
    fun `formattedTimeLeftAt and isExpiredAt handle epoch seconds and milliseconds`() {
        val nowMillis = 1_700_000_000_000L
        val dealInTwoHours = WatchDealJsonCodec.sampleDeal(nowEpochSeconds = 1_700_000_000L)

        assertFalse(dealInTwoHours.isExpiredAt(nowMillis))
        assertEquals("2h 0m left", dealInTwoHours.formattedTimeLeftAt(nowMillis))
        assertTrue(dealInTwoHours.isExpiredAt(nowMillis + 8_000_000L))
        assertEquals("Expired", dealInTwoHours.formattedTimeLeftAt(nowMillis + 8_000_000L))
    }

    @Test
    fun `selectTopActiveDeal prioritizes non-expired hot deals by rating and students`() {
        val nowMillis = 1_700_000_000_000L
        val expiredHigh = WatchDealDto(
            id = 1,
            title = "Expired",
            expiredTime = 1_699_000_000L,
            rating = 5.0,
            students = 99_000,
        )
        val activeLower = WatchDealDto(
            id = 2,
            title = "Active 4.7",
            expiredTime = 1_700_005_000L,
            rating = 4.7,
            students = 10_000,
        )
        val activeHigher = WatchDealDto(
            id = 3,
            title = "Active 4.9",
            expiredTime = 1_700_005_000L,
            rating = 4.9,
            students = 50_000,
        )

        val list = listOf(expiredHigh, activeLower, activeHigher)
        val top = WatchDealJsonCodec.selectTopActiveDeal(list, nowMillis)
        assertNotNull(top)
        assertEquals(3, top.id)
        assertEquals(2, WatchDealJsonCodec.countActiveDeals(list, nowMillis))
    }

    @Test
    fun `parseCourseIdFromDeepLink extracts positive course ID across supported prefixes`() {
        assertEquals(501, WatchDealJsonCodec.parseCourseIdFromDeepLink("coursedeals://courses/501?bookmark=1"))
        assertEquals(777, WatchDealJsonCodec.parseCourseIdFromDeepLink("https://coursedeals.com/deal/777"))
        assertEquals(null, WatchDealJsonCodec.parseCourseIdFromDeepLink("coursedeals://feed"))
    }
}
