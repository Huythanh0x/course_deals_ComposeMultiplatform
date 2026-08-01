package com.thanh0x.coursedeals.data.repository

import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.domain.user.AuthCredentials
import com.thanh0x.coursedeals.domain.user.TokenData
import com.thanh0x.coursedeals.domain.user.UserAuthenticationRepository
import com.thanh0x.coursedeals.domain.source.LocalAuthenticationDataSource
import com.thanh0x.coursedeals.domain.source.RemoteAuthenticationDataSource
import javax.inject.Inject

class UserAuthenticationRepositoryImpl @Inject constructor(
    private val localAuthenticationDataSource: LocalAuthenticationDataSource,
    private val remoteAuthenticationDataSource: RemoteAuthenticationDataSource
) : UserAuthenticationRepository {
    override suspend fun login(credentials: AuthCredentials): AppResult<TokenData> =
        remoteAuthenticationDataSource.login(credentials)

    override suspend fun register(credentials: AuthCredentials): AppResult<Unit> =
        remoteAuthenticationDataSource.register(credentials)

    override suspend fun getLocalToken(): String? =
        localAuthenticationDataSource.getLocalToken()

    override suspend fun saveLocalToken(token: String) =
        localAuthenticationDataSource.saveLocalToken(token)

    override suspend fun clearLocalToken() {
        localAuthenticationDataSource.clearLocalToken()
    }

    override suspend fun checkIfTokenExpired(): AppResult<Unit> = remoteAuthenticationDataSource.checkIfTokenExpired()

    override suspend fun getCipherTextWrapper() =
        localAuthenticationDataSource.getCipherTextWrapper()

    override suspend fun saveCipherTextWrapper(fingerprintToken: String) =
        localAuthenticationDataSource.saveCipherTextWrapper(fingerprintToken)

    override suspend fun clearCipherTextWrapper() =
        localAuthenticationDataSource.clearCipherTextWrapper()

    override suspend fun getLocalRefreshToken(): String? =
        localAuthenticationDataSource.getLocalRefreshToken()

    override suspend fun saveLocalRefreshToken(refreshToken: String) =
        localAuthenticationDataSource.saveLocalRefreshToken(refreshToken)

    override suspend fun clearLocalRefreshToken() =
        localAuthenticationDataSource.clearLocalRefreshToken()

    override suspend fun requestFingerprintToken() = remoteAuthenticationDataSource.requestFingerprintToken()

    override suspend fun requestAccessToken() = remoteAuthenticationDataSource.requestAccessToken()
}
