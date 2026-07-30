package com.thanh0x.coursedeals.util

import android.annotation.SuppressLint
import timber.log.Timber
import java.time.Duration
import java.time.Instant

object TimeLeft {
    private const val MILLIS_THRESHOLD = 10_000_000_000L

    @SuppressLint("NewApi")
    @Suppress("TooGenericExceptionCaught")
    fun getDurationFromNow(expiredDate: Long?): Long {
        if (expiredDate == null) return -1L
        return try {
            val inputInstant = if (expiredDate > MILLIS_THRESHOLD) {
                Instant.ofEpochMilli(expiredDate)
            } else {
                Instant.ofEpochSecond(expiredDate)
            }
            val now = Instant.now()
            Duration.between(now, inputInstant).toMinutes()
        } catch (e: Exception) {
            Timber.e(e, "Error parsing timestamp: $expiredDate")
            -1L
        }
    }

    @SuppressLint("NewApi")
    @Suppress("TooGenericExceptionCaught")
    fun getDurationFromNowToAgo(timestamp: Long?): Long {
        if (timestamp == null) return -1L
        return try {
            val inputInstant = if (timestamp > MILLIS_THRESHOLD) {
                Instant.ofEpochMilli(timestamp)
            } else {
                Instant.ofEpochSecond(timestamp)
            }
            val now = Instant.now()
            Duration.between(inputInstant, now).toMinutes()
        } catch (e: Exception) {
            Timber.e(e, "Error parsing timestamp: $timestamp")
            -1L
        }
    }
}
