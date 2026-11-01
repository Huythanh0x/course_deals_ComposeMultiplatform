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

actual fun platformBaseUrl(): String {
    val isAndroid = try {
        Class.forName("android.os.Build")
        true
    } catch (_: Throwable) {
        false
    }
    return if (isAndroid) "http://10.0.2.2:8081" else "http://localhost:8081"
}
