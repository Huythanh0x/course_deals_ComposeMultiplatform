package com.thanh0x.coursedeals.core.network.di

import com.thanh0x.coursedeals.core.common.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.darwin.Darwin

actual fun createPlatformHttpClient(block: HttpClientConfig<*>.() -> Unit): HttpClient {
    return HttpClient(Darwin) {
        engine {
            configureRequest {
                setAllowsCellularAccess(true)
            }
        }
        block()
    }
}

actual fun platformBaseUrl(): String =
    BuildConfig.LOCAL_API_BASE_URL.ifBlank { "http://localhost:8081" }
