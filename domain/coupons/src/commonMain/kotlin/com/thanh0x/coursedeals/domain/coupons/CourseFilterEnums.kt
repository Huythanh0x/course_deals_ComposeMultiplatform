package com.thanh0x.coursedeals.domain.coupons

import kotlinx.serialization.Serializable

@Serializable
enum class CourseCategory(val dbValue: String) {
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

@Serializable
enum class CourseLanguage {
    ALL,
    ENGLISH,
    OTHERS,
}

@Serializable
enum class SortOption {
    NEWEST,
    RATING,
    STUDENTS,
    REVIEWS,
    EXPIRING_SOON,
}

