package com.thanh0x.coursedeals.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.jetbrains.skiko.wasm.onWasmReady
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.w3c.dom.HTMLElement
import org.w3c.fetch.Response
import kotlin.js.Promise

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    onWasmReady {
        CanvasBasedWindow(title = "Course Deal — Compose Multiplatform Web (JS)", canvasElementId = "ComposeTarget") {
            CourseDealsWebScreen()
        }
    }
}

@Suppress("FunctionOnlyReturningConstant")
internal actual fun currentRuntimeLabel(): String = "Kotlin/JS (IR)"

@Suppress("FunctionOnlyReturningConstant")
internal actual fun alternateRuntimeLabel(): String = "Kotlin/Wasm"

internal actual fun switchRuntimeTarget() {
    window.location.href = "/app/?target=wasm"
}

internal actual fun hideLoadingOverlay() {
    (document.getElementById("loading-overlay") as? HTMLElement)?.style?.display = "none"
}

internal actual fun navigateToUrl(url: String) {
    window.location.href = url
}

internal actual fun openExternalTab(url: String) {
    window.open(url, "_blank")
}

internal actual fun encodeUriParam(value: String): String = encodeURIComponent(value)

internal actual suspend fun fetchUrlText(url: String): String {
    val response = window.fetch(url).await()
    return response.text().await()
}

internal actual suspend fun fetchUrlBytes(url: String): ByteArray {
    val response = window.fetch(url).await()
    if (!response.ok) error("HTTP ${response.status}")
    val buffer = response.arrayBuffer().await()
    val view = Int8Array(buffer)
    return ByteArray(view.length) { idx -> view[idx] }
}

internal actual suspend fun postUrlJson(
    url: String,
    jsonBody: String,
): Int {
    val response = jsPostJson(url, jsonBody).await()
    return response.status.toInt()
}

internal actual fun currentTimeMillis(): Long = jsDateNow().toLong()

internal actual fun getLocalStorageItem(key: String): String? =
    runCatching { localStorage.getItem(key) }.getOrNull()

internal actual fun setLocalStorageItem(
    key: String,
    value: String,
) {
    runCatching { localStorage.setItem(key, value) }
}

private external fun encodeURIComponent(str: String): String

private fun jsDateNow(): Double = js("Date.now()") as Double

@Suppress("UnusedParameter")
private fun jsPostJson(
    url: String,
    body: String,
): Promise<Response> {
    val raw =
        js(
            "fetch(url, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: body })",
        )
    return raw as Promise<Response>
}
