package com.thanh0x.coursedeals.data.coupons.source.remote

import com.thanh0x.coursedeals.core.network.NetworkResponse
import com.thanh0x.coursedeals.data.coupons.model.CouponDto
import com.thanh0x.coursedeals.data.coupons.model.CouponRequestDto
import com.thanh0x.coursedeals.data.coupons.model.CourseDealResponse
import com.thanh0x.coursedeals.data.coupons.model.ResponseStatusFromServer
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class CouponServiceImpl(
    private val httpClient: HttpClient
) : CouponService {

    private suspend inline fun <reified T> safeRequest(
        block: () -> HttpResponse
    ): NetworkResponse<T> {
        return try {
            val response = block()
            if (response.status.isSuccess()) {
                val parsedBody: T = response.body()
                NetworkResponse.success(
                    body = parsedBody,
                    code = response.status.value,
                    message = response.status.description
                )
            } else {
                NetworkResponse.error(
                    code = response.status.value,
                    message = response.status.description
                )
            }
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            NetworkResponse.error(
                code = -1,
                message = e.message ?: "Unknown Error"
            )
        }
    }

    override suspend fun fetchAllCoupons(): NetworkResponse<CourseDealResponse> {
        return safeRequest {
            httpClient.get("/api/v1/coupons")
        }
    }

    override suspend fun fetchPagedCoupons(
        pageIndex: Int,
        numberPerPage: Int
    ): NetworkResponse<CourseDealResponse> {
        return safeRequest {
            httpClient.get("/api/v1/coupons") {
                parameter("pageIndex", pageIndex)
                parameter("numberPerPage", numberPerPage)
            }
        }
    }

    override suspend fun fetchCouponDetail(courseId: Int): NetworkResponse<CouponDto> {
        return safeRequest {
            httpClient.get("/api/v1/coupons/$courseId")
        }
    }

    override suspend fun fetchEnhancedCouponDetail(courseId: Int): NetworkResponse<CouponDto> {
        return safeRequest {
            httpClient.get("/api/v1/coupons/$courseId/details")
        }
    }

    override suspend fun searchCourseCoupon(
        query: String,
        pageIndex: Int,
        numberPerPage: Int
    ): NetworkResponse<CourseDealResponse> {
        return safeRequest {
            httpClient.get("/api/v1/coupons/search") {
                parameter("query", query)
                parameter("pageIndex", pageIndex)
                parameter("numberPerPage", numberPerPage)
            }
        }
    }

    override suspend fun postCoupon(couponUrl: String): NetworkResponse<ResponseStatusFromServer> {
        return safeRequest {
            httpClient.post("/api/v1/coupons") {
                contentType(ContentType.Application.Json)
                setBody(CouponRequestDto(couponUrl = couponUrl))
            }
        }
    }

    override suspend fun deleteCoupon(couponUrl: String): NetworkResponse<ResponseStatusFromServer> {
        return safeRequest {
            httpClient.delete("/api/v1/coupons") {
                contentType(ContentType.Application.Json)
                setBody(CouponRequestDto(couponUrl = couponUrl))
            }
        }
    }
}
