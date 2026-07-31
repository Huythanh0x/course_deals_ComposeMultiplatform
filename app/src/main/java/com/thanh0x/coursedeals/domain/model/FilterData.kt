package com.thanh0x.coursedeals.domain.model

data class FilterData(
    val categories: List<CourseCategory> = emptyList(),
    val language: CourseLanguage = CourseLanguage.ALL,
    val sortBy: SortOption = SortOption.NEWEST
)
