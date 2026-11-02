package com.thanh0x.coursedeals.data.user.source.remote

import com.thanh0x.coursedeals.core.network.NetworkResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

interface AppConfigService {
    suspend fun getAppConfigRawJson(): NetworkResponse<String>
}

class AppConfigServiceImpl(
    private val httpClient: HttpClient,
) : AppConfigService {
    override suspend fun getAppConfigRawJson(): NetworkResponse<String> {
        return try {
            val response = httpClient.get(APP_CONFIG_PATH)
            if (response.status.isSuccess()) {
                NetworkResponse.success(
                    body = response.bodyAsText(),
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

    companion object {
        const val APP_CONFIG_PATH = "/api/v1/app/config"
    }
}
