package com.thanh0x.coursedeals.util

import android.content.Context
import android.text.Html
import com.thanh0x.coursedeals.R

class MapperToView(val context: Context) {
    fun mapTimeLeft(expiredDate: Long): String {
        return when (TimeLeft.getDurationFromNow(expiredDate)) {
            in 0..1 -> context.getString(
                R.string.few_minute_left, TimeLeft.getDurationFromNow(expiredDate)
            )

            in 2..2 * 60 -> context.getString(
                R.string.lots_of_minutes_left, TimeLeft.getDurationFromNow(expiredDate)
            )

            in 2 * 60..2 * 60 * 24 -> context.getString(
                R.string.lots_of_hours_left, TimeLeft.getDurationFromNow(expiredDate) / 60
            )

            in 2 * 60 * 24..30 * 60 * 24 -> context.getString(
                R.string.lots_of_days_left, TimeLeft.getDurationFromNow(expiredDate) / 60 / 24
            )

            else -> context.getString(
                R.string.unknown_time_left
            )
        }
    }

    fun mapContentLength(contentLength: Int): String {
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

    fun mapHTMLContent(htmlString: String): String {
        return Html.fromHtml(htmlString, Html.FROM_HTML_MODE_LEGACY).toString()
    }

    fun mapRating(rating: Double): Float {
        return rating.toFloat()
    }

    fun mapNumberOfReview(numberOfReview: Int): String {
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

    fun mapNumberOfStudent(numberOfStudent: Int): String {
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
