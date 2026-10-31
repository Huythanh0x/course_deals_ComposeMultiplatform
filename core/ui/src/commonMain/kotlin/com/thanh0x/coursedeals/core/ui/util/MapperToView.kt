package com.thanh0x.coursedeals.core.ui.util

import com.thanh0x.coursedeals.core.common.Constant
import com.thanh0x.coursedeals.core.common.TimeLeft
import com.thanh0x.coursedeals.core.ui.resources.Res
import com.thanh0x.coursedeals.core.ui.resources.formatMultiplatformString
import kotlin.math.roundToInt

class MapperToView(val context: Any? = null) {
    fun mapTimeLeft(expiredDate: Long?): String {
        val minutesInHour = Constant.MINUTES_IN_HOUR
        val minutesInDay = Constant.MINUTES_IN_HOUR * Constant.HOURS_IN_DAY
        val minutesInMonth = minutesInDay * Constant.DAYS_IN_MONTH
        val minutesInTwoMonths = minutesInMonth * 2

        return when (val expiry = TimeLeft.getExpiryDuration(expiredDate)) {
            is TimeLeft.ExpiryDuration.Unknown -> Res.string.unknown_time_left.defaultText
            is TimeLeft.ExpiryDuration.Expired -> Res.string.expired_time_left.defaultText
            is TimeLeft.ExpiryDuration.Active -> {
                val durationInMinutes = expiry.minutesLeft
                when (durationInMinutes) {
                    1L -> formatMultiplatformString(Res.string.few_minute_left.defaultText, 1)
                    in 2..minutesInHour -> formatMultiplatformString(
                        Res.string.lots_of_minutes_left.defaultText,
                        durationInMinutes,
                    )
                    in (minutesInHour + 1)..minutesInDay -> formatMultiplatformString(
                        Res.string.lots_of_hours_left.defaultText,
                        durationInMinutes / minutesInHour,
                    )
                    in (minutesInDay + 1)..minutesInTwoMonths -> formatMultiplatformString(
                        Res.string.lots_of_days_left.defaultText,
                        durationInMinutes / minutesInDay,
                    )
                    else -> formatMultiplatformString(
                        Res.string.lots_of_months_left.defaultText,
                        durationInMinutes / minutesInMonth,
                    )
                }
            }
        }
    }

    fun mapTimeAgo(timestamp: Long?): String {
        val minutesAgo = TimeLeft.getDurationFromNowToAgo(timestamp)
        val minutesInHour = Constant.MINUTES_IN_HOUR
        val minutesInDay = Constant.MINUTES_IN_HOUR * Constant.HOURS_IN_DAY

        return when (minutesAgo) {
            -1L -> Res.string.unknown_time_ago.defaultText
            in 0..1 -> Res.string.few_minute_ago.defaultText
            in 2..minutesInHour -> formatMultiplatformString(
                Res.string.lots_of_minutes_ago.defaultText,
                minutesAgo,
            )
            in (minutesInHour + 1)..minutesInDay -> formatMultiplatformString(
                Res.string.lots_of_hours_ago.defaultText,
                minutesAgo / minutesInHour,
            )
            else -> formatMultiplatformString(
                Res.string.lots_of_days_ago.defaultText,
                minutesAgo / minutesInDay,
            )
        }
    }

    fun mapContentLength(contentLength: Int?): String {
        val minutesInHour = Constant.MINUTES_IN_HOUR
        return when (contentLength) {
            null -> Res.string.unknown_content_length.defaultText
            0 -> Res.string.text_practice_test.defaultText
            else -> {
                if (contentLength < minutesInHour) {
                    formatMultiplatformString(Res.string.lots_of_content_length.defaultText, contentLength)
                } else {
                    val hours = contentLength.toDouble() / minutesInHour
                    val formattedHours = if ((hours % 1.0) == 0.0) {
                        hours.toInt().toString()
                    } else {
                        ((hours * 10.0).roundToInt() / 10.0).toString()
                    }
                    formatMultiplatformString(Res.string.hours_content_length.defaultText, formattedHours)
                }
            }
        }
    }

    fun mapHTMLContent(htmlString: String?): String {
        if (htmlString == null) return ""
        return htmlString
            .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("</p>", RegexOption.IGNORE_CASE), "\n\n")
            .replace(Regex("<li>", RegexOption.IGNORE_CASE), "• ")
            .replace(Regex("</li>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<[^>]*>"), "")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&nbsp;", " ")
            .trim()
    }

    fun mapRating(rating: Double?): Float {
        return rating?.toFloat() ?: 0f
    }

    fun mapRatingValue(rating: Double?): String {
        val value = rating ?: 0.0
        val tenths = (value * 10.0).roundToInt()
        return "${tenths / 10}.${tenths % 10}"
    }

    fun mapNumberOfReview(numberOfReview: Int?): String {
        if (numberOfReview == null) return formatMultiplatformString(Res.string.few_review.defaultText, 0)
        return if (numberOfReview <= 1) {
            formatMultiplatformString(Res.string.few_review.defaultText, numberOfReview)
        } else if (numberOfReview > Constant.THRESHOLD_THOUSANDS) {
            formatMultiplatformString(
                Res.string.thousands_reviews.defaultText,
                numberOfReview / Constant.THRESHOLD_THOUSANDS,
            )
        } else {
            formatMultiplatformString(Res.string.lots_of_review.defaultText, numberOfReview)
        }
    }

    fun mapNumberOfStudent(numberOfStudent: Int?): String {
        if (numberOfStudent == null) return formatMultiplatformString(Res.string.few_student.defaultText, 0)
        return if (numberOfStudent <= 1) {
            formatMultiplatformString(Res.string.few_student.defaultText, numberOfStudent)
        } else if (numberOfStudent > Constant.THRESHOLD_THOUSANDS) {
            formatMultiplatformString(
                Res.string.thousand_students.defaultText,
                numberOfStudent / Constant.THRESHOLD_THOUSANDS,
            )
        } else {
            formatMultiplatformString(Res.string.lots_of_student.defaultText, numberOfStudent)
        }
    }
}
