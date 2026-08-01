package com.thanh0x.coursedeals.domain.coupons

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class CourseCategory(val dbValue: String, val displayResId: Int) : Parcelable {
    DEVELOPMENT("Development", 0),
    DESIGN("Design", 0),
    BUSINESS("Business", 0),
    IT_AND_SOFTWARE("IT & Software", 0),
    MARKETING("Marketing", 0),
    PERSONAL_DEVELOPMENT("Personal Development", 0),
    PHOTOGRAPHY("Photography", 0),
    MUSIC("Music", 0),
    HEALTH_AND_FITNESS("Health & Fitness", 0),
    FINANCE("Finance", 0),
}

@Parcelize
enum class CourseLanguage(val displayResId: Int) : Parcelable {
    ALL(0),
    ENGLISH(0),
    OTHERS(0),
}

@Parcelize
enum class SortOption(val displayResId: Int) : Parcelable {
    NEWEST(0),
    RATING(0),
    STUDENTS(0),
    REVIEWS(0),
    EXPIRING_SOON(0),
}
