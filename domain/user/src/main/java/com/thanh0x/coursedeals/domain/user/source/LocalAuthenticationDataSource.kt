package com.thanh0x.coursedeals.domain.user.source

interface LocalAuthenticationDataSource {
    suspend fun getLocalToken(): String?
    suspend fun saveLocalToken(newLoginToken: String)
    suspend fun clearLocalToken()
    suspend fun getCipherTextWrapper(): String?
    suspend fun saveCipherTextWrapper(newLoginToken: String)
    suspend fun clearCipherTextWrapper()
    suspend fun getLocalRefreshToken(): String?
    suspend fun saveLocalRefreshToken(newLoginToken: String)
    suspend fun clearLocalRefreshToken()
}
