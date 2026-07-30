package com.thanh0x.coursedeals.util

import android.annotation.SuppressLint
import timber.log.Timber
import java.time.Duration
import java.time.Instant

object TimeLeft {
    @SuppressLint("NewApi")
    @Suppress("TooGenericExceptionCaught")
    fun getDurationFromNow(expiredDate: Long?): Long {
        if (expiredDate == null) return -1L
        return try {
            val inputInstant = Instant.ofEpochSecond(expiredDate)
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
            val inputInstant = Instant.ofEpochSecond(timestamp)
            val now = Instant.now()
            Duration.between(inputInstant, now).toMinutes()
        } catch (e: Exception) {
            Timber.e(e, "Error parsing timestamp: $timestamp")
            -1L
        }
    }
}
