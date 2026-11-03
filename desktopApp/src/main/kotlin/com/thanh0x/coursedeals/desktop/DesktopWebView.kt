package com.thanh0x.coursedeals.desktop

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import com.thanh0x.coursedeals.core.logging.LoggerFactory
import com.thanh0x.coursedeals.core.logging.debug
import com.thanh0x.coursedeals.core.logging.warn
import com.thanh0x.coursedeals.core.ui.util.PlatformActions
import dev.datlag.kcef.KCEF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URI
import java.util.Locale
import kotlin.math.max

const val TAG_WEBVIEW_TOOLBAR = "desktop_webview_toolbar"
const val TAG_WEBVIEW_BACK_BUTTON = "desktop_webview_back_button"
const val TAG_WEBVIEW_FORWARD_BUTTON = "desktop_webview_forward_button"
const val TAG_WEBVIEW_RELOAD_BUTTON = "desktop_webview_reload_button"
const val TAG_WEBVIEW_ADDRESS_BAR = "desktop_webview_address_bar"
const val TAG_WEBVIEW_OPEN_EXTERNAL_BUTTON = "desktop_webview_open_external_button"
const val TAG_WEBVIEW_PROGRESS = "desktop_webview_progress"
const val TAG_WEBVIEW_ERROR_CARD = "desktop_webview_error_card"
const val TAG_WEBVIEW_RETRY_BUTTON = "desktop_webview_retry_button"

private const val INITIAL_LOADING_PROGRESS = 0.1f

private const val KCEF_INITIAL_PROGRESS = 0.05f

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

sealed interface DesktopKcefState {
    data object Idle : DesktopKcefState
    data class Initializing(val progress: Float) : DesktopKcefState
    data object Initialized : DesktopKcefState
    data class Error(val message: String) : DesktopKcefState
}

object DesktopKcefInitializer {
    var state by mutableStateOf<DesktopKcefState>(DesktopKcefState.Idle)
        private set

    @Volatile
    private var isStarting = false

    suspend fun initialize() {
        if (state is DesktopKcefState.Initialized || isStarting) return
        isStarting = true
        state = DesktopKcefState.Initializing(KCEF_INITIAL_PROGRESS)

        withContext(Dispatchers.IO) {
            runCatching {
                val bundleDir = File(System.getProperty("user.home"), ".coursedeals/kcef-bundle")
                bundleDir.mkdirs()
                KCEF.init(
                    builder = {
                        installDir(bundleDir)
                        progress {
                            onDownloading { p ->
                                state = DesktopKcefState.Initializing(max(p, KCEF_INITIAL_PROGRESS))
                            }
                            onInitialized {
                                state = DesktopKcefState.Initialized
                                webViewLogger.debug { "KCEF Chromium Engine initialized successfully" }
                            }
                        }
                    },
                    onError = { error ->
                        val msg = error?.message ?: "Chromium Engine initialization error"
                        webViewLogger.warn(error) { "KCEF initialization failed: $msg" }
                        state = DesktopKcefState.Error(msg)
                    },
                    onRestartRequired = {
                        state = DesktopKcefState.Error("A restart is required to finish setting up Chromium.")
                    },
                )
            }.onFailure { throwable ->
                if (throwable is kotlinx.coroutines.CancellationException) {
                    throw throwable
                }
                val msg = throwable.message ?: "Failed to initialize Chromium engine"
                webViewLogger.warn(throwable) { "KCEF init exception: $msg" }
                state = DesktopKcefState.Error(msg)
            }
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

    LaunchedEffect(url, isUrlValid) {
        if (!isUrlValid) {
            currentOnPageError()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        DesktopBrowserToolbar(
            uiState = uiState,
            onBackClick = { /* Wired when navigator is available below */ },
            onForwardClick = { /* Wired when navigator is available below */ },
            onReloadClick = {
                if (isValidHttpUrl(uiState.currentUrl)) {
                    uiState = uiState.onLoadStarted(uiState.currentUrl)
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
                        } else {
                            currentOnPageError()
                        }
                    },
                    onOpenExternalClick = {
                        PlatformActions.openUrl(uiState.currentUrl)
                    },
                )
            } else if (!enableNativeWebEngine) {
                LaunchedEffect(url) {
                    currentOnPageStarted()
                    currentOnPageFinished()
                }
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Embedded Web Engine Disabled (Preview / Test Mode)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                DesktopChromiumContent(
                    url = url,
                    uiState = uiState,
                    onUiStateChange = { uiState = it },
                    onPageStarted = currentOnPageStarted,
                    onPageFinished = currentOnPageFinished,
                    onPageError = currentOnPageError,
                )
            }
        }
    }
}

@Composable
private fun DesktopChromiumContent(
    url: String,
    uiState: DesktopBrowserUiState,
    onUiStateChange: (DesktopBrowserUiState) -> Unit,
    onPageStarted: () -> Unit,
    onPageFinished: () -> Unit,
    onPageError: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val kcefState = DesktopKcefInitializer.state

    LaunchedEffect(Unit) {
        DesktopKcefInitializer.initialize()
    }

    when (kcefState) {
        is DesktopKcefState.Idle, is DesktopKcefState.Initializing -> {
            val downloadProgress = (kcefState as? DesktopKcefState.Initializing)?.progress
                ?: KCEF_INITIAL_PROGRESS
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Preparing Chromium Engine...",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Desktop Preview is setting up the embedded Chromium browser. " +
                                "This happens once on first launch.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { downloadProgress },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { PlatformActions.openUrl(url) },
                        ) {
                            Text("Open in Default Browser")
                        }
                    }
                }
            }
        }

        is DesktopKcefState.Error -> {
            DesktopBrowserErrorCard(
                url = url,
                errorMessage = kcefState.message,
                onRetryClick = {
                    coroutineScope.launch {
                        DesktopKcefInitializer.initialize()
                    }
                },
                onOpenExternalClick = {
                    PlatformActions.openUrl(url)
                },
            )
        }

        is DesktopKcefState.Initialized -> {
            val webViewState = rememberWebViewState(url)
            val navigator = rememberWebViewNavigator()

            val loadingState = webViewState.loadingState
            LaunchedEffect(loadingState) {
                when (loadingState) {
                    is LoadingState.Loading -> {
                        webViewLogger.debug { "WebView loading: progress=${loadingState.progress}" }
                        onUiStateChange(
                            uiState.copy(
                                isLoading = true,
                                progress = loadingState.progress,
                                canGoBack = navigator.canGoBack,
                                canGoForward = navigator.canGoForward,
                            )
                        )
                        onPageStarted()
                    }
                    is LoadingState.Finished -> {
                        webViewLogger.debug { "WebView finished: title='${webViewState.pageTitle}', url='${webViewState.lastLoadedUrl}'" }
                        onUiStateChange(
                            uiState.copy(
                                isLoading = false,
                                progress = 1f,
                                pageTitle = webViewState.pageTitle.orEmpty(),
                                canGoBack = navigator.canGoBack,
                                canGoForward = navigator.canGoForward,
                            )
                        )
                        onPageFinished()
                    }
                    else -> Unit
                }
            }

            LaunchedEffect(webViewState.errorsForCurrentRequest.size) {
                val mainFrameError = webViewState.errorsForCurrentRequest.lastOrNull { it.isFromMainFrame }
                if (mainFrameError != null) {
                    onUiStateChange(uiState.copy(errorMessage = mainFrameError.description))
                    onPageError()
                }
            }

            WebView(
                state = webViewState,
                navigator = navigator,
                modifier = Modifier.fillMaxSize(),
            )
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
