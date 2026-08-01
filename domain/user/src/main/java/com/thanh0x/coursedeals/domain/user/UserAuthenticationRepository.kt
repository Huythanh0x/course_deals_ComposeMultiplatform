package com.thanh0x.coursedeals.domain.user

import com.thanh0x.coursedeals.core.common.AppResult

interface UserAuthenticationRepository {
    suspend fun login(credentials: AuthCredentials): AppResult<TokenData>
    suspend fun register(credentials: AuthCredentials): AppResult<Unit>
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
}
