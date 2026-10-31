package com.thanh0x.coursedeals.core.ui.components

import com.thanh0x.coursedeals.core.common.TimeProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CountdownFormatterTest {

    private val fixedNowMillis = 1792497600000L // 2026-10-20T12:00:00Z
    private val testTimeProvider = TimeProvider { fixedNowMillis }

    @Test
    fun formatExpiry_nullOrNonPositive_returnsUnknownState() {
        val nullResult = CountdownFormatter.formatExpiry(null, testTimeProvider)
        assertTrue(nullResult.isUnknown)
        assertFalse(nullResult.isExpired)
        assertEquals("unknown", nullResult.displayText)
        assertEquals(ExpiryUrgency.NORMAL, nullResult.urgency)
        assertEquals(0L, nullResult.remainingMillis)

        val zeroResult = CountdownFormatter.formatExpiry(0L, testTimeProvider)
        assertTrue(zeroResult.isUnknown)

        val negativeResult = CountdownFormatter.formatExpiry(-500L, testTimeProvider)
        assertTrue(negativeResult.isUnknown)
    }

    @Test
    fun formatExpiry_pastTimestamp_returnsExpiredState() {
        val pastMillis = fixedNowMillis - 60_000L
        val result = CountdownFormatter.formatExpiry(pastMillis, testTimeProvider)

        assertFalse(result.isUnknown)
        assertTrue(result.isExpired)
        assertEquals("expired", result.displayText)
        assertEquals(ExpiryUrgency.CRITICAL, result.urgency)
        assertEquals(0L, result.remainingMillis)

        // Seconds format
        val pastSeconds = (fixedNowMillis - 100_000L) / 1000L
        val resultSeconds = CountdownFormatter.formatExpiry(pastSeconds, testTimeProvider)
        assertTrue(resultSeconds.isExpired)
    }

    @Test
    fun formatExpiry_moreThan24Hours_returnsDaysLeftWithNormalUrgency() {
        // 2 days in milliseconds
        val twoDaysFutureMillis = fixedNowMillis + (2 * 24 * 3600 * 1000L)
        val result = CountdownFormatter.formatExpiry(twoDaysFutureMillis, testTimeProvider)

        assertFalse(result.isUnknown)
        assertFalse(result.isExpired)
        assertEquals(ExpiryUrgency.NORMAL, result.urgency)
        assertEquals("2 days left", result.displayText)

        // 3 days in seconds
        val threeDaysFutureSeconds = (fixedNowMillis + (3 * 24 * 3600 * 1000L)) / 1000L
        val resultSeconds = CountdownFormatter.formatExpiry(threeDaysFutureSeconds, testTimeProvider)
        assertEquals(ExpiryUrgency.NORMAL, resultSeconds.urgency)
        assertEquals("3 days left", resultSeconds.displayText)

        // Exactly 1 day + 2 hours (26 hours) -> "1 day left"
        val oneDayTwoHoursMillis = fixedNowMillis + (26 * 3600 * 1000L)
        val resultOneDay = CountdownFormatter.formatExpiry(oneDayTwoHoursMillis, testTimeProvider)
        assertEquals(ExpiryUrgency.NORMAL, resultOneDay.urgency)
        assertEquals("1 day left", resultOneDay.displayText)
    }

    @Test
    fun formatExpiry_under24Hours_returnsHoursMinutesWithWarningUrgency() {
        // 8 hours and 42 minutes remaining
        val futureMillis = fixedNowMillis + (8 * 3600 * 1000L) + (42 * 60 * 1000L)
        val result = CountdownFormatter.formatExpiry(futureMillis, testTimeProvider)

        assertFalse(result.isUnknown)
        assertFalse(result.isExpired)
        assertEquals(ExpiryUrgency.WARNING, result.urgency)
        assertEquals("08h 42m remaining", result.displayText)

        // 1 hour and 5 minutes remaining
        val oneHourFiveMinutesMillis = fixedNowMillis + (1 * 3600 * 1000L) + (5 * 60 * 1000L)
        val resultOneHour = CountdownFormatter.formatExpiry(oneHourFiveMinutesMillis, testTimeProvider)
        assertEquals(ExpiryUrgency.WARNING, resultOneHour.urgency)
        assertEquals("01h 05m remaining", resultOneHour.displayText)

        // 23 hours and 59 minutes remaining (just below 24h)
        val almostDayMillis = fixedNowMillis + (23 * 3600 * 1000L) + (59 * 60 * 1000L)
        val resultAlmostDay = CountdownFormatter.formatExpiry(almostDayMillis, testTimeProvider)
        assertEquals(ExpiryUrgency.WARNING, resultAlmostDay.urgency)
        assertEquals("23h 59m remaining", resultAlmostDay.displayText)
    }

    @Test
    fun formatExpiry_underOneHour_returnsExpiringSoonWithCriticalUrgency() {
        // 28 minutes remaining
        val twentyEightMinutesMillis = fixedNowMillis + (28 * 60 * 1000L)
        val result = CountdownFormatter.formatExpiry(twentyEightMinutesMillis, testTimeProvider)

        assertFalse(result.isUnknown)
        assertFalse(result.isExpired)
        assertEquals(ExpiryUrgency.CRITICAL, result.urgency)
        assertEquals("Expiring soon: 28m", result.displayText)

        // 1 minute remaining
        val oneMinuteMillis = fixedNowMillis + (1 * 60 * 1000L)
        val resultOneMinute = CountdownFormatter.formatExpiry(oneMinuteMillis, testTimeProvider)
        assertEquals(ExpiryUrgency.CRITICAL, resultOneMinute.urgency)
        assertEquals("Expiring soon: 1m", resultOneMinute.displayText)

        // 45 seconds remaining (< 1 minute)
        val fortyFiveSecondsMillis = fixedNowMillis + 45_000L
        val resultSeconds = CountdownFormatter.formatExpiry(fortyFiveSecondsMillis, testTimeProvider)
        assertEquals(ExpiryUrgency.CRITICAL, resultSeconds.urgency)
        assertEquals("Expiring soon: 45s", resultSeconds.displayText)
    }

    @Test
    fun formatExpiry_exactBoundaries() {
        // Boundary: 24h exactly
        val exact24hMillis = fixedNowMillis + (24 * 3600 * 1000L)
        val result24h = CountdownFormatter.formatExpiry(exact24hMillis, testTimeProvider)
        assertEquals(ExpiryUrgency.NORMAL, result24h.urgency)
        assertEquals("1 day left", result24h.displayText)

        // Boundary: 1h exactly
        val exact1hMillis = fixedNowMillis + (3600 * 1000L)
        val result1h = CountdownFormatter.formatExpiry(exact1hMillis, testTimeProvider)
        assertEquals(ExpiryUrgency.WARNING, result1h.urgency)
        assertEquals("01h 00m remaining", result1h.displayText)
    }
}
