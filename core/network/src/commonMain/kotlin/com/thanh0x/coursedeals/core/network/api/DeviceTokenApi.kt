package com.thanh0x.coursedeals.core.network.api

import com.thanh0x.coursedeals.core.network.NetworkResponse
import com.thanh0x.coursedeals.core.network.model.DeviceTokenRequest
import com.thanh0x.coursedeals.core.network.model.DeviceTokenResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

interface DeviceTokenApi {
    suspend fun registerDeviceToken(request: DeviceTokenRequest): NetworkResponse<DeviceTokenResponse>
    suspend fun deleteDeviceToken(token: String): NetworkResponse<Unit>
}

class DeviceTokenApiImpl(
    private val httpClient: HttpClient,
) : DeviceTokenApi {

    override suspend fun registerDeviceToken(request: DeviceTokenRequest): NetworkResponse<DeviceTokenResponse> {
        return try {
            val response = httpClient.post("/api/v1/users/device-token") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (response.status.isSuccess()) {
                val parsed: DeviceTokenResponse = response.body()
                NetworkResponse.success(
                    body = parsed,
                    code = response.status.value,
                    message = response.status.description,
                )
            } else {
                NetworkResponse.error(
                    code = response.status.value,
                    message = response.status.description,
                )
            }
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            NetworkResponse.error(
                code = -1,
                message = e.message ?: "Unknown Error",
            )
        }
    }

    override suspend fun deleteDeviceToken(token: String): NetworkResponse<Unit> {
        return try {
            val response = httpClient.delete("/api/v1/users/device-token") {
                parameter("fcmToken", token)
            }
            if (response.status.isSuccess()) {
                NetworkResponse.success(
                    body = Unit,
                    code = response.status.value,
                    message = response.status.description,
                )
            } else {
                NetworkResponse.error(
                    code = response.status.value,
                    message = response.status.description,
                )
            }
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            NetworkResponse.error(
                code = -1,
                message = e.message ?: "Unknown Error",
            )
        }
    }
}
