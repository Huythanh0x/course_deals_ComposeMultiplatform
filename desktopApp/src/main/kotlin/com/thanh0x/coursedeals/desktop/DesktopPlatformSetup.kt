package com.thanh0x.coursedeals.desktop

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.thanh0x.coursedeals.core.logging.LoggerFactory
import com.thanh0x.coursedeals.core.logging.warn
import com.thanh0x.coursedeals.core.ui.components.customNetworkImageRenderer
import com.thanh0x.coursedeals.core.ui.util.PlatformActions
import com.thanh0x.coursedeals.core.ui.web.customPlatformWebViewRenderer
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap
import org.jetbrains.skia.Image as SkiaImage

private const val MAX_IMAGE_CACHE_ENTRIES = 120

private val desktopLogger by lazy {
    LoggerFactory.getLogger("DesktopPlatformSetup")
}

private val desktopImageHttpClient by lazy {
    HttpClient(OkHttp)
}

private val desktopImageCache = ConcurrentHashMap<String, ImageBitmap>()

fun installDesktopPlatformActions() {
    PlatformActions.onCopyToClipboard = { text ->
        runCatching {
            val selection = StringSelection(text)
            Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, null)
        }.onFailure { throwable ->
            desktopLogger.warn(throwable) { "Failed to copy text to system clipboard" }
        }
    }

    PlatformActions.onOpenUrl = { url ->
        runCatching {
            if (isValidHttpUrl(url) &&
                Desktop.isDesktopSupported() &&
                Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)
            ) {
                Desktop.getDesktop().browse(URI(url.trim()))
            } else {
                desktopLogger.warn { "Skipped opening unsupported or invalid URL in system browser: $url" }
            }
        }.onFailure { throwable ->
            desktopLogger.warn(throwable) { "Failed to open URL in system browser: $url" }
        }
    }

    PlatformActions.onSendEmail = { email, subject ->
        runCatching {
            val encodedSubject = URLEncoder.encode(subject, StandardCharsets.UTF_8).replace("+", "%20")
            val mailUri = URI("mailto:$email?subject=$encodedSubject")
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.MAIL)) {
                Desktop.getDesktop().mail(mailUri)
                true
            } else {
                false
            }
        }.getOrElse { throwable ->
            desktopLogger.warn(throwable) { "Failed to launch system mail client for $email" }
            false
        }
    }

    PlatformActions.onShareLink = { title, url ->
        PlatformActions.copyToClipboard("$title\n$url")
        PlatformActions.showToast("Link copied to clipboard")
    }
}

fun installDesktopRenderers() {
    customNetworkImageRenderer = { url, contentDescription, modifier, contentScale, error ->
        DesktopNetworkImage(
            url = url,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            error = error,
        )
    }

    customPlatformWebViewRenderer = { url, modifier, onPageStarted, onPageFinished, onPageError ->
        DesktopWebView(
            url = url,
            modifier = modifier,
            onPageStarted = onPageStarted,
            onPageFinished = onPageFinished,
            onPageError = onPageError,
        )
    }
}

@Composable
private fun DesktopNetworkImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier,
    contentScale: ContentScale,
    error: Painter?,
) {
    var bitmap by remember(url) { mutableStateOf(if (url != null) desktopImageCache[url] else null) }
    var failed by remember(url) { mutableStateOf(false) }

    LaunchedEffect(url) {
        if (url.isNullOrBlank()) {
            failed = true
        } else if (bitmap == null) {
            val loaded = withContext(Dispatchers.IO) {
                runCatching {
                    val bytes = desktopImageHttpClient.get(url).readRawBytes()
                    SkiaImage.makeFromEncoded(bytes).toComposeImageBitmap()
                }.getOrNull()
            }
            if (loaded != null) {
                if (desktopImageCache.size > MAX_IMAGE_CACHE_ENTRIES) {
                    desktopImageCache.clear()
                }
                desktopImageCache[url] = loaded
                bitmap = loaded
            } else {
                failed = true
            }
        }
    }

    val currentBitmap = bitmap
    if (currentBitmap != null) {
        Image(
            bitmap = currentBitmap,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier,
        )
    } else if (failed && error != null) {
        Image(
            painter = error,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier,
        )
    } else {
        Box(modifier = modifier)
    }
}
