package com.thanh0x.coursedeals.data.model

data class CourseDealResponse(
    val lastFetchTime: String,
    val totalCoupon: Long,
    val totalPage: Int,
    val currentPage: Int,
    val courses: List<Coupon>
)