package com.thanh0x.coursedeals.domain.model

data class FilterData(
    val categories: List<String> = emptyList(),
    val language: String? = null,
    val sortBy: String? = null
)
