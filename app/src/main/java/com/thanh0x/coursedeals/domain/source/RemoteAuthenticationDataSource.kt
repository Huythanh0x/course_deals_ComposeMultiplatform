package com.thanh0x.coursedeals.domain.source

import com.thanh0x.coursedeals.domain.model.AppResult
import com.thanh0x.coursedeals.domain.model.AuthCredentials
import com.thanh0x.coursedeals.domain.model.TokenData

interface RemoteAuthenticationDataSource {
    suspend fun register(credentials: AuthCredentials): AppResult<Unit>
    suspend fun login(credentials: AuthCredentials): AppResult<TokenData>
    suspend fun checkIfTokenExpired(): AppResult<Unit>
    suspend fun requestFingerprintToken(): AppResult<TokenData>
    suspend fun requestAccessToken(): AppResult<TokenData>
}
