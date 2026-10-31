package com.thanh0x.coursedeals.data.coupons.model

import kotlinx.serialization.Serializable

@Serializable
data class SearchHistory(
    val query: String,
    val timestamp: Long,
)
