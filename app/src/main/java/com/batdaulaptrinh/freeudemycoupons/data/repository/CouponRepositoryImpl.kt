package com.batdaulaptrinh.freeudemycoupons.data.repository

import com.batdaulaptrinh.freeudemycoupons.data.model.Coupon
import com.batdaulaptrinh.freeudemycoupons.data.source.remote.RemotePagingCouponDataSourceImpl
import com.batdaulaptrinh.freeudemycoupons.domain.repository.CouponRepository
import com.batdaulaptrinh.freeudemycoupons.domain.source.LocalCouponDataSource
import com.batdaulaptrinh.freeudemycoupons.domain.source.RemoteCouponDataSource
import javax.inject.Inject

class CouponRepositoryImpl @Inject constructor(
    private val localCouponDataSource: LocalCouponDataSource,
    private val remoteCouponDataSource: RemoteCouponDataSource,
    private val remotePagingCouponDataSource: RemotePagingCouponDataSourceImpl,
) : CouponRepository {
    override suspend fun getAllCoupons() = localCouponDataSource.getAllCoupons()

    override suspend fun insertCoupon(coupon: Coupon) = localCouponDataSource.insertCoupon(coupon)

    override suspend fun queryCouponByName(query: String) =
        localCouponDataSource.queryCouponByName(query)

    override suspend fun clearALlCoupons() = localCouponDataSource.clearALlCoupons()

    override suspend fun requestPostANewCoupon(couponUrl: String) =
        remoteCouponDataSource.requestPostANewCoupon(couponUrl)

    override suspend fun requestDeleteACoupon(couponUrl: String) =
        remoteCouponDataSource.requestDeleteACoupon(couponUrl)

    override fun getRemotePagingCouponSource() = remotePagingCouponDataSource

    override suspend fun fetchCouponDetail(courseId: Int) =
        remoteCouponDataSource.fetchCouponDetail(courseId)
}
