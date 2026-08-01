package com.thanh0x.coursedeals.domain.source

import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.domain.coupons.Coupon

interface RemoteCouponDataSource {
    suspend fun requestPostANewCoupon(couponUrl: String): AppResult<Unit>

    suspend fun requestDeleteACoupon(couponUrl: String): AppResult<Unit>

    suspend fun fetchCouponDetail(courseId: Int): AppResult<Coupon>
}
