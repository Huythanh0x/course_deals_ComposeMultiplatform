package com.thanh0x.coursedeals.data.user.source.remote

import com.thanh0x.coursedeals.core.network.NetworkResponse
import com.thanh0x.coursedeals.data.user.model.PreferenceDto
import com.thanh0x.coursedeals.data.user.model.UpdateUserProfileRequest
import com.thanh0x.coursedeals.data.user.model.UserProfileDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class UserProfileServiceImpl(
    private val httpClient: HttpClient
) : UserProfileService {

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

    override suspend fun getPreferences(): NetworkResponse<PreferenceDto> {
        return safeRequest {
            httpClient.get("/api/v1/preferences")
        }
    }

    override suspend fun updatePreferences(preferences: PreferenceDto): NetworkResponse<PreferenceDto> {
        return safeRequest {
            httpClient.put("/api/v1/preferences") {
                contentType(ContentType.Application.Json)
                setBody(preferences)
            }
        }
    }

    override suspend fun getUserProfile(): NetworkResponse<UserProfileDto> {
        return safeRequest {
            httpClient.get("/api/v1/user/profile")
        }
    }

    override suspend fun updateUserProfile(request: UpdateUserProfileRequest): NetworkResponse<UserProfileDto> {
        return safeRequest {
            httpClient.put("/api/v1/user/profile") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }
}
