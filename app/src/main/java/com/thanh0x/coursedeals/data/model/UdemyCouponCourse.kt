package com.batdaulaptrinh.freeudemycoupons.data.model

data class UdemyCouponCourse(
    val lastFetchTime: String,
    val totalCoupon: Long,
    val totalPage: Int,
    val currentPage: Int,
    val courses: List<Coupon>
)