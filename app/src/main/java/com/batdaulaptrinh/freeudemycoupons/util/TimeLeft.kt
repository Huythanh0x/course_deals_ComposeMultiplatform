package com.batdaulaptrinh.freeudemycoupons.util

import android.annotation.SuppressLint
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
    fun getDurationFromNow(dateTimeString: String): Long {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssXXX")
        val dateTime = LocalDateTime.parse(dateTimeString, formatter)
        val inputInstant = dateTime.atZone(ZoneId.systemDefault()).toInstant()
        val now = Instant.now()
        return Duration.between(now, inputInstant).toMinutes()
    }
}
