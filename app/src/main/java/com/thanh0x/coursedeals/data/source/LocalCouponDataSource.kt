package com.thanh0x.coursedeals.data.source

import androidx.paging.PagingSource
import com.thanh0x.coursedeals.data.model.Coupon

interface LocalCouponDataSource {
    suspend fun getAllCoupons(): List<Coupon>

    fun getPagingCoupons(): PagingSource<Int, Coupon>

    suspend fun insertCoupon(coupon: Coupon)

    suspend fun insertCoupons(coupons: List<Coupon>)

    suspend fun queryCouponByName(query: String): List<Coupon>

    suspend fun clearALlCoupons(): Int
}
