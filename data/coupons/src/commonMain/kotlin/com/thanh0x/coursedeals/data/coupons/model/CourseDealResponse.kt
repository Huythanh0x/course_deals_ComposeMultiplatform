package com.thanh0x.coursedeals.data.coupons.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CourseDealResponse(
    @SerialName("lastFetchTime")
    val lastFetchTime: Long,
    @SerialName("totalCoupon")
    val totalCoupon: Long,
    @SerialName("totalPage")
    val totalPage: Int,
    @SerialName("currentPage")
    val currentPage: Int,
    @SerialName("courses")
    val courses: List<CouponDto>
)
