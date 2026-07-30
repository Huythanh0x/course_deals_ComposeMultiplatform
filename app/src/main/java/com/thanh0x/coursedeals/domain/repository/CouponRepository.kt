package com.thanh0x.coursedeals.domain.repository

import androidx.paging.PagingSource
import com.thanh0x.coursedeals.domain.model.AppResult
import com.thanh0x.coursedeals.domain.model.Coupon

interface CouponRepository {
    suspend fun getAllCoupons(): List<Coupon>

    suspend fun insertCoupon(coupon: Coupon)

    suspend fun queryCouponByName(query: String): List<Coupon>

    suspend fun clearALlCoupons(): Int

    suspend fun requestPostANewCoupon(couponUrl: String): AppResult<Unit>

    suspend fun requestDeleteACoupon(couponUrl: String): AppResult<Unit>

    fun getRemotePagingCouponSource(): PagingSource<Int, Coupon>

    suspend fun fetchCouponDetail(courseId: Int): AppResult<Coupon>
}
