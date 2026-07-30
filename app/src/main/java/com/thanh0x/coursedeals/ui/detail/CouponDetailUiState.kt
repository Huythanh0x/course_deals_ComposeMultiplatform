package com.thanh0x.coursedeals.ui.detail

import com.thanh0x.coursedeals.domain.model.Coupon

data class CouponDetailUiState(
    val isLoading: Boolean = false,
    val coupon: Coupon? = null,
    val error: String? = null,
    val isInternetAvailable: Boolean = true
)
