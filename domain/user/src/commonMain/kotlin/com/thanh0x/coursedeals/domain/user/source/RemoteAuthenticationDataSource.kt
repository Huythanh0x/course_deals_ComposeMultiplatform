package com.thanh0x.coursedeals.domain.user.source

import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.domain.user.AuthCredentials
import com.thanh0x.coursedeals.domain.user.TokenData

interface RemoteAuthenticationDataSource {
    suspend fun register(email: String, password: String, fullName: String? = null): AppResult<String>
    suspend fun sendOtp(email: String): AppResult<String>
    suspend fun verifyOtp(email: String, otp: String): AppResult<TokenData>
    suspend fun refreshToken(refreshToken: String): AppResult<TokenData>
    suspend fun login(credentials: AuthCredentials): AppResult<TokenData>
    suspend fun checkIfTokenExpired(): AppResult<Unit>
    suspend fun requestFingerprintToken(): AppResult<TokenData>
    suspend fun requestAccessToken(): AppResult<TokenData>
    suspend fun socialLogin(provider: String, idToken: String): AppResult<TokenData>
}
