package com.thanh0x.coursedeals.domain.source

import com.thanh0x.coursedeals.data.model.Coupon
import com.thanh0x.coursedeals.data.model.ResponseStatusFromServer
import retrofit2.Response

interface RemoteCouponDataSource {
    suspend fun requestPostANewCoupon(couponUrl: String): Response<ResponseStatusFromServer>

    suspend fun requestDeleteACoupon(couponUrl: String): Response<ResponseStatusFromServer>

    suspend fun fetchCouponDetail(courseId: Int): Response<Coupon>
}
