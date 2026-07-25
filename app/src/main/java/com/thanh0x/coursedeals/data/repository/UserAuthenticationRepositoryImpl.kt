package com.thanh0x.coursedeals.data.repository

import com.thanh0x.coursedeals.data.model.PostAuthenticationData
import com.thanh0x.coursedeals.domain.repository.UserAuthenticationRepository
import com.thanh0x.coursedeals.domain.source.LocalAuthenticationDataSource
import com.thanh0x.coursedeals.domain.source.RemoteAuthenticationDataSource
import javax.inject.Inject

class UserAuthenticationRepositoryImpl @Inject constructor(
    private val localAuthenticationDataSource: LocalAuthenticationDataSource,
    private val remoteAuthenticationDataSource: RemoteAuthenticationDataSource
) : UserAuthenticationRepository {
    override suspend fun login(postAuthenticationData: PostAuthenticationData) =
        remoteAuthenticationDataSource.login(postAuthenticationData)

    override suspend fun register(postAuthenticationData: PostAuthenticationData) =
        remoteAuthenticationDataSource.register(postAuthenticationData)

    override suspend fun getLocalToken(): String? =
        localAuthenticationDataSource.getLocalToken()

    override suspend fun saveLocalToken(newLoginToken: String) =
        localAuthenticationDataSource.saveLocalToken(newLoginToken)

    override suspend fun clearLocalToken() {
        localAuthenticationDataSource.clearLocalToken()
    }

    override suspend fun checkIfTokeExpired() = remoteAuthenticationDataSource.checkIfTokeExpired()

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
