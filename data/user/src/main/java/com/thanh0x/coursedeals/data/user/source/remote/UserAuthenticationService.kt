package com.thanh0x.coursedeals.data.user.source.remote

import com.thanh0x.coursedeals.data.user.model.PostAuthenticationData
import com.thanh0x.coursedeals.data.user.model.SocialLoginRequest
import com.thanh0x.coursedeals.data.user.model.TokenResponseData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface UserAuthenticationService {
    @POST("/api/v1/auth/login")
    @Deprecated("Use social login instead")
    suspend fun login(@Body postAuthenticationData: PostAuthenticationData): Response<TokenResponseData>

    @POST("/api/v1/auth/social/login")
    suspend fun socialLogin(@Body request: SocialLoginRequest): Response<TokenResponseData>

    @POST("/api/v1/auth/register")
    @Deprecated("Use social login instead")
    suspend fun register(@Body postAuthenticationData: PostAuthenticationData): Response<Any>

    @GET("/api/v1/coupons")
    suspend fun checkIfTokeExpired(): Response<Any>

    @GET("/api/v1/auth/fingerprint-token")
    suspend fun requestFingerprintToken(): Response<TokenResponseData>

    @GET("/api/v1/auth/access-token")
    suspend fun requestAccessToken(): Response<TokenResponseData>
}
