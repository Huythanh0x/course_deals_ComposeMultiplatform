package com.thanh0x.coursedeals.data.coupons.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CouponRequestDto(
    @SerialName("couponUrl")
    val couponUrl: String
)
