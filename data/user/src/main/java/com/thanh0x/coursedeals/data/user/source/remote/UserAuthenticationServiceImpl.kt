package com.thanh0x.coursedeals.data.user.source.remote

import com.thanh0x.coursedeals.core.network.NetworkResponse
import com.thanh0x.coursedeals.data.user.model.PostAuthenticationData
import com.thanh0x.coursedeals.data.user.model.RefreshTokenRequest
import com.thanh0x.coursedeals.data.user.model.RegisterRequest
import com.thanh0x.coursedeals.data.user.model.RegisterResponseData
import com.thanh0x.coursedeals.data.user.model.SocialLoginRequest
import com.thanh0x.coursedeals.data.user.model.TokenResponseData
import com.thanh0x.coursedeals.data.user.model.VerifyOtpRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import javax.inject.Inject

class UserAuthenticationServiceImpl @Inject constructor(
    private val httpClient: HttpClient
) : UserAuthenticationService {

    private suspend inline fun <reified T> safeRequest(
        block: () -> HttpResponse
    ): NetworkResponse<T> {
        return try {
            val response = block()
            if (response.status.isSuccess()) {
                val parsedBody: T = response.body()
                NetworkResponse.success(
                    body = parsedBody,
                    code = response.status.value,
                    message = response.status.description
                )
            } else {
                NetworkResponse.error(
                    code = response.status.value,
                    message = response.status.description
                )
            }
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            NetworkResponse.error(
                code = -1,
                message = e.localizedMessage ?: "Unknown Error"
            )
        }
    }

    override suspend fun login(postAuthenticationData: PostAuthenticationData): NetworkResponse<TokenResponseData> {
        return safeRequest {
            httpClient.post("/api/v1/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(postAuthenticationData)
            }
        }
    }

    override suspend fun socialLogin(request: SocialLoginRequest): NetworkResponse<TokenResponseData> {
        return safeRequest {
            httpClient.post("/api/v1/auth/social/login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    override suspend fun register(request: RegisterRequest): NetworkResponse<RegisterResponseData> {
        return safeRequest {
            httpClient.post("/api/v1/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    override suspend fun verifyOtp(request: VerifyOtpRequest): NetworkResponse<TokenResponseData> {
        return safeRequest {
            httpClient.post("/api/v1/auth/verify-otp") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    override suspend fun refreshToken(request: RefreshTokenRequest): NetworkResponse<TokenResponseData> {
        return safeRequest {
            httpClient.post("/api/v1/auth/refresh") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    override suspend fun checkIfTokeExpired(): NetworkResponse<Unit> {
        return try {
            val response = httpClient.get("/api/v1/coupons")
            if (response.status.isSuccess()) {
                NetworkResponse.success(Unit, response.status.value, response.status.description)
            } else {
                NetworkResponse.error(response.status.value, response.status.description)
            }
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            NetworkResponse.error(-1, e.localizedMessage ?: "Unknown Error")
        }
    }

    override suspend fun requestFingerprintToken(): NetworkResponse<TokenResponseData> {
        return safeRequest {
            httpClient.get("/api/v1/auth/fingerprint-token")
        }
    }

    override suspend fun requestAccessToken(): NetworkResponse<TokenResponseData> {
        return safeRequest {
            httpClient.get("/api/v1/auth/access-token")
        }
    }
}
