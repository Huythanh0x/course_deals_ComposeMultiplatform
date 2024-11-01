package com.batdaulaptrinh.freeudemycoupons.domain.repository

import com.batdaulaptrinh.freeudemycoupons.data.model.Coupon
import com.batdaulaptrinh.freeudemycoupons.data.model.ResponseStatusFromServer
import com.batdaulaptrinh.freeudemycoupons.data.source.remote.RemotePagingCouponDataSourceImpl
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
