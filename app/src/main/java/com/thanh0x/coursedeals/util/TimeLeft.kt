package com.thanh0x.coursedeals.util

import android.annotation.SuppressLint
import android.util.Log
import java.time.Duration
import java.time.Instant

object TimeLeft {
    @SuppressLint("NewApi")
    fun getDurationFromNow(expiredDate: Long?): Long {
        if (expiredDate == null) return -1L
        return try {
            val inputInstant = Instant.ofEpochSecond(expiredDate)
            val now = Instant.now()
            Duration.between(now, inputInstant).toMinutes()
        } catch (e: Exception) {
            Log.e("TimeLeft", "Error parsing timestamp: $expiredDate", e)
            -1L
        }
    }

    @SuppressLint("NewApi")
    fun getDurationFromNowToAgo(timestamp: Long?): Long {
        if (timestamp == null) return -1L
        return try {
            val inputInstant = Instant.ofEpochSecond(timestamp)
            val now = Instant.now()
            Duration.between(inputInstant, now).toMinutes()
        } catch (e: Exception) {
            Log.e("TimeLeft", "Error parsing timestamp: $timestamp", e)
            -1L
        }
    }
}
