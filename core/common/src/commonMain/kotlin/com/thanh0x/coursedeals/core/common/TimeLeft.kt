package com.thanh0x.coursedeals.core.common

/**
 * Platform-independent time provider abstraction for multiplatform time queries and testability.
 */
fun interface TimeProvider {
    fun currentTimeMillis(): Long
}

/**
 * Expected platform-specific current time in milliseconds since Unix epoch (1970-01-01T00:00:00Z).
 */
expect fun platformCurrentTimeMillis(): Long

/**
 * Default system time provider.
 */
val SystemTimeProvider: TimeProvider = TimeProvider { platformCurrentTimeMillis() }

object TimeLeft {
    private const val MILLIS_THRESHOLD = 10_000_000_000L
    private const val MILLIS_PER_MINUTE = 60_000L

    sealed interface ExpiryDuration {
        object Unknown : ExpiryDuration
        object Expired : ExpiryDuration
        data class Active(val minutesLeft: Long) : ExpiryDuration
    }

    fun getExpiryDuration(
        expiredDate: Long?,
        timeProvider: TimeProvider = SystemTimeProvider,
    ): ExpiryDuration {
        if (expiredDate == null || expiredDate <= 0L) return ExpiryDuration.Unknown
        return try {
            val inputMillis = if (expiredDate > MILLIS_THRESHOLD) {
                expiredDate
            } else {
                expiredDate * 1000L
            }
            val nowMillis = timeProvider.currentTimeMillis()
            if (inputMillis < nowMillis) {
                ExpiryDuration.Expired
            } else {
                val minutes = (inputMillis - nowMillis) / MILLIS_PER_MINUTE
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

    fun getDurationFromNowToAgo(
        timestamp: Long?,
        timeProvider: TimeProvider = SystemTimeProvider,
    ): Long {
        if (timestamp == null || timestamp <= 0L) return -1L
        return try {
            val inputMillis = if (timestamp > MILLIS_THRESHOLD) {
                timestamp
            } else {
                timestamp * 1000L
            }
            val nowMillis = timeProvider.currentTimeMillis()
            if (inputMillis > nowMillis) {
                -1L
            } else {
                (nowMillis - inputMillis) / MILLIS_PER_MINUTE
            }
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            -1L
        }
    }
}
