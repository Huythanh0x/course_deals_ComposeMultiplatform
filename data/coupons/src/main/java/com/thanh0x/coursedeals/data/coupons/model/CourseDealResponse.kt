package com.thanh0x.coursedeals.data.coupons.model

import com.google.gson.annotations.SerializedName

data class CourseDealResponse(
    @SerializedName("lastFetchTime")
    val lastFetchTime: Long,
    @SerializedName("totalCoupon")
    val totalCoupon: Long,
    @SerializedName("totalPage")
    val totalPage: Int,
    @SerializedName("currentPage")
    val currentPage: Int,
    @SerializedName("courses")
    val courses: List<CouponDto>
)
