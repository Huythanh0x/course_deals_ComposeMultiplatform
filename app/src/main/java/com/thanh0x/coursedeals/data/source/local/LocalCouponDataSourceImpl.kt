package com.batdaulaptrinh.freeudemycoupons.data.source.local

import com.batdaulaptrinh.freeudemycoupons.data.model.Coupon
import com.batdaulaptrinh.freeudemycoupons.domain.source.LocalCouponDataSource
import javax.inject.Inject

class LocalCouponDataSourceImpl @Inject constructor(private val couponDao: CouponDao) :
    LocalCouponDataSource {
    override suspend fun getAllCoupons(): List<Coupon> = couponDao.getAllCoupons()

    override suspend fun insertCoupon(coupon: Coupon) = couponDao.insertCoupon(coupon)

    override suspend fun insertCoupons(coupons: List<Coupon>) = couponDao.insertCoupons(coupons)

    override suspend fun queryCouponByName(query: String): List<Coupon> =
        couponDao.queryCouponByName(query)

    override suspend fun clearALlCoupons() = couponDao.clearAllCoupons()
}