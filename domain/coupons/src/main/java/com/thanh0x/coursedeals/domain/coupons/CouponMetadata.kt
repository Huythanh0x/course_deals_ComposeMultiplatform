package com.thanh0x.coursedeals.domain.coupons

data class CouponMetadata(
    val totalCoupon: Long,
    val lastFetchTime: Long,
    val localFetchTime: Long
)
