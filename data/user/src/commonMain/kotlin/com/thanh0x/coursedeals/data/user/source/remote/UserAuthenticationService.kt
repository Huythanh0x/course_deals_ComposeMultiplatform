package com.thanh0x.coursedeals.data.user.source.remote

import com.thanh0x.coursedeals.core.network.NetworkResponse
import com.thanh0x.coursedeals.core.network.openapi.model.AuthResponseDTO
import com.thanh0x.coursedeals.core.network.openapi.model.EmailLoginRequestDTO
import com.thanh0x.coursedeals.core.network.openapi.model.RefreshTokenRequestDTO
import com.thanh0x.coursedeals.core.network.openapi.model.RegisterRequestDTO
import com.thanh0x.coursedeals.core.network.openapi.model.RegisterResponseDTO
import com.thanh0x.coursedeals.core.network.openapi.model.SendOtpRequestDTO
import com.thanh0x.coursedeals.core.network.openapi.model.SocialLoginRequestDTO
import com.thanh0x.coursedeals.core.network.openapi.model.VerifyOtpRequestDTO

interface UserAuthenticationService {
    suspend fun login(postAuthenticationData: EmailLoginRequestDTO): NetworkResponse<AuthResponseDTO>

    suspend fun socialLogin(request: SocialLoginRequestDTO): NetworkResponse<AuthResponseDTO>

    suspend fun register(request: RegisterRequestDTO): NetworkResponse<RegisterResponseDTO>

    suspend fun sendOtp(request: SendOtpRequestDTO): NetworkResponse<RegisterResponseDTO>

    suspend fun verifyOtp(request: VerifyOtpRequestDTO): NetworkResponse<AuthResponseDTO>

    suspend fun refreshToken(request: RefreshTokenRequestDTO): NetworkResponse<AuthResponseDTO>

    suspend fun checkIfTokeExpired(): NetworkResponse<Unit>

    suspend fun requestFingerprintToken(): NetworkResponse<AuthResponseDTO>

    suspend fun requestAccessToken(): NetworkResponse<AuthResponseDTO>
}
