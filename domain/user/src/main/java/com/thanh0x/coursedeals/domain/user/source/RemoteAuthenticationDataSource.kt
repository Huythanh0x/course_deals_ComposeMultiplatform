package com.thanh0x.coursedeals.domain.user.source

import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.domain.user.AuthCredentials
import com.thanh0x.coursedeals.domain.user.TokenData

interface RemoteAuthenticationDataSource {
    suspend fun register(credentials: AuthCredentials): AppResult<Unit>
    suspend fun login(credentials: AuthCredentials): AppResult<TokenData>
    suspend fun checkIfTokenExpired(): AppResult<Unit>
    suspend fun requestFingerprintToken(): AppResult<TokenData>
    suspend fun requestAccessToken(): AppResult<TokenData>
}
