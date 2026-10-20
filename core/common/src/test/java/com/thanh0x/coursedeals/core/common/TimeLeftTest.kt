package com.thanh0x.coursedeals.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class TimeLeftTest {

    private val fixedInstant = Instant.parse("2026-10-20T12:00:00Z")
    private val fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC)

    @Test
    fun getExpiryDuration_returnsUnknown_forNullOrNonPositive() {
        assertEquals(TimeLeft.ExpiryDuration.Unknown, TimeLeft.getExpiryDuration(null, fixedClock))
        assertEquals(TimeLeft.ExpiryDuration.Unknown, TimeLeft.getExpiryDuration(0L, fixedClock))
        assertEquals(TimeLeft.ExpiryDuration.Unknown, TimeLeft.getExpiryDuration(-1L, fixedClock))
        assertEquals(TimeLeft.ExpiryDuration.Unknown, TimeLeft.getExpiryDuration(-1000L, fixedClock))
    }

    @Test
    fun getExpiryDuration_returnsExpired_forPastTimestamps() {
        // 1 hour ago in seconds
        val oneHourAgoSeconds = fixedInstant.minusSeconds(3600).epochSecond
        assertEquals(TimeLeft.ExpiryDuration.Expired, TimeLeft.getExpiryDuration(oneHourAgoSeconds, fixedClock))

        // 30 seconds ago in milliseconds
        val thirtySecondsAgoMillis = fixedInstant.minusSeconds(30).toEpochMilli()
        assertEquals(TimeLeft.ExpiryDuration.Expired, TimeLeft.getExpiryDuration(thirtySecondsAgoMillis, fixedClock))
    }

    @Test
    fun getExpiryDuration_returnsOneMinute_forFutureWithinOneMinute() {
        // 30 seconds into the future
        val thirtySecondsFuture = fixedInstant.plusSeconds(30).epochSecond
        val result = TimeLeft.getExpiryDuration(thirtySecondsFuture, fixedClock)
        assertTrue(result is TimeLeft.ExpiryDuration.Active)
        assertEquals(1L, (result as TimeLeft.ExpiryDuration.Active).minutesLeft)
    }

    @Test
    fun getExpiryDuration_returnsCorrectMinutes_forFutureDurations() {
        // 45 minutes into the future
        val fortyFiveMinutesFuture = fixedInstant.plusSeconds(45 * 60).epochSecond
        val result = TimeLeft.getExpiryDuration(fortyFiveMinutesFuture, fixedClock)
        assertTrue(result is TimeLeft.ExpiryDuration.Active)
        assertEquals(45L, (result as TimeLeft.ExpiryDuration.Active).minutesLeft)

        // 3 days into the future (in milliseconds)
        val threeDaysFutureMillis = fixedInstant.plusSeconds(3 * 24 * 3600).toEpochMilli()
        val resultDays = TimeLeft.getExpiryDuration(threeDaysFutureMillis, fixedClock)
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
        assertEquals(-1L, TimeLeft.getDurationFromNowToAgo(null, fixedClock))
        assertEquals(-1L, TimeLeft.getDurationFromNowToAgo(0L, fixedClock))
        // Future timestamp returns -1L
        val future = fixedInstant.plusSeconds(600).epochSecond
        assertEquals(-1L, TimeLeft.getDurationFromNowToAgo(future, fixedClock))
        // 10 minutes ago
        val tenMinutesAgo = fixedInstant.minusSeconds(600).epochSecond
        assertEquals(10L, TimeLeft.getDurationFromNowToAgo(tenMinutesAgo, fixedClock))
    }
}
