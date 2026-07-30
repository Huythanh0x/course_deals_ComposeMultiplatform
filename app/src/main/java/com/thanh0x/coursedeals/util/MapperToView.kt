package com.thanh0x.coursedeals.util

import android.content.Context
import android.text.Html
import com.thanh0x.coursedeals.R

class MapperToView(val context: Context) {
    fun mapTimeLeft(expiredDate: Long?): String {
        val durationInMinutes = TimeLeft.getDurationFromNow(expiredDate)
        return when (durationInMinutes) {
            -1L -> context.getString(R.string.unknown_time_left)
            in 0..1 -> context.getString(
                R.string.few_minute_left, durationInMinutes
            )

            in 2..2 * 60 -> context.getString(
                R.string.lots_of_minutes_left, durationInMinutes
            )

            in 2 * 60..2 * 60 * 24 -> context.getString(
                R.string.lots_of_hours_left, durationInMinutes / 60
            )

            in 2 * 60 * 24..30 * 60 * 24 -> context.getString(
                R.string.lots_of_days_left, durationInMinutes / 60 / 24
            )

            else -> context.getString(
                R.string.unknown_time_left
            )
        }
    }

    fun mapTimeAgo(timestamp: Long?): String {
        val minutesAgo = TimeLeft.getDurationFromNowToAgo(timestamp)
        return when (minutesAgo) {
            -1L -> context.getString(R.string.unknown_time_ago)
            in 0..1 -> context.getString(R.string.few_minute_ago)
            in 2..60 -> context.getString(R.string.lots_of_minutes_ago, minutesAgo)
            in 61..1440 -> context.getString(R.string.lots_of_hours_ago, minutesAgo / 60)
            else -> context.getString(R.string.lots_of_days_ago, minutesAgo / 60 / 24)
        }
    }

    fun mapContentLength(contentLength: Int?): String {
        if (contentLength == null) return context.getString(R.string.unknown_content_length)
        return when (contentLength) {
            in 0..1 -> context.getString(
                R.string.few_content_length, contentLength
            )

            in 2..60 -> context.getString(
                R.string.lots_of_content_length, contentLength
            )

            in 60..Int.MAX_VALUE -> context.getString(
                R.string.hours_content_length, contentLength / 60
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

    fun mapNumberOfReview(numberOfReview: Int?): String {
        if (numberOfReview == null) return context.getString(R.string.few_review, 0)
        return if (numberOfReview <= 1) {
            context.getString(
                R.string.few_review, numberOfReview
            )
        } else if (numberOfReview > 1000) {
            context.getString(
                R.string.thousands_reviews, numberOfReview / 1000
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
        } else if (numberOfStudent > 1000) {
            context.getString(
                R.string.thousand_students, numberOfStudent / 1000
            )
        } else {
            context.getString(
                R.string.lots_of_student, numberOfStudent
            )
        }
    }
}
