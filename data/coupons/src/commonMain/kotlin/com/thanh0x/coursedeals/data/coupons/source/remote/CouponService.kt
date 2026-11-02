package com.thanh0x.coursedeals.data.coupons.source.remote

import com.thanh0x.coursedeals.core.network.NetworkResponse
import com.thanh0x.coursedeals.core.network.openapi.model.CouponDetailDTO
import com.thanh0x.coursedeals.core.network.openapi.model.CourseReportRequest
import com.thanh0x.coursedeals.core.network.openapi.model.PagedCouponResponseDTO

interface CouponService {
    suspend fun fetchAllCoupons(): NetworkResponse<PagedCouponResponseDTO>

    suspend fun fetchPagedCoupons(
        pageIndex: Int,
        numberPerPage: Int = 10
    ): NetworkResponse<PagedCouponResponseDTO>

    suspend fun fetchCouponDetail(
        courseId: Int
    ): NetworkResponse<CouponDetailDTO>

    suspend fun fetchEnhancedCouponDetail(
        courseId: Int
    ): NetworkResponse<CouponDetailDTO>

    suspend fun searchCourseCoupon(
        query: String,
        pageIndex: Int = 0,
        numberPerPage: Int = 10
    ): NetworkResponse<PagedCouponResponseDTO>

    suspend fun postCoupon(couponUrl: String): NetworkResponse<Unit>

    suspend fun deleteCoupon(couponUrl: String): NetworkResponse<Unit>

    suspend fun reportCourse(
        courseId: Int,
        request: CourseReportRequest,
    ): NetworkResponse<Unit>
}
