package com.batdaulaptrinh.freeudemycoupons.domain.source

import com.batdaulaptrinh.freeudemycoupons.data.model.PostAuthenticationData
import com.batdaulaptrinh.freeudemycoupons.data.model.TokenResponseData
import retrofit2.Response

interface RemoteAuthenticationDataSource {
    suspend fun register(postAuthenticationData: PostAuthenticationData): Response<Any>
    suspend fun login(postAuthenticationData: PostAuthenticationData): Response<TokenResponseData>
    suspend fun checkIfTokeExpired(): Response<Any>
    suspend fun requestFingerprintToken(): Response<TokenResponseData>
    suspend fun requestAccessToken(): Response<TokenResponseData>
}
