package com.thanh0x.coursedeals.data.coupons.source.remote

import com.thanh0x.coursedeals.core.network.NetworkResponse
import com.thanh0x.coursedeals.data.coupons.model.CouponDto
import com.thanh0x.coursedeals.data.coupons.model.CourseDealResponse
import com.thanh0x.coursedeals.data.coupons.model.ResponseStatusFromServer

interface CouponService {
    suspend fun fetchAllCoupons(): NetworkResponse<CourseDealResponse>

    suspend fun fetchPagedCoupons(
        pageIndex: Int,
        numberPerPage: Int = 10
    ): NetworkResponse<CourseDealResponse>

    suspend fun fetchCouponDetail(
        courseId: Int
    ): NetworkResponse<CouponDto>

    suspend fun fetchEnhancedCouponDetail(
        courseId: Int
    ): NetworkResponse<CouponDto>

    suspend fun searchCourseCoupon(
        query: String,
        pageIndex: Int = 0,
        numberPerPage: Int = 10
    ): NetworkResponse<CourseDealResponse>

    suspend fun postCoupon(couponUrl: String): NetworkResponse<ResponseStatusFromServer>

    suspend fun deleteCoupon(couponUrl: String): NetworkResponse<ResponseStatusFromServer>
}
