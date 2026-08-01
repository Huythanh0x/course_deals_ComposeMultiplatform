package com.thanh0x.coursedeals.util

import com.thanh0x.coursedeals.BuildConfig

object Constant {
    const val COUPON_TABLE_NAME = "coupon_table_name"
    const val COUPON_DATABASE_NAME = "coupon_database_name"
    const val BASE_URL_API = BuildConfig.BASE_URL_API
    const val DATA_STORE_NAME = "data_store_name"
    const val PREFERENCES_LOGIN_TOKEN = "login_token"
    const val PREFERENCES_ENABLE_DARK_MODE = "enable_dark_mode"
    const val PREFERENCES_ENABLE_FINGERPRINT = "fingerprint"
    const val PREFERENCES_REFRESH_TOKEN = "refresh_token"
    const val PREFERENCES_FINGERPRINT_TOKEN = "fingerprint_token"
    const val PREFERENCES_SHOW_LOCAL_FETCH_TIME = "show_local_fetch_time"
    const val PREFERENCES_FAV_CATEGORIES = "fav_categories"
    const val PREFERENCES_FAV_KEYWORDS = "fav_keywords"
    const val PREFERENCES_NOTIFICATIONS_ENABLED = "notifications_enabled"
    const val NETWORK_AUTHORIZATION_HEADER = "Authorization"
    const val NETWORK_BEARER_PREFIX = "Bearer"
    const val ITEMS_PER_PAGE = 10
    const val SPLASH_LOADING_TIME_MS = 1500L
    const val MY_COURSE_URL = "https://www.udemy.com/home/my-courses/learning"

    const val MINUTES_IN_HOUR = 60
    const val HOURS_IN_DAY = 24
    const val DAYS_IN_MONTH = 30

    const val NETWORK_TIMEOUT_SECONDS = 30L
    const val THRESHOLD_THOUSANDS = 1000
    const val DATE_ARRAY_MIN_SIZE = 3
    const val KEY_SIZE_256 = 256
    const val GCM_TAG_LENGTH = 128
}
