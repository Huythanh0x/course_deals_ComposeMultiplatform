package com.thanh0x.coursedeals.desktop

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.thanh0x.coursedeals.core.logging.LoggerFactory
import com.thanh0x.coursedeals.core.logging.debug
import com.thanh0x.coursedeals.core.logging.warn
import com.thanh0x.coursedeals.core.ui.util.PlatformActions
import javafx.animation.PauseTransition
import javafx.application.Platform
import javafx.concurrent.Worker
import javafx.embed.swing.JFXPanel
import javafx.embed.swing.SwingFXUtils
import javafx.scene.Scene
import javafx.scene.web.WebView
import javafx.util.Duration
import java.net.URI
import java.util.Locale
import javax.swing.SwingUtilities

const val TAG_WEBVIEW_TOOLBAR = "desktop_webview_toolbar"
const val TAG_WEBVIEW_BACK_BUTTON = "desktop_webview_back_button"
const val TAG_WEBVIEW_FORWARD_BUTTON = "desktop_webview_forward_button"
const val TAG_WEBVIEW_RELOAD_BUTTON = "desktop_webview_reload_button"
const val TAG_WEBVIEW_ADDRESS_BAR = "desktop_webview_address_bar"
const val TAG_WEBVIEW_OPEN_EXTERNAL_BUTTON = "desktop_webview_open_external_button"
const val TAG_WEBVIEW_PROGRESS = "desktop_webview_progress"
const val TAG_WEBVIEW_ERROR_CARD = "desktop_webview_error_card"
const val TAG_WEBVIEW_RETRY_BUTTON = "desktop_webview_retry_button"

private val SNAPSHOT_SETTLE_DELAYS_MS = listOf(600.0, 1800.0, 3200.0)
private const val DEFAULT_WEBVIEW_WIDTH = 430.0
private const val DEFAULT_WEBVIEW_HEIGHT = 760.0
private const val INITIAL_LOADING_PROGRESS = 0.1f
private const val HISTORY_STEP_BACK = -1
private const val HISTORY_STEP_FORWARD = 1

private val webViewLogger by lazy {
    LoggerFactory.getLogger("DesktopWebView")
}

data class DesktopBrowserUiState(
    val currentUrl: String,
    val pageTitle: String = "",
    val isLoading: Boolean = false,
    val progress: Float = 0f,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val errorMessage: String? = null,
) {
    fun onLoadStarted(targetUrl: String = currentUrl): DesktopBrowserUiState = copy(
        currentUrl = targetUrl,
        isLoading = true,
        progress = INITIAL_LOADING_PROGRESS,
        errorMessage = null,
    )

    fun onProgressChanged(newProgress: Float): DesktopBrowserUiState = copy(
        progress = newProgress.coerceIn(0f, 1f),
    )

    fun onLoadSucceeded(
        loadedUrl: String = currentUrl,
        title: String? = null,
        canGoBack: Boolean = this.canGoBack,
        canGoForward: Boolean = this.canGoForward,
    ): DesktopBrowserUiState = copy(
        currentUrl = loadedUrl.ifBlank { currentUrl },
        pageTitle = title?.takeIf { it.isNotBlank() } ?: pageTitle,
        isLoading = false,
        progress = 1f,
        canGoBack = canGoBack,
        canGoForward = canGoForward,
        errorMessage = null,
    )

    fun onLoadFailed(reason: String): DesktopBrowserUiState = copy(
        isLoading = false,
        progress = 0f,
        errorMessage = reason,
    )
}

fun isValidHttpUrl(url: String): Boolean {
    val trimmed = url.trim()
    if (trimmed.isEmpty()) return false
    return runCatching {
        val uri = URI(trimmed)
        val scheme = uri.scheme?.lowercase(Locale.ROOT)
        (scheme == "http" || scheme == "https") && !uri.host.isNullOrBlank()
    }.getOrDefault(false)
}

fun formatDisplayHost(url: String): String {
    val trimmed = url.trim()
    if (trimmed.isEmpty()) return "about:blank"
    return runCatching {
        val uri = URI(trimmed)
        val host = uri.host?.removePrefix("www.")?.takeIf { it.isNotBlank() } ?: return trimmed
        val path = uri.path?.takeIf { it.isNotEmpty() && it != "/" }.orEmpty()
        "$host$path"
    }.getOrDefault(trimmed)
}

private class DesktopWebEngineController {
    @Volatile
    var webView: WebView? = null

    fun loadUrl(url: String) {
        runOnFxThread {
            webView?.engine?.load(url)
        }
    }

    fun goBack() {
        runOnFxThread {
            val history = webView?.engine?.history ?: return@runOnFxThread
            if (history.currentIndex > 0) {
                history.go(HISTORY_STEP_BACK)
            }
        }
    }

    fun goForward() {
        runOnFxThread {
            val history = webView?.engine?.history ?: return@runOnFxThread
            if (history.currentIndex + 1 < history.entries.size) {
                history.go(HISTORY_STEP_FORWARD)
            }
        }
    }

    fun reload() {
        runOnFxThread {
            webView?.engine?.reload()
        }
    }

    fun dispose() {
        runOnFxThread {
            webView?.engine?.load("about:blank")
            webView = null
        }
    }

    private fun runOnFxThread(block: () -> Unit) {
        runCatching {
            if (Platform.isFxApplicationThread()) {
                block()
            } else {
                Platform.runLater(block)
            }
        }.onFailure { throwable ->
            webViewLogger.warn(throwable) { "JavaFX WebEngine action failed" }
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

    val isUrlValid = remember(url) { isValidHttpUrl(url) }
    var uiState by remember(url) {
        mutableStateOf(
            if (isUrlValid) {
                DesktopBrowserUiState(currentUrl = url, isLoading = true, progress = INITIAL_LOADING_PROGRESS)
            } else {
                DesktopBrowserUiState(
                    currentUrl = url,
                    isLoading = false,
                    errorMessage = "Invalid or unsupported URL: ${url.ifBlank { "(empty)" }}",
                )
            }
        )
    }
    var webSnapshotBitmap by remember(url) { mutableStateOf<ImageBitmap?>(null) }
    val controller = remember { DesktopWebEngineController() }

    LaunchedEffect(url, isUrlValid) {
        if (!isUrlValid) {
            currentOnPageError()
        } else if (controller.webView != null) {
            controller.loadUrl(url)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            controller.dispose()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        DesktopBrowserToolbar(
            uiState = uiState,
            onBackClick = { controller.goBack() },
            onForwardClick = { controller.goForward() },
            onReloadClick = {
                if (isValidHttpUrl(uiState.currentUrl)) {
                    uiState = uiState.onLoadStarted(uiState.currentUrl)
                    if (controller.webView != null) {
                        controller.reload()
                    }
                }
            },
            onOpenExternalClick = {
                PlatformActions.openUrl(uiState.currentUrl)
            },
        )

        if (uiState.isLoading) {
            LinearProgressIndicator(
                progress = { uiState.progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .testTag(TAG_WEBVIEW_PROGRESS),
            )
        } else {
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            val errorMessage = uiState.errorMessage
            if (errorMessage != null) {
                DesktopBrowserErrorCard(
                    url = uiState.currentUrl,
                    errorMessage = errorMessage,
                    onRetryClick = {
                        if (isValidHttpUrl(uiState.currentUrl)) {
                            uiState = uiState.onLoadStarted(uiState.currentUrl)
                            controller.loadUrl(uiState.currentUrl)
                        } else {
                            currentOnPageError()
                        }
                    },
                    onOpenExternalClick = {
                        PlatformActions.openUrl(uiState.currentUrl)
                    },
                )
            } else if (enableNativeWebEngine) {
                SwingPanel(
                    background = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxSize(),
                    factory = {
                        createJavaFxBrowserPanel(
                            initialUrl = url,
                            controller = controller,
                            onStateUpdate = { transform ->
                                SwingUtilities.invokeLater {
                                    uiState = transform(uiState)
                                }
                            },
                            onSnapshotCaptured = { bitmap ->
                                SwingUtilities.invokeLater {
                                    webSnapshotBitmap = bitmap
                                }
                            },
                            onPageStarted = {
                                SwingUtilities.invokeLater { currentOnPageStarted() }
                            },
                            onPageFinished = {
                                SwingUtilities.invokeLater { currentOnPageFinished() }
                            },
                            onPageError = {
                                SwingUtilities.invokeLater { currentOnPageError() }
                            },
                        )
                    },
                )

                val snapshot = webSnapshotBitmap
                if (snapshot != null) {
                    Image(
                        bitmap = snapshot,
                        contentDescription = uiState.pageTitle.ifBlank { "Embedded web content" },
                        contentScale = ContentScale.FillWidth,
                        alignment = Alignment.TopCenter,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
fun DesktopBrowserToolbar(
    uiState: DesktopBrowserUiState,
    onBackClick: () -> Unit,
    onForwardClick: () -> Unit,
    onReloadClick: () -> Unit,
    onOpenExternalClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag(TAG_WEBVIEW_TOOLBAR),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onBackClick,
                enabled = uiState.canGoBack,
                modifier = Modifier
                    .size(32.dp)
                    .testTag(TAG_WEBVIEW_BACK_BUTTON),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(18.dp),
                )
            }

            IconButton(
                onClick = onForwardClick,
                enabled = uiState.canGoForward,
                modifier = Modifier
                    .size(32.dp)
                    .testTag(TAG_WEBVIEW_FORWARD_BUTTON),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Forward",
                    modifier = Modifier.size(18.dp),
                )
            }

            IconButton(
                onClick = onReloadClick,
                modifier = Modifier
                    .size(32.dp)
                    .testTag(TAG_WEBVIEW_RELOAD_BUTTON),
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
                modifier = Modifier
                    .weight(1f)
                    .testTag(TAG_WEBVIEW_ADDRESS_BAR),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (uiState.currentUrl.startsWith("https://", ignoreCase = true)) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "HTTPS Secure",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = formatDisplayHost(uiState.currentUrl),
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
                modifier = Modifier
                    .size(32.dp)
                    .testTag(TAG_WEBVIEW_OPEN_EXTERNAL_BUTTON),
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

@Composable
fun DesktopBrowserErrorCard(
    url: String,
    errorMessage: String,
    onRetryClick: () -> Unit,
    onOpenExternalClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .testTag(TAG_WEBVIEW_ERROR_CARD),
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
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(36.dp),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Unable to load page",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                if (url.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = url,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        onClick = onRetryClick,
                        modifier = Modifier.testTag(TAG_WEBVIEW_RETRY_BUTTON),
                    ) {
                        Text(text = "Retry")
                    }
                    Button(
                        onClick = onOpenExternalClick,
                    ) {
                        Text(text = "Open in Browser")
                    }
                }
            }
        }
    }
}

private fun createJavaFxBrowserPanel(
    initialUrl: String,
    controller: DesktopWebEngineController,
    onStateUpdate: ((DesktopBrowserUiState) -> DesktopBrowserUiState) -> Unit,
    onSnapshotCaptured: (ImageBitmap) -> Unit,
    onPageStarted: () -> Unit,
    onPageFinished: () -> Unit,
    onPageError: () -> Unit,
): JFXPanel {
    runCatching {
        Platform.setImplicitExit(false)
    }.onFailure { throwable ->
        webViewLogger.warn(throwable) { "Failed to set JavaFX implicitExit=false" }
    }

    val jfxPanel = JFXPanel()
    runCatching {
        Platform.runLater {
            runCatching {
                val webView = WebView()
                webView.isContextMenuEnabled = true
                controller.webView = webView
                val engine = webView.engine

                engine.locationProperty().addListener { _, _, newLocation ->
                    if (!newLocation.isNullOrBlank()) {
                        onStateUpdate { state -> state.copy(currentUrl = newLocation) }
                    }
                }

                engine.titleProperty().addListener { _, _, newTitle ->
                    if (!newTitle.isNullOrBlank()) {
                        onStateUpdate { state -> state.copy(pageTitle = newTitle) }
                    }
                }

                engine.history.currentIndexProperty().addListener { _, _, _ ->
                    val currentIndex = engine.history.currentIndex
                    val totalEntries = engine.history.entries.size
                    onStateUpdate { state ->
                        state.copy(
                            canGoBack = currentIndex > 0,
                            canGoForward = currentIndex + 1 < totalEntries,
                        )
                    }
                }

                engine.loadWorker.progressProperty().addListener { _, _, newProgress ->
                    val p = newProgress.toFloat()
                    if (p >= 0f) {
                        onStateUpdate { state -> state.onProgressChanged(p) }
                    }
                }

                engine.loadWorker.stateProperty().addListener { _, _, newState ->
                    when (newState) {
                        Worker.State.RUNNING -> {
                            onStateUpdate { state ->
                                state.onLoadStarted(engine.location?.takeIf { it.isNotBlank() } ?: state.currentUrl)
                            }
                            onPageStarted()
                        }

                        Worker.State.SUCCEEDED -> {
                            val currentIndex = engine.history.currentIndex
                            val totalEntries = engine.history.entries.size
                            onStateUpdate { state ->
                                state.onLoadSucceeded(
                                    loadedUrl = engine.location ?: state.currentUrl,
                                    title = engine.title,
                                    canGoBack = currentIndex > 0,
                                    canGoForward = currentIndex + 1 < totalEntries,
                                )
                            }
                            onPageFinished()
                            scheduleWebViewSnapshots(webView, onSnapshotCaptured)
                        }

                        Worker.State.FAILED -> {
                            val exceptionMessage = engine.loadWorker.exception?.message
                                ?: "Failed to load webpage"
                            webViewLogger.warn {
                                "JavaFX WebEngine load failed for ${engine.location}: $exceptionMessage"
                            }
                            onStateUpdate { state -> state.onLoadFailed(exceptionMessage) }
                            onPageError()
                        }

                        else -> Unit
                    }
                }

                jfxPanel.scene = Scene(webView)
                engine.load(initialUrl)
            }.onFailure { throwable ->
                webViewLogger.warn(throwable) { "Failed to initialize JavaFX WebView scene" }
                onStateUpdate { state ->
                    state.onLoadFailed(throwable.message ?: "JavaFX WebView initialization failed")
                }
                onPageError()
            }
        }
    }.onFailure { throwable ->
        webViewLogger.warn(throwable) { "Failed to dispatch JavaFX initialization to Platform.runLater" }
        onStateUpdate { state ->
            state.onLoadFailed(throwable.message ?: "JavaFX runtime unavailable")
        }
        onPageError()
    }

    return jfxPanel
}

private fun scheduleWebViewSnapshots(
    webView: WebView,
    onSnapshotCaptured: (ImageBitmap) -> Unit,
) {
    captureWebViewSnapshot(webView, onSnapshotCaptured)
    SNAPSHOT_SETTLE_DELAYS_MS.forEach { delayMs ->
        val settleTransition = PauseTransition(Duration.millis(delayMs))
        settleTransition.setOnFinished {
            captureWebViewSnapshot(webView, onSnapshotCaptured)
        }
        settleTransition.play()
    }
}

private fun captureWebViewSnapshot(
    webView: WebView,
    onSnapshotCaptured: (ImageBitmap) -> Unit,
) {
    runCatching {
        if (webView.width <= 0.0 || webView.height <= 0.0) {
            webView.resize(DEFAULT_WEBVIEW_WIDTH, DEFAULT_WEBVIEW_HEIGHT)
        }
        val fxImage = webView.snapshot(null, null) ?: return
        val bufferedImage = SwingFXUtils.fromFXImage(fxImage, null) ?: return
        val composeBitmap = bufferedImage.toComposeImageBitmap()
        webViewLogger.debug {
            "Captured JavaFX WebView snapshot (${composeBitmap.width}x${composeBitmap.height})"
        }
        onSnapshotCaptured(composeBitmap)
    }.onFailure { throwable ->
        webViewLogger.warn(throwable) { "Failed to snapshot JavaFX WebView into Skia ImageBitmap" }
    }
}
