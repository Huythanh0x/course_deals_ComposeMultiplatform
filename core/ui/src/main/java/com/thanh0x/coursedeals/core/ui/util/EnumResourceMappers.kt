package com.thanh0x.coursedeals.core.ui.util

import com.thanh0x.coursedeals.core.ui.R
import com.thanh0x.coursedeals.core.ui.resources.Res
import com.thanh0x.coursedeals.core.ui.resources.StringResource
import com.thanh0x.coursedeals.domain.coupons.CourseCategory
import com.thanh0x.coursedeals.domain.coupons.CourseLanguage
import com.thanh0x.coursedeals.domain.coupons.SortOption

val CourseCategory.categoryResId: Int
    get() = when (this) {
        CourseCategory.DEVELOPMENT -> R.string.cat_1
        CourseCategory.DESIGN -> R.string.cat_2
        CourseCategory.BUSINESS -> R.string.cat_3
        CourseCategory.IT_AND_SOFTWARE -> R.string.cat_4
        CourseCategory.MARKETING -> R.string.cat_5
        CourseCategory.PERSONAL_DEVELOPMENT -> R.string.cat_6
        CourseCategory.PHOTOGRAPHY -> R.string.cat_7
        CourseCategory.MUSIC -> R.string.cat_8
        CourseCategory.HEALTH_AND_FITNESS -> R.string.cat_9
        CourseCategory.FINANCE -> R.string.cat_10
    }

val CourseCategory.categoryResource: StringResource
    get() = when (this) {
        CourseCategory.DEVELOPMENT -> Res.string.cat_1
        CourseCategory.DESIGN -> Res.string.cat_2
        CourseCategory.BUSINESS -> Res.string.cat_3
        CourseCategory.IT_AND_SOFTWARE -> Res.string.cat_4
        CourseCategory.MARKETING -> Res.string.cat_5
        CourseCategory.PERSONAL_DEVELOPMENT -> Res.string.cat_6
        CourseCategory.PHOTOGRAPHY -> Res.string.cat_7
        CourseCategory.MUSIC -> Res.string.cat_8
        CourseCategory.HEALTH_AND_FITNESS -> Res.string.cat_9
        CourseCategory.FINANCE -> Res.string.cat_10
    }

val CourseLanguage.languageResId: Int
    get() = when (this) {
        CourseLanguage.ALL -> R.string.lang_all
        CourseLanguage.ENGLISH -> R.string.lang_english
        CourseLanguage.OTHERS -> R.string.lang_others
    }

val CourseLanguage.languageResource: StringResource
    get() = when (this) {
        CourseLanguage.ALL -> Res.string.lang_all
        CourseLanguage.ENGLISH -> Res.string.lang_english
        CourseLanguage.OTHERS -> Res.string.lang_others
    }

val SortOption.sortResId: Int
    get() = when (this) {
        SortOption.NEWEST -> R.string.sort_newest
        SortOption.RATING -> R.string.sort_rating
        SortOption.STUDENTS -> R.string.sort_students
        SortOption.REVIEWS -> R.string.sort_reviews
        SortOption.EXPIRING_SOON -> R.string.sort_expiring
    }

val SortOption.sortResource: StringResource
    get() = when (this) {
        SortOption.NEWEST -> Res.string.sort_newest
        SortOption.RATING -> Res.string.sort_rating
        SortOption.STUDENTS -> Res.string.sort_students
        SortOption.REVIEWS -> Res.string.sort_reviews
        SortOption.EXPIRING_SOON -> Res.string.sort_expiring
    }

