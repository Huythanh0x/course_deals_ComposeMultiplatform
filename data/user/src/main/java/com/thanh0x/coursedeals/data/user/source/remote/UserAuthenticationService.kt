package com.thanh0x.coursedeals.data.user.source.remote

import com.thanh0x.coursedeals.data.user.model.PostAuthenticationData
import com.thanh0x.coursedeals.data.user.model.RefreshTokenRequest
import com.thanh0x.coursedeals.data.user.model.RegisterRequest
import com.thanh0x.coursedeals.data.user.model.RegisterResponseData
import com.thanh0x.coursedeals.data.user.model.SocialLoginRequest
import com.thanh0x.coursedeals.data.user.model.TokenResponseData
import com.thanh0x.coursedeals.data.user.model.VerifyOtpRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface UserAuthenticationService {
    @POST("/api/v1/auth/login")
    suspend fun login(@Body postAuthenticationData: PostAuthenticationData): Response<TokenResponseData>

    @POST("/api/v1/auth/social/login")
    suspend fun socialLogin(@Body request: SocialLoginRequest): Response<TokenResponseData>

    @POST("/api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponseData>

    @POST("/api/v1/auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<TokenResponseData>

    @POST("/api/v1/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<TokenResponseData>

    @GET("/api/v1/coupons")
    suspend fun checkIfTokeExpired(): Response<Any>

    @GET("/api/v1/auth/fingerprint-token")
    suspend fun requestFingerprintToken(): Response<TokenResponseData>

    @GET("/api/v1/auth/access-token")
    suspend fun requestAccessToken(): Response<TokenResponseData>
}
