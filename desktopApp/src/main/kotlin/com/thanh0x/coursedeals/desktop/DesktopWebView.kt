package com.thanh0x.coursedeals.desktop

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import com.thanh0x.coursedeals.core.logging.LoggerFactory
import com.thanh0x.coursedeals.core.logging.debug
import com.thanh0x.coursedeals.core.ui.util.PlatformActions
import dev.datlag.kcef.KCEF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.cef.CefApp
import java.io.File
import java.net.URI
import java.util.Locale

const val TAG_WEBVIEW_TOOLBAR = "desktop_webview_toolbar"
const val TAG_WEBVIEW_BACK_BUTTON = "desktop_webview_back_button"
const val TAG_WEBVIEW_FORWARD_BUTTON = "desktop_webview_forward_button"
const val TAG_WEBVIEW_RELOAD_BUTTON = "desktop_webview_reload_button"
const val TAG_WEBVIEW_ADDRESS_BAR = "desktop_webview_address_bar"
const val TAG_WEBVIEW_OPEN_EXTERNAL_BUTTON = "desktop_webview_open_external_button"
const val TAG_WEBVIEW_PROGRESS = "desktop_webview_progress"
const val TAG_WEBVIEW_DISABLED = "desktop_webview_disabled"

private val webViewLogger by lazy {
    LoggerFactory.getLogger("DesktopWebView")
}

fun isValidHttpUrl(url: String): Boolean {
    if (url.isBlank()) return false
    return runCatching {
        val uri = URI(url.trim())
        val scheme = uri.scheme?.lowercase(Locale.ROOT)
        (scheme == "http" || scheme == "https") && !uri.host.isNullOrBlank()
    }.getOrDefault(false)
}

fun formatDisplayHost(url: String): String {
    if (url.isBlank()) return "about:blank"
    val trimmed = url.trim()
    return runCatching {
        val uri = URI(trimmed)
        val host = uri.host ?: return trimmed
        val cleanHost = host.removePrefix("www.")
        val path = uri.rawPath.orEmpty()
        val display = if (path.length > 1) "$cleanHost$path" else cleanHost
        if (display.endsWith("/")) display else "$display/"
    }.getOrDefault(trimmed)
}

object DesktopKcefInitializer {
    var isInitialized by mutableStateOf(
        runCatching { CefApp.getState() == CefApp.CefAppState.INITIALIZED }.getOrDefault(false),
    )
        private set

    suspend fun initialize() {
        if (isInitialized) return
        runCatching {
            KCEF.init(
                builder = {
                    installDir(File(System.getProperty("user.home"), ".coursedeals/kcef-bundle"))
                    progress {
                        onInitialized {
                            isInitialized = true
                            webViewLogger.debug { "Chromium browser engine ready" }
                        }
                    }
                },
                onError = { it?.printStackTrace() },
            )
        }
    }
}

@Composable
fun DesktopWebView(
    url: String,
    modifier: Modifier = Modifier,
    onPageStarted: () -> Unit = {},
    onPageFinished: () -> Unit = {},
    onPageError: () -> Unit = {},
    enableNativeWebEngine: Boolean = true,
) {
    val currentOnPageStarted by rememberUpdatedState(onPageStarted)
    val currentOnPageFinished by rememberUpdatedState(onPageFinished)
    val currentOnPageError by rememberUpdatedState(onPageError)

    if (!enableNativeWebEngine) {
        Column(modifier = modifier.fillMaxSize()) {
            DesktopBrowserToolbar(
                url = url,
                canGoBack = false,
                canGoForward = false,
                onBackClick = {},
                onForwardClick = {},
                onReloadClick = {},
                onOpenExternalClick = { PlatformActions.openUrl(url) },
            )
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth().testTag(TAG_WEBVIEW_DISABLED),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Native Web Engine Disabled (Preview / Test Mode)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        return
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            DesktopKcefInitializer.initialize()
        }
    }

    val navigator = rememberWebViewNavigator()
    val state = rememberWebViewState(url)

    LaunchedEffect(state.loadingState) {
        when (state.loadingState) {
            is LoadingState.Loading -> currentOnPageStarted()
            is LoadingState.Finished -> currentOnPageFinished()
            else -> Unit
        }
    }

    LaunchedEffect(state.errorsForCurrentRequest.size) {
        val mainFrameError = state.errorsForCurrentRequest.lastOrNull { it.isFromMainFrame }
        if (mainFrameError != null) {
            currentOnPageError()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        DesktopBrowserToolbar(
            url = state.lastLoadedUrl ?: url,
            canGoBack = navigator.canGoBack,
            canGoForward = navigator.canGoForward,
            onBackClick = { navigator.navigateBack() },
            onForwardClick = { navigator.navigateForward() },
            onReloadClick = { navigator.reload() },
            onOpenExternalClick = { PlatformActions.openUrl(state.lastLoadedUrl ?: url) },
        )

        val loadingState = state.loadingState
        if (loadingState is LoadingState.Loading) {
            LinearProgressIndicator(
                progress = { loadingState.progress },
                modifier = Modifier.fillMaxWidth().height(2.dp).testTag(TAG_WEBVIEW_PROGRESS),
            )
        } else {
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (!DesktopKcefInitializer.isInitialized) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Preparing Chromium Browser...",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            } else {
                WebView(
                    state = state,
                    navigator = navigator,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
fun DesktopBrowserToolbar(
    url: String,
    canGoBack: Boolean,
    canGoForward: Boolean,
    onBackClick: () -> Unit,
    onForwardClick: () -> Unit,
    onReloadClick: () -> Unit,
    onOpenExternalClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth().testTag(TAG_WEBVIEW_TOOLBAR),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onBackClick,
                enabled = canGoBack,
                modifier = Modifier.size(32.dp).testTag(TAG_WEBVIEW_BACK_BUTTON),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(18.dp),
                )
            }

            IconButton(
                onClick = onForwardClick,
                enabled = canGoForward,
                modifier = Modifier.size(32.dp).testTag(TAG_WEBVIEW_FORWARD_BUTTON),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Forward",
                    modifier = Modifier.size(18.dp),
                )
            }

            IconButton(
                onClick = onReloadClick,
                modifier = Modifier.size(32.dp).testTag(TAG_WEBVIEW_RELOAD_BUTTON),
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reload",
                    modifier = Modifier.size(18.dp),
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.weight(1f).testTag(TAG_WEBVIEW_ADDRESS_BAR),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (url.startsWith("https://", ignoreCase = true)) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "HTTPS Secure",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = formatDisplayHost(url),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = onOpenExternalClick,
                modifier = Modifier.size(32.dp).testTag(TAG_WEBVIEW_OPEN_EXTERNAL_BUTTON),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "Open in External Browser",
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
