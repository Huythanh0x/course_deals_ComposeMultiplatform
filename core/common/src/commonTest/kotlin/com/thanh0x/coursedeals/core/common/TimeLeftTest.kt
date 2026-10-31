package com.thanh0x.coursedeals.core.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TimeLeftTest {

    private val fixedTimestampMillis = 1792497600000L // 2026-10-20T12:00:00Z
    private val fixedTimeProvider = TimeProvider { fixedTimestampMillis }

    @Test
    fun getExpiryDuration_returnsUnknown_forNullOrNonPositive() {
        assertEquals(TimeLeft.ExpiryDuration.Unknown, TimeLeft.getExpiryDuration(null, fixedTimeProvider))
        assertEquals(TimeLeft.ExpiryDuration.Unknown, TimeLeft.getExpiryDuration(0L, fixedTimeProvider))
        assertEquals(TimeLeft.ExpiryDuration.Unknown, TimeLeft.getExpiryDuration(-1L, fixedTimeProvider))
        assertEquals(TimeLeft.ExpiryDuration.Unknown, TimeLeft.getExpiryDuration(-1000L, fixedTimeProvider))
    }

    @Test
    fun getExpiryDuration_returnsExpired_forPastTimestamps() {
        // 1 hour ago in seconds
        val oneHourAgoSeconds = (fixedTimestampMillis - 3600 * 1000L) / 1000L
        assertEquals(TimeLeft.ExpiryDuration.Expired, TimeLeft.getExpiryDuration(oneHourAgoSeconds, fixedTimeProvider))

        // 30 seconds ago in milliseconds
        val thirtySecondsAgoMillis = fixedTimestampMillis - 30 * 1000L
        assertEquals(TimeLeft.ExpiryDuration.Expired, TimeLeft.getExpiryDuration(thirtySecondsAgoMillis, fixedTimeProvider))
    }

    @Test
    fun getExpiryDuration_returnsOneMinute_forFutureWithinOneMinute() {
        // 30 seconds into the future
        val thirtySecondsFuture = (fixedTimestampMillis + 30 * 1000L) / 1000L
        val result = TimeLeft.getExpiryDuration(thirtySecondsFuture, fixedTimeProvider)
        assertTrue(result is TimeLeft.ExpiryDuration.Active)
        assertEquals(1L, (result as TimeLeft.ExpiryDuration.Active).minutesLeft)
    }

    @Test
    fun getExpiryDuration_returnsCorrectMinutes_forFutureDurations() {
        // 45 minutes into the future
        val fortyFiveMinutesFuture = (fixedTimestampMillis + 45 * 60 * 1000L) / 1000L
        val result = TimeLeft.getExpiryDuration(fortyFiveMinutesFuture, fixedTimeProvider)
        assertTrue(result is TimeLeft.ExpiryDuration.Active)
        assertEquals(45L, (result as TimeLeft.ExpiryDuration.Active).minutesLeft)

        // 3 days into the future (in milliseconds)
        val threeDaysFutureMillis = fixedTimestampMillis + 3 * 24 * 3600 * 1000L
        val resultDays = TimeLeft.getExpiryDuration(threeDaysFutureMillis, fixedTimeProvider)
        assertTrue(resultDays is TimeLeft.ExpiryDuration.Active)
        assertEquals(3 * 24 * 60L, (resultDays as TimeLeft.ExpiryDuration.Active).minutesLeft)
    }

    @Test
    fun getDurationFromNow_legacyMethodReturnsExpectedCodes() {
        assertEquals(-1L, TimeLeft.getDurationFromNow(null))
        assertEquals(-1L, TimeLeft.getDurationFromNow(0L))
        // Past timestamp
        assertEquals(-2L, TimeLeft.getDurationFromNow(100L))
    }

    @Test
    fun getDurationFromNowToAgo_handlesEdgeCases() {
        assertEquals(-1L, TimeLeft.getDurationFromNowToAgo(null, fixedTimeProvider))
        assertEquals(-1L, TimeLeft.getDurationFromNowToAgo(0L, fixedTimeProvider))
        // Future timestamp returns -1L
        val future = (fixedTimestampMillis + 600 * 1000L) / 1000L
        assertEquals(-1L, TimeLeft.getDurationFromNowToAgo(future, fixedTimeProvider))
        // 10 minutes ago
        val tenMinutesAgo = (fixedTimestampMillis - 600 * 1000L) / 1000L
        assertEquals(10L, TimeLeft.getDurationFromNowToAgo(tenMinutesAgo, fixedTimeProvider))
    }
}
