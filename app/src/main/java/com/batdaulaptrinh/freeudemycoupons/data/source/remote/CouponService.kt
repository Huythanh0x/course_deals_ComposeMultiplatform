package com.batdaulaptrinh.freeudemycoupons.data.source.remote

import com.batdaulaptrinh.freeudemycoupons.data.model.Coupon
import com.batdaulaptrinh.freeudemycoupons.data.model.ResponseStatusFromServer
import com.batdaulaptrinh.freeudemycoupons.data.model.UdemyCouponCourse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CouponService {
    @GET("/api/v1/coupons")
    suspend fun fetchAllCoupons(): Response<UdemyCouponCourse>

    @GET("/api/v1/coupons")
    suspend fun fetchPagedCoupons(
        @Query(value = "pageIndex") pageIndex: Int,
        @Query(value = "numberPerPage") numberPerPage: Int = 10
    ): Response<UdemyCouponCourse>

    @GET("/api/v1/coupons/{courseId}")
    suspend fun fetchCouponDetail(
        @Path(value = "courseId") courseId: Int,
    ): Response<Coupon>

    @GET("/api/v1/coupons/search")
    suspend fun searchCourseCoupon(
        @Query("query") query: String,
        @Query(value = "pageIndex") pageIndex: Int = 0,
        @Query(value = "numberPerPage") numberPerPage: Int = 10
    ): Response<UdemyCouponCourse>

    @POST("api/v1/coupons")
    suspend fun postCoupon(@Body couponUrl: String): Response<ResponseStatusFromServer>

    @DELETE("api/v1/coupons")
    suspend fun deleteCoupon(@Body couponUrl: String): Response<ResponseStatusFromServer>
}
