package com.thanh0x.coursedeals.domain.user

import com.thanh0x.coursedeals.core.common.AppResult

@Suppress("TooManyFunctions")
interface UserAuthenticationRepository {
    suspend fun login(credentials: AuthCredentials): AppResult<TokenData>
    suspend fun register(email: String, password: String, fullName: String? = null): AppResult<String>
    suspend fun sendOtp(email: String): AppResult<String>
    suspend fun verifyOtp(email: String, otp: String): AppResult<TokenData>
    suspend fun refreshToken(refreshToken: String): AppResult<TokenData>
    suspend fun getLocalToken(): String?
    suspend fun saveLocalToken(token: String)
    suspend fun clearLocalToken()
    suspend fun checkIfTokenExpired(): AppResult<Unit>
    suspend fun getCipherTextWrapper(): String?
    suspend fun saveCipherTextWrapper(fingerprintToken: String)
    suspend fun clearCipherTextWrapper()
    suspend fun getLocalRefreshToken(): String?
    suspend fun saveLocalRefreshToken(refreshToken: String)
    suspend fun clearLocalRefreshToken()
    suspend fun requestFingerprintToken(): AppResult<TokenData>
    suspend fun requestAccessToken(): AppResult<TokenData>
    suspend fun socialLogin(provider: String, idToken: String): AppResult<TokenData>
}
