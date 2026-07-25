package com.batdaulaptrinh.freeudemycoupons.domain.source

import com.batdaulaptrinh.freeudemycoupons.data.model.Coupon

interface LocalCouponDataSource {
    suspend fun getAllCoupons(): List<Coupon>

    suspend fun insertCoupon(coupon: Coupon)

    suspend fun insertCoupons(coupons: List<Coupon>)

    suspend fun queryCouponByName(query: String): List<Coupon>

    suspend fun clearALlCoupons(): Int
}