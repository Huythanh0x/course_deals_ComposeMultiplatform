package com.thanh0x.coursedeals.domain.model

import com.thanh0x.coursedeals.R

enum class CourseCategory(val dbValue: String, val displayResId: Int) {
    DEVELOPMENT("Development", R.string.cat_1),
    DESIGN("Design", R.string.cat_2),
    BUSINESS("Business", R.string.cat_3),
    IT_AND_SOFTWARE("IT & Software", R.string.cat_4),
    MARKETING("Marketing", R.string.cat_5),
    PERSONAL_DEVELOPMENT("Personal Development", R.string.cat_6),
    PHOTOGRAPHY("Photography", R.string.cat_7),
    MUSIC("Music", R.string.cat_8),
    HEALTH_AND_FITNESS("Health & Fitness", R.string.cat_9),
    FINANCE("Finance", R.string.cat_10)
}

enum class CourseLanguage(val displayResId: Int) {
    ALL(R.string.lang_all),
    ENGLISH(R.string.lang_english),
    OTHERS(R.string.lang_others)
}

enum class SortOption(val displayResId: Int) {
    NEWEST(R.string.sort_newest),
    RATING(R.string.sort_rating),
    STUDENTS(R.string.sort_students),
    REVIEWS(R.string.sort_reviews),
    EXPIRING_SOON(R.string.sort_expiring)
}
