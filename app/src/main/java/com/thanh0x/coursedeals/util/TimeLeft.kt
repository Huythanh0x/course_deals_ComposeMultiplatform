package com.thanh0x.coursedeals.util

import android.annotation.SuppressLint
import android.util.Log
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object TimeLeft {
    @SuppressLint("NewApi")
    private fun getTimeDifferentInMinute(startTime: String): Long {
        if (startTime == "") return 0
        val startInstant = LocalDateTime.parse(startTime).toInstant(ZoneOffset.UTC)
        val endInstant = Instant.now()
        return (endInstant.toEpochMilli() - startInstant.toEpochMilli()) / (1000 * 60)
    }

    @SuppressLint("NewApi")
    fun getDurationFromNow(expiredDate: Double?): Long {
        if (expiredDate == null) return -1L
        return try {
            val seconds = expiredDate.toLong()
            val inputInstant = Instant.ofEpochSecond(seconds)
            val now = Instant.now()
            Duration.between(now, inputInstant).toMinutes()
        } catch (e: Exception) {
            Log.e("TimeLeft", "Error parsing timestamp: $expiredDate", e)
            -1L
        }
    }
}
