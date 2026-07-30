package com.thanh0x.coursedeals.data.source.remote

import com.thanh0x.coursedeals.data.mapper.toDomain
import com.thanh0x.coursedeals.data.mapper.toDto
import com.thanh0x.coursedeals.domain.model.AppResult
import com.thanh0x.coursedeals.domain.model.AuthCredentials
import com.thanh0x.coursedeals.domain.model.TokenData
import com.thanh0x.coursedeals.domain.source.RemoteAuthenticationDataSource

class RemoteAuthenticationDataSourceImpl(private val userAuthenticationService: UserAuthenticationService) :
    RemoteAuthenticationDataSource {
    override suspend fun register(credentials: AuthCredentials): AppResult<Unit> {
        return try {
            val response = userAuthenticationService.register(credentials.toDto())
            if (response.isSuccessful) {
                AppResult.Success(Unit)
            } else {
                AppResult.Error(response.message(), response.code())
            }
        } catch (e: Exception) {
            AppResult.Error(e.localizedMessage ?: "Unknown Error")
        }
    }

    override suspend fun login(credentials: AuthCredentials): AppResult<TokenData> {
        return try {
            val response = userAuthenticationService.login(credentials.toDto())
            val body = response.body()
            if (response.isSuccessful && body != null) {
                AppResult.Success(body.toDomain())
            } else {
                AppResult.Error(response.message(), response.code())
            }
        } catch (e: Exception) {
            AppResult.Error(e.localizedMessage ?: "Unknown Error")
        }
    }

    override suspend fun checkIfTokenExpired(): AppResult<Unit> {
        return try {
            val response = userAuthenticationService.checkIfTokeExpired()
            if (response.isSuccessful) {
                AppResult.Success(Unit)
            } else {
                AppResult.Error(response.message(), response.code())
            }
        } catch (e: Exception) {
            AppResult.Error(e.localizedMessage ?: "Unknown Error")
        }
    }

    override suspend fun requestFingerprintToken(): AppResult<TokenData> {
        return try {
            val response = userAuthenticationService.requestFingerprintToken()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                AppResult.Success(body.toDomain())
            } else {
                AppResult.Error(response.message(), response.code())
            }
        } catch (e: Exception) {
            AppResult.Error(e.localizedMessage ?: "Unknown Error")
        }
    }

    override suspend fun requestAccessToken(): AppResult<TokenData> {
        return try {
            val response = userAuthenticationService.requestAccessToken()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                AppResult.Success(body.toDomain())
            } else {
                AppResult.Error(response.message(), response.code())
            }
        } catch (e: Exception) {
            AppResult.Error(e.localizedMessage ?: "Unknown Error")
        }
    }
}
