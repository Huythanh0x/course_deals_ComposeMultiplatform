package com.thanh0x.coursedeals.data.user.source.remote

import com.thanh0x.coursedeals.core.network.NetworkResponse
import com.thanh0x.coursedeals.core.network.openapi.model.AuthResponseDTO
import com.thanh0x.coursedeals.core.network.openapi.model.EmailLoginRequestDTO
import com.thanh0x.coursedeals.core.network.openapi.model.RefreshTokenRequestDTO
import com.thanh0x.coursedeals.core.network.openapi.model.RegisterRequestDTO
import com.thanh0x.coursedeals.core.network.openapi.model.RegisterResponseDTO
import com.thanh0x.coursedeals.core.network.openapi.model.SendOtpRequestDTO
import com.thanh0x.coursedeals.core.network.openapi.model.SocialLoginRequestDTO
import com.thanh0x.coursedeals.core.network.openapi.model.VerifyOtpRequestDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class UserAuthenticationServiceImpl(
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
                message = e.message ?: "Unknown Error"
            )
        }
    }

    override suspend fun login(postAuthenticationData: EmailLoginRequestDTO): NetworkResponse<AuthResponseDTO> {
        return safeRequest {
            httpClient.post("/api/v1/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(postAuthenticationData)
            }
        }
    }

    override suspend fun socialLogin(request: SocialLoginRequestDTO): NetworkResponse<AuthResponseDTO> {
        return safeRequest {
            httpClient.post("/api/v1/auth/social/login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    override suspend fun register(request: RegisterRequestDTO): NetworkResponse<RegisterResponseDTO> {
        return safeRequest {
            httpClient.post("/api/v1/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    override suspend fun sendOtp(request: SendOtpRequestDTO): NetworkResponse<RegisterResponseDTO> {
        return safeRequest {
            httpClient.post("/api/v1/auth/send-otp") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    override suspend fun verifyOtp(request: VerifyOtpRequestDTO): NetworkResponse<AuthResponseDTO> {
        return safeRequest {
            httpClient.post("/api/v1/auth/verify-otp") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    override suspend fun refreshToken(request: RefreshTokenRequestDTO): NetworkResponse<AuthResponseDTO> {
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
            NetworkResponse.error(-1, e.message ?: "Unknown Error")
        }
    }

    override suspend fun requestFingerprintToken(): NetworkResponse<AuthResponseDTO> {
        return safeRequest {
            httpClient.get("/api/v1/auth/fingerprint-token")
        }
    }

    override suspend fun requestAccessToken(): NetworkResponse<AuthResponseDTO> {
        return safeRequest {
            httpClient.get("/api/v1/auth/access-token")
        }
    }
}
