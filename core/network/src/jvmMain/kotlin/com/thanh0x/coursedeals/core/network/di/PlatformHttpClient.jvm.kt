package com.thanh0x.coursedeals.core.network.di

import com.thanh0x.coursedeals.core.common.Constant
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import java.util.concurrent.TimeUnit

actual fun createPlatformHttpClient(block: HttpClientConfig<*>.() -> Unit): HttpClient {
    return HttpClient(OkHttp) {
        engine {
            config {
                readTimeout(Constant.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                connectTimeout(Constant.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            }
        }
        block()
    }
}

actual fun platformBaseUrl(): String = "http://192.168.2.185:8081"
