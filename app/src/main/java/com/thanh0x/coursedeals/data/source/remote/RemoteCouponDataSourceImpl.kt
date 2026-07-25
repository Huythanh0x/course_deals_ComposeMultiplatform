package com.thanh0x.coursedeals.data.source.remote

import com.thanh0x.coursedeals.domain.source.RemoteCouponDataSource

class RemoteCouponDataSourceImpl(private val couponService: CouponService) :
    RemoteCouponDataSource {
    override suspend fun requestPostANewCoupon(couponUrl: String) =
        couponService.postCoupon(couponUrl)

    override suspend fun requestDeleteACoupon(couponUrl: String) =
        couponService.deleteCoupon(couponUrl)

    override suspend fun fetchCouponDetail(courseId: Int) =
        couponService.fetchCouponDetail(courseId)
}
