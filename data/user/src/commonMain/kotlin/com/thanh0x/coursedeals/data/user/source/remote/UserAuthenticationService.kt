package com.thanh0x.coursedeals.data.user.source.remote

import com.thanh0x.coursedeals.core.network.NetworkResponse
import com.thanh0x.coursedeals.data.user.model.PostAuthenticationData
import com.thanh0x.coursedeals.data.user.model.RefreshTokenRequest
import com.thanh0x.coursedeals.data.user.model.RegisterRequest
import com.thanh0x.coursedeals.data.user.model.RegisterResponseData
import com.thanh0x.coursedeals.data.user.model.SocialLoginRequest
import com.thanh0x.coursedeals.data.user.model.TokenResponseData
import com.thanh0x.coursedeals.data.user.model.VerifyOtpRequest

interface UserAuthenticationService {
    suspend fun login(postAuthenticationData: PostAuthenticationData): NetworkResponse<TokenResponseData>

    suspend fun socialLogin(request: SocialLoginRequest): NetworkResponse<TokenResponseData>

    suspend fun register(request: RegisterRequest): NetworkResponse<RegisterResponseData>

    suspend fun verifyOtp(request: VerifyOtpRequest): NetworkResponse<TokenResponseData>

    suspend fun refreshToken(request: RefreshTokenRequest): NetworkResponse<TokenResponseData>

    suspend fun checkIfTokeExpired(): NetworkResponse<Unit>

    suspend fun requestFingerprintToken(): NetworkResponse<TokenResponseData>

    suspend fun requestAccessToken(): NetworkResponse<TokenResponseData>
}
