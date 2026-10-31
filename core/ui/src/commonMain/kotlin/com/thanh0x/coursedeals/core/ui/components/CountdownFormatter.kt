package com.thanh0x.coursedeals.core.ui.components

import androidx.compose.ui.graphics.Color
import com.thanh0x.coursedeals.core.common.Constant
import com.thanh0x.coursedeals.core.common.SystemTimeProvider
import com.thanh0x.coursedeals.core.common.TimeProvider
import com.thanh0x.coursedeals.core.ui.resources.Res
import com.thanh0x.coursedeals.core.ui.resources.formatMultiplatformString

/**
 * Urgency level for coupon expiry.
 */
enum class ExpiryUrgency {
    NORMAL,  // > 24 hours
    WARNING, // < 24 hours (pulsing / warning state)
    CRITICAL // < 1 hour (high urgency badge)
}

/**
 * Parsed countdown state providing localized text, urgency level, and remaining duration info.
 */
data class CountdownState(
    val displayText: String,
    val urgency: ExpiryUrgency,
    val isExpired: Boolean,
    val isUnknown: Boolean,
    val remainingMillis: Long,
)

object CountdownFormatter {
    private const val MILLIS_THRESHOLD = 10_000_000_000L
    private const val MILLIS_PER_SECOND = 1_000L
    private const val MILLIS_PER_MINUTE = 60_000L
    private const val MILLIS_PER_HOUR = 3_600_000L
    private const val MILLIS_PER_DAY = 86_400_000L

    fun formatExpiry(
        expiredDate: Long?,
        timeProvider: TimeProvider = SystemTimeProvider,
    ): CountdownState {
        if (expiredDate == null || expiredDate <= 0L) {
            return CountdownState(
                displayText = Res.string.unknown_time_left.defaultText,
                urgency = ExpiryUrgency.NORMAL,
                isExpired = false,
                isUnknown = true,
                remainingMillis = 0L,
            )
        }

        val targetMillis = if (expiredDate > MILLIS_THRESHOLD) {
            expiredDate
        } else {
            expiredDate * MILLIS_PER_SECOND
        }

        val currentMillis = timeProvider.currentTimeMillis()
        val diffMillis = targetMillis - currentMillis

        if (diffMillis <= 0L) {
            return CountdownState(
                displayText = Res.string.expired_time_left.defaultText,
                urgency = ExpiryUrgency.CRITICAL,
                isExpired = true,
                isUnknown = false,
                remainingMillis = 0L,
            )
        }

        val totalMinutes = diffMillis / MILLIS_PER_MINUTE
        val totalHours = diffMillis / MILLIS_PER_HOUR
        val totalDays = diffMillis / MILLIS_PER_DAY

        return when {
            // Under 1 hour -> High urgency badge ("Expiring soon: 28m" or "Expiring soon: 45s" when < 1m)
            diffMillis < MILLIS_PER_HOUR -> {
                val text = if (totalMinutes >= 1L) {
                    formatMultiplatformString(Res.string.countdown_expiring_soon.defaultText, totalMinutes)
                } else {
                    val seconds = (diffMillis / MILLIS_PER_SECOND).coerceAtLeast(1L)
                    formatMultiplatformString(Res.string.countdown_expiring_soon_seconds.defaultText, seconds)
                }
                CountdownState(
                    displayText = text,
                    urgency = ExpiryUrgency.CRITICAL,
                    isExpired = false,
                    isUnknown = false,
                    remainingMillis = diffMillis,
                )
            }

            // Under 24 hours -> Warning state ("08h 42m remaining")
            diffMillis < MILLIS_PER_DAY -> {
                val hours = totalHours
                val minutes = (diffMillis % MILLIS_PER_HOUR) / MILLIS_PER_MINUTE
                val text = formatMultiplatformString(
                    Res.string.countdown_hours_minutes_remaining.defaultText,
                    hours,
                    minutes,
                )
                CountdownState(
                    displayText = text,
                    urgency = ExpiryUrgency.WARNING,
                    isExpired = false,
                    isUnknown = false,
                    remainingMillis = diffMillis,
                )
            }

            // Exactly 1 day or multiple days left ("> 24h") -> "2 days left" / "1 day left"
            else -> {
                val text = if (totalDays <= 1L) {
                    Res.string.countdown_one_day_left.defaultText
                } else {
                    formatMultiplatformString(Res.string.countdown_days_left.defaultText, totalDays)
                }
                CountdownState(
                    displayText = text,
                    urgency = ExpiryUrgency.NORMAL,
                    isExpired = false,
                    isUnknown = false,
                    remainingMillis = diffMillis,
                )
            }
        }
    }
}
