package com.thanh0x.coursedeals.util

import android.content.Context
import android.text.Html
import com.thanh0x.coursedeals.R

class MapperToView(val context: Context) {
    fun mapTimeLeft(expiredDate: Long?): String {
        val durationInMinutes = TimeLeft.getDurationFromNow(expiredDate)
        val minutesInHour = Constant.MINUTES_IN_HOUR
        val minutesInDay = Constant.MINUTES_IN_HOUR * Constant.HOURS_IN_DAY
        val minutesInMonth = minutesInDay * Constant.DAYS_IN_MONTH

        return when (durationInMinutes) {
            -1L -> context.getString(R.string.unknown_time_left)
            in 0..1 -> context.getString(
                R.string.few_minute_left, durationInMinutes
            )

            in 2..minutesInHour -> context.getString(
                R.string.lots_of_minutes_left, durationInMinutes
            )

            in (minutesInHour + 1)..minutesInDay -> context.getString(
                R.string.lots_of_hours_left, durationInMinutes / minutesInHour
            )

            in (minutesInDay + 1)..minutesInMonth -> context.getString(
                R.string.lots_of_days_left, durationInMinutes / minutesInDay
            )

            else -> context.getString(
                R.string.unknown_time_left
            )
        }
    }

    fun mapTimeAgo(timestamp: Long?): String {
        val minutesAgo = TimeLeft.getDurationFromNowToAgo(timestamp)
        val minutesInHour = Constant.MINUTES_IN_HOUR
        val minutesInDay = Constant.MINUTES_IN_HOUR * Constant.HOURS_IN_DAY

        return when (minutesAgo) {
            -1L -> context.getString(R.string.unknown_time_ago)
            in 0..1 -> context.getString(R.string.few_minute_ago)
            in 2..minutesInHour -> context.getString(R.string.lots_of_minutes_ago, minutesAgo)
            in (minutesInHour + 1)..minutesInDay -> context.getString(
                R.string.lots_of_hours_ago,
                minutesAgo / minutesInHour
            )

            else -> context.getString(R.string.lots_of_days_ago, minutesAgo / minutesInDay)
        }
    }

    fun mapContentLength(contentLength: Int?): String {
        if (contentLength == null) return context.getString(R.string.unknown_content_length)
        val minutesInHour = Constant.MINUTES_IN_HOUR
        return when (contentLength) {
            in 0..1 -> context.getString(
                R.string.few_content_length, contentLength
            )

            in 2..minutesInHour -> context.getString(
                R.string.lots_of_content_length, contentLength
            )

            in (minutesInHour + 1)..Int.MAX_VALUE -> context.getString(
                R.string.hours_content_length, contentLength / minutesInHour
            )

            else -> context.getString(
                R.string.unknown_content_length
            )
        }
    }

    fun mapHTMLContent(htmlString: String?): String {
        if (htmlString == null) return ""
        return Html.fromHtml(htmlString, Html.FROM_HTML_MODE_LEGACY).toString()
    }

    fun mapRating(rating: Double?): Float {
        return rating?.toFloat() ?: 0f
    }

    fun mapRatingValue(rating: Double?): String {
        return "%.1f".format(rating ?: 0.0)
    }

    fun mapNumberOfReview(numberOfReview: Int?): String {
        if (numberOfReview == null) return context.getString(R.string.few_review, 0)
        return if (numberOfReview <= 1) {
            context.getString(
                R.string.few_review, numberOfReview
            )
        } else if (numberOfReview > Constant.THRESHOLD_THOUSANDS) {
            context.getString(
                R.string.thousands_reviews, numberOfReview / Constant.THRESHOLD_THOUSANDS
            )
        } else {
            context.getString(
                R.string.lots_of_review, numberOfReview
            )
        }
    }

    fun mapNumberOfStudent(numberOfStudent: Int?): String {
        if (numberOfStudent == null) return context.getString(R.string.few_student, 0)
        return if (numberOfStudent <= 1) {
            context.getString(
                R.string.few_student, numberOfStudent
            )
        } else if (numberOfStudent > Constant.THRESHOLD_THOUSANDS) {
            context.getString(
                R.string.thousand_students, numberOfStudent / Constant.THRESHOLD_THOUSANDS
            )
        } else {
            context.getString(
                R.string.lots_of_student, numberOfStudent
            )
        }
    }
}
