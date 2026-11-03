package com.thanh0x.coursedeals.core.ui.util

import com.thanh0x.coursedeals.core.common.notification.NotificationTopic
import com.thanh0x.coursedeals.core.ui.resources.Res
import com.thanh0x.coursedeals.core.ui.resources.StringResource
import com.thanh0x.coursedeals.domain.coupons.CourseCategory
import com.thanh0x.coursedeals.domain.coupons.CourseLanguage
import com.thanh0x.coursedeals.domain.coupons.SortOption

val NotificationTopic.displayNameResource: StringResource
    get() = when (this) {
        NotificationTopic.ALL -> Res.string.notif_topic_all
        NotificationTopic.HOT_DEALS -> Res.string.notif_topic_hot
        NotificationTopic.DEVELOPMENT -> Res.string.notif_topic_development
        NotificationTopic.DESIGN -> Res.string.notif_topic_design
        NotificationTopic.BUSINESS -> Res.string.notif_topic_business
        NotificationTopic.IT_SOFTWARE -> Res.string.notif_topic_it_software
        NotificationTopic.MARKETING -> Res.string.notif_topic_marketing
        NotificationTopic.PERSONAL_DEVELOPMENT -> Res.string.notif_topic_personal_development
        NotificationTopic.PHOTOGRAPHY -> Res.string.notif_topic_photography
        NotificationTopic.MUSIC -> Res.string.notif_topic_music
        NotificationTopic.HEALTH_FITNESS -> Res.string.notif_topic_health_fitness
        NotificationTopic.FINANCE -> Res.string.notif_topic_finance
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

val CourseLanguage.languageResource: StringResource
    get() = when (this) {
        CourseLanguage.ALL -> Res.string.lang_all
        CourseLanguage.ENGLISH -> Res.string.lang_english
        CourseLanguage.OTHERS -> Res.string.lang_others
    }

val SortOption.sortResource: StringResource
    get() = when (this) {
        SortOption.NEWEST -> Res.string.sort_newest
        SortOption.RATING -> Res.string.sort_rating
        SortOption.STUDENTS -> Res.string.sort_students
        SortOption.REVIEWS -> Res.string.sort_reviews
        SortOption.EXPIRING_SOON -> Res.string.sort_expiring
    }
