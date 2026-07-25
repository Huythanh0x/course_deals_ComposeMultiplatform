package com.batdaulaptrinh.freeudemycoupons.domain.source

import com.batdaulaptrinh.freeudemycoupons.data.model.Coupon
import com.batdaulaptrinh.freeudemycoupons.data.model.ResponseStatusFromServer
import retrofit2.Response

interface RemoteCouponDataSource {
    suspend fun requestPostANewCoupon(couponUrl: String): Response<ResponseStatusFromServer>

    suspend fun requestDeleteACoupon(couponUrl: String): Response<ResponseStatusFromServer>

    suspend fun fetchCouponDetail(courseId: Int): Response<Coupon>
}
