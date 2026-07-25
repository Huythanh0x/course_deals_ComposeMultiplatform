package com.thanh0x.coursedeals.domain.repository

import com.thanh0x.coursedeals.data.model.Coupon
import com.thanh0x.coursedeals.data.model.ResponseStatusFromServer
import com.thanh0x.coursedeals.data.source.remote.RemotePagingCouponDataSourceImpl
import retrofit2.Response

interface CouponRepository {
    suspend fun getAllCoupons(): List<Coupon>

    suspend fun insertCoupon(coupon: Coupon)

    suspend fun queryCouponByName(query: String): List<Coupon>

    suspend fun clearALlCoupons(): Int

    suspend fun requestPostANewCoupon(couponUrl: String): Response<ResponseStatusFromServer>

    suspend fun requestDeleteACoupon(couponUrl: String): Response<ResponseStatusFromServer>

    fun getRemotePagingCouponSource(): RemotePagingCouponDataSourceImpl

    suspend fun fetchCouponDetail(courseId: Int): Response<Coupon>
}
