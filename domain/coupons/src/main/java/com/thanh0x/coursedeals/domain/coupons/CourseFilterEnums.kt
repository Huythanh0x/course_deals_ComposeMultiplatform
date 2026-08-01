package com.thanh0x.coursedeals.domain.coupons

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class CourseCategory(val dbValue: String) : Parcelable {
    DEVELOPMENT("Development"),
    DESIGN("Design"),
    BUSINESS("Business"),
    IT_AND_SOFTWARE("IT & Software"),
    MARKETING("Marketing"),
    PERSONAL_DEVELOPMENT("Personal Development"),
    PHOTOGRAPHY("Photography"),
    MUSIC("Music"),
    HEALTH_AND_FITNESS("Health & Fitness"),
    FINANCE("Finance"),
}

@Parcelize
enum class CourseLanguage : Parcelable {
    ALL,
    ENGLISH,
    OTHERS,
}

@Parcelize
enum class SortOption : Parcelable {
    NEWEST,
    RATING,
    STUDENTS,
    REVIEWS,
    EXPIRING_SOON,
}
