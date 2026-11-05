@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.thanh0x.coursedeals.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.w3c.dom.HTMLElement
import org.w3c.fetch.Response
import kotlin.js.JsString
import kotlin.js.Promise
import kotlin.js.toJsString

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(title = "Course Deal — Compose Multiplatform Web (Wasm)", canvasElementId = "ComposeTarget") {
        CourseDealsWebScreen()
    }
}

@Suppress("FunctionOnlyReturningConstant")
internal actual fun currentRuntimeLabel(): String = "Kotlin/Wasm"

@Suppress("FunctionOnlyReturningConstant")
internal actual fun alternateRuntimeLabel(): String = "Kotlin/JS"

internal actual fun switchRuntimeTarget() {
    window.location.href = "/app/?target=js"
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
    val response = window.fetch(url).await<Response>()
    return response.text().await<JsString>().toString()
}

internal actual suspend fun fetchUrlBytes(url: String): ByteArray {
    val response = window.fetch(url).await<Response>()
    if (!response.ok) error("HTTP ${response.status}")
    val buffer = response.arrayBuffer().await<ArrayBuffer>()
    val view = Int8Array(buffer)
    return ByteArray(view.length) { idx -> view[idx] }
}

internal actual suspend fun postUrlJson(
    url: String,
    jsonBody: String,
): Int {
    val response = jsPostJson(url.toJsString(), jsonBody.toJsString()).await<Response>()
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

private fun jsDateNow(): Double = js("Date.now()")

@Suppress("UnusedParameter")
private fun jsPostJson(
    url: JsString,
    body: JsString,
): Promise<Response> =
    js("fetch(url, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: body })")
