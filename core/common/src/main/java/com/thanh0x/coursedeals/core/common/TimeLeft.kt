package com.thanh0x.coursedeals.core.common

import java.time.Clock
import java.time.Duration
import java.time.Instant

object TimeLeft {
    private const val MILLIS_THRESHOLD = 10_000_000_000L

    sealed interface ExpiryDuration {
        object Unknown : ExpiryDuration
        object Expired : ExpiryDuration
        data class Active(val minutesLeft: Long) : ExpiryDuration
    }

    fun getExpiryDuration(expiredDate: Long?, clock: Clock = Clock.systemUTC()): ExpiryDuration {
        if (expiredDate == null || expiredDate <= 0L) return ExpiryDuration.Unknown
        return try {
            val inputInstant = if (expiredDate > MILLIS_THRESHOLD) {
                Instant.ofEpochMilli(expiredDate)
            } else {
                Instant.ofEpochSecond(expiredDate)
            }
            val now = Instant.now(clock)
            if (inputInstant.isBefore(now)) {
                ExpiryDuration.Expired
            } else {
                val minutes = Duration.between(now, inputInstant).toMinutes()
                ExpiryDuration.Active(if (minutes <= 0L) 1L else minutes)
            }
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            ExpiryDuration.Unknown
        }
    }

    fun getDurationFromNow(expiredDate: Long?): Long {
        return when (val expiry = getExpiryDuration(expiredDate)) {
            is ExpiryDuration.Unknown -> -1L
            is ExpiryDuration.Expired -> -2L
            is ExpiryDuration.Active -> expiry.minutesLeft
        }
    }

    fun getDurationFromNowToAgo(timestamp: Long?, clock: Clock = Clock.systemUTC()): Long {
        if (timestamp == null || timestamp <= 0L) return -1L
        return try {
            val inputInstant = if (timestamp > MILLIS_THRESHOLD) {
                Instant.ofEpochMilli(timestamp)
            } else {
                Instant.ofEpochSecond(timestamp)
            }
            val now = Instant.now(clock)
            if (inputInstant.isAfter(now)) {
                -1L
            } else {
                Duration.between(inputInstant, now).toMinutes()
            }
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            -1L
        }
    }
}

