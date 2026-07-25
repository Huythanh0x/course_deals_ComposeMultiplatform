package com.batdaulaptrinh.freeudemycoupons.data.source.remote

import com.batdaulaptrinh.freeudemycoupons.data.model.PostAuthenticationData
import com.batdaulaptrinh.freeudemycoupons.data.model.TokenResponseData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface UserAuthenticationService {
    @POST("/api/v1/auth/login")
    suspend fun login(@Body postAuthenticationData: PostAuthenticationData): Response<TokenResponseData>

    @POST("/api/v1/auth/register")
    suspend fun register(@Body postAuthenticationData: PostAuthenticationData): Response<Any>

    @GET("/api/v1/coupons")
    suspend fun checkIfTokeExpired(): Response<Any>

    @GET("/api/v1/auth/fingerprint-token")
    suspend fun requestFingerprintToken(): Response<TokenResponseData>

    @GET("/api/v1/auth/access-token")
    suspend fun requestAccessToken(): Response<TokenResponseData>
}
