package com.thanh0x.coursedeals.feature.detail

import com.thanh0x.coursedeals.domain.coupons.Coupon

data class CouponDetailUiState(
    val isLoading: Boolean = false,
    val coupon: Coupon? = null,
    val error: String? = null,
    val isInternetAvailable: Boolean = true,
    val isFromCache: Boolean = false,
    val cachedAt: Long? = null,
    val isReporting: Boolean = false,
    val reportSuccess: Boolean = false,
    val reportError: String? = null,
)
