package com.thanh0x.coursedeals.desktop

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI(url))
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

    customPlatformWebViewRenderer = { url, modifier, onPageStarted, onPageFinished, _ ->
        DesktopWebViewFallback(
            url = url,
            modifier = modifier,
            onPageStarted = onPageStarted,
            onPageFinished = onPageFinished,
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

@Composable
private fun DesktopWebViewFallback(
    url: String,
    modifier: Modifier,
    onPageStarted: () -> Unit,
    onPageFinished: () -> Unit,
) {
    LaunchedEffect(url) {
        onPageStarted()
        onPageFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Course Enrollment Link",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = url,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { PlatformActions.openUrl(url) },
                ) {
                    Text(text = "Open in Browser")
                }
            }
        }
    }
}
