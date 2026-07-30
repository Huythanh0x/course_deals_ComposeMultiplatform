package com.thanh0x.coursedeals.domain.source

import com.thanh0x.coursedeals.domain.model.AppResult
import com.thanh0x.coursedeals.domain.model.Coupon

interface RemoteCouponDataSource {
    suspend fun requestPostANewCoupon(couponUrl: String): AppResult<Unit>

    suspend fun requestDeleteACoupon(couponUrl: String): AppResult<Unit>

    suspend fun fetchCouponDetail(courseId: Int): AppResult<Coupon>
}
