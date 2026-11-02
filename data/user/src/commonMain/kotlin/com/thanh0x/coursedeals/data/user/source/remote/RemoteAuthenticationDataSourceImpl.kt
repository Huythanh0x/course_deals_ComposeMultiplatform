package com.thanh0x.coursedeals.data.user.source.remote

import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.core.network.openapi.model.RefreshTokenRequestDTO
import com.thanh0x.coursedeals.core.network.openapi.model.RegisterRequestDTO
import com.thanh0x.coursedeals.core.network.openapi.model.SendOtpRequestDTO
import com.thanh0x.coursedeals.core.network.openapi.model.SocialLoginRequestDTO
import com.thanh0x.coursedeals.core.network.openapi.model.VerifyOtpRequestDTO
import com.thanh0x.coursedeals.data.user.mapper.toDomain
import com.thanh0x.coursedeals.data.user.mapper.toDto
import com.thanh0x.coursedeals.data.user.mapper.toSocialProvider
import com.thanh0x.coursedeals.domain.user.AuthCredentials
import com.thanh0x.coursedeals.domain.user.TokenData
import com.thanh0x.coursedeals.domain.user.source.RemoteAuthenticationDataSource

class RemoteAuthenticationDataSourceImpl(
    private val userAuthenticationService: UserAuthenticationService
) : RemoteAuthenticationDataSource {

    @Suppress("TooGenericExceptionCaught")
    override suspend fun register(
        email: String,
        password: String,
        fullName: String?
    ): AppResult<String> {
        return try {
            val response = userAuthenticationService.register(
                RegisterRequestDTO(email = email, password = password, fullName = fullName)
            )
            val body = response.body()
            if (response.isSuccessful) {
                AppResult.Success(
                    body?.message?.takeIf { it.isNotBlank() }
                        ?: body?.email?.takeIf { it.isNotBlank() }
                        ?: email
                )
            } else {
                AppResult.Error(response.message(), response.code())
            }
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unknown Error")
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun sendOtp(email: String): AppResult<String> {
        return try {
            val response = userAuthenticationService.sendOtp(SendOtpRequestDTO(email = email))
            val body = response.body()
            if (response.isSuccessful) {
                AppResult.Success(
                    body?.message?.takeIf { it.isNotBlank() }
                        ?: body?.email?.takeIf { it.isNotBlank() }
                        ?: email
                )
            } else {
                AppResult.Error(response.message(), response.code())
            }
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unknown Error")
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun verifyOtp(email: String, otp: String): AppResult<TokenData> {
        return try {
            val response = userAuthenticationService.verifyOtp(
                VerifyOtpRequestDTO(email = email, otp = otp)
            )
            val body = response.body()
            if (response.isSuccessful && body != null) {
                AppResult.Success(body.toDomain())
            } else {
                AppResult.Error(response.message(), response.code())
            }
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unknown Error")
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun refreshToken(refreshToken: String): AppResult<TokenData> {
        return try {
            val response = userAuthenticationService.refreshToken(
                RefreshTokenRequestDTO(refreshToken = refreshToken)
            )
            val body = response.body()
            if (response.isSuccessful && body != null) {
                AppResult.Success(body.toDomain())
            } else {
                AppResult.Error(response.message(), response.code())
            }
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unknown Error")
        }
    }

    @Suppress("TooGenericExceptionCaught")
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
            AppResult.Error(e.message ?: "Unknown Error")
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun checkIfTokenExpired(): AppResult<Unit> {
        return try {
            val response = userAuthenticationService.checkIfTokeExpired()
            if (response.isSuccessful) {
                AppResult.Success(Unit)
            } else {
                AppResult.Error(response.message(), response.code())
            }
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unknown Error")
        }
    }

    @Suppress("TooGenericExceptionCaught")
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
            AppResult.Error(e.message ?: "Unknown Error")
        }
    }

    @Suppress("TooGenericExceptionCaught")
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
            AppResult.Error(e.message ?: "Unknown Error")
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun socialLogin(provider: String, idToken: String): AppResult<TokenData> {
        return try {
            val response = userAuthenticationService.socialLogin(
                SocialLoginRequestDTO(
                    idToken = idToken,
                    provider = provider.toSocialProvider()
                )
            )
            val body = response.body()
            if (response.isSuccessful && body != null) {
                AppResult.Success(body.toDomain())
            } else {
                AppResult.Error(response.message(), response.code())
            }
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unknown Error")
        }
    }
}
