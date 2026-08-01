package com.thanh0x.coursedeals.data.coupons.source.remote

import com.thanh0x.coursedeals.data.coupons.model.CouponDto
import com.thanh0x.coursedeals.data.coupons.model.ResponseStatusFromServer
import com.thanh0x.coursedeals.data.coupons.model.CourseDealResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CouponService {
    @GET("/api/v1/coupons")
    suspend fun fetchAllCoupons(): Response<CourseDealResponse>

    @GET("/api/v1/coupons")
    suspend fun fetchPagedCoupons(
        @Query(value = "pageIndex") pageIndex: Int,
        @Query(value = "numberPerPage") numberPerPage: Int = 10
    ): Response<CourseDealResponse>

    @GET("/api/v1/coupons/{courseId}")
    suspend fun fetchCouponDetail(
        @Path(value = "courseId") courseId: Int,
    ): Response<CouponDto>

    @GET("/api/v1/coupons/{courseId}/details")
    suspend fun fetchEnhancedCouponDetail(
        @Path(value = "courseId") courseId: Int,
    ): Response<CouponDto>

    @GET("/api/v1/coupons/search")
    suspend fun searchCourseCoupon(
        @Query("query") query: String,
        @Query(value = "pageIndex") pageIndex: Int = 0,
        @Query(value = "numberPerPage") numberPerPage: Int = 10
    ): Response<CourseDealResponse>

    @POST("api/v1/coupons")
    suspend fun postCoupon(@Body couponUrl: String): Response<ResponseStatusFromServer>

    @DELETE("api/v1/coupons")
    suspend fun deleteCoupon(@Body couponUrl: String): Response<ResponseStatusFromServer>
}
