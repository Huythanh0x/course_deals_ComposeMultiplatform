package com.thanh0x.coursedeals.domain.coupons

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FilterData(
    val categories: List<CourseCategory> = emptyList(),
    val language: CourseLanguage = CourseLanguage.ALL,
    val sortBy: SortOption = SortOption.NEWEST,
    val minRating: Double = 0.0,
) : Parcelable
