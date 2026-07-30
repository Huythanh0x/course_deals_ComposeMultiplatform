package com.thanh0x.coursedeals.data.source.local

import androidx.paging.PagingSource
import com.thanh0x.coursedeals.data.model.Coupon
import com.thanh0x.coursedeals.data.source.LocalCouponDataSource
import javax.inject.Inject

class LocalCouponDataSourceImpl @Inject constructor(private val couponDao: CouponDao) :
    LocalCouponDataSource {
    override suspend fun getAllCoupons(): List<Coupon> = couponDao.getAllCoupons()

    override fun getPagingCoupons(): PagingSource<Int, Coupon> = couponDao.getPagingCoupons()

    override suspend fun insertCoupon(coupon: Coupon) = couponDao.insertCoupon(coupon)

    override suspend fun insertCoupons(coupons: List<Coupon>) = couponDao.insertCoupons(coupons)

    override fun queryCouponByName(query: String): PagingSource<Int, Coupon> =
        couponDao.queryCouponByName(query)

    override suspend fun clearALlCoupons() = couponDao.clearAllCoupons()
}
