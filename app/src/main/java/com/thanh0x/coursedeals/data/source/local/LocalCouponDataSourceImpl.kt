package com.thanh0x.coursedeals.data.source.local

import com.thanh0x.coursedeals.data.mapper.toDomain
import com.thanh0x.coursedeals.data.mapper.toEntity
import com.thanh0x.coursedeals.domain.model.Coupon
import com.thanh0x.coursedeals.domain.source.LocalCouponDataSource
import javax.inject.Inject

class LocalCouponDataSourceImpl @Inject constructor(private val couponDao: CouponDao) :
    LocalCouponDataSource {
    override suspend fun getAllCoupons(): List<Coupon> = couponDao.getAllCoupons().map { it.toDomain() }

    override suspend fun insertCoupon(coupon: Coupon) = couponDao.insertCoupon(coupon.toEntity())

    override suspend fun insertCoupons(coupons: List<Coupon>) = couponDao.insertCoupons(coupons.map { it.toEntity() })

    override suspend fun queryCouponByName(query: String): List<Coupon> =
        couponDao.queryCouponByName(query).map { it.toDomain() }

    override suspend fun clearALlCoupons() = couponDao.clearAllCoupons()
}
