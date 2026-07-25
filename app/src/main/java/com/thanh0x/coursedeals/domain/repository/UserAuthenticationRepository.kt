package com.batdaulaptrinh.freeudemycoupons.domain.repository

import com.batdaulaptrinh.freeudemycoupons.data.model.PostAuthenticationData
import com.batdaulaptrinh.freeudemycoupons.data.model.TokenResponseData
import retrofit2.Response


interface UserAuthenticationRepository {
    suspend fun login(postAuthenticationData: PostAuthenticationData): Response<TokenResponseData>
    suspend fun register(postAuthenticationData: PostAuthenticationData): Response<Any>
    suspend fun getLocalToken(): String?
    suspend fun saveLocalToken(newLoginToken: String)
    suspend fun clearLocalToken()
    suspend fun checkIfTokeExpired(): Response<Any>
    suspend fun getCipherTextWrapper(): String?
    suspend fun saveCipherTextWrapper(fingerprintToken: String)
    suspend fun clearCipherTextWrapper()
    suspend fun getLocalRefreshToken(): String?
    suspend fun saveLocalRefreshToken(refreshToken: String)
    suspend fun clearLocalRefreshToken()
    suspend fun requestFingerprintToken(): Response<TokenResponseData>
    suspend fun requestAccessToken(): Response<TokenResponseData>
}
