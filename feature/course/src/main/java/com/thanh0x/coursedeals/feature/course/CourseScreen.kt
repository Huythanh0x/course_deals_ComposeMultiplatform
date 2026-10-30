package com.thanh0x.coursedeals.feature.course

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thanh0x.coursedeals.core.common.Constant

@Composable
fun CourseRoute(
    viewModel: CourseViewModel,
    modifier: Modifier = Modifier,
    onPageStarted: () -> Unit = {},
    onPageFinished: () -> Unit = {},
    onPageError: () -> Unit = {},
    onWebViewCreated: (WebView) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CourseScreen(
        uiState = uiState,
        modifier = modifier,
        onPageStarted = onPageStarted,
        onPageFinished = onPageFinished,
        onPageError = onPageError,
        onWebViewCreated = onWebViewCreated,
    )
}

@Composable
fun CourseScreen(
    uiState: CourseUiState,
    modifier: Modifier = Modifier,
    onPageStarted: () -> Unit = {},
    onPageFinished: () -> Unit = {},
    onPageError: () -> Unit = {},
    onWebViewCreated: (WebView) -> Unit = {},
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isInternetAvailable) {
                CourseWebView(
                    url = Constant.MY_COURSE_URL,
                    modifier = Modifier.fillMaxSize(),
                    onPageStarted = onPageStarted,
                    onPageFinished = onPageFinished,
                    onPageError = onPageError,
                    onWebViewCreated = onWebViewCreated,
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CourseWebView(
    url: String,
    modifier: Modifier = Modifier,
    onPageStarted: () -> Unit = {},
    onPageFinished: () -> Unit = {},
    onPageError: () -> Unit = {},
    onWebViewCreated: (WebView) -> Unit = {},
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                visibility = View.INVISIBLE
                settings.apply {
                    javaScriptEnabled = true
                    javaScriptCanOpenWindowsAutomatically = true
                    domStorageEnabled = true
                    allowFileAccess = true
                    builtInZoomControls = true
                    displayZoomControls = false
                }
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        visibility = View.VISIBLE
                        onPageStarted()
                        super.onPageStarted(view, url, favicon)
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        visibility = View.VISIBLE
                        onPageFinished()
                        super.onPageFinished(view, url)
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?,
                    ) {
                        onPageError()
                        super.onReceivedError(view, request, error)
                    }
                }
                onWebViewCreated(this)
                loadUrl(url)
            }
        },
        update = { webView ->
            if (webView.url != url) {
                webView.loadUrl(url)
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun CourseScreenPreview() {
    CourseScreen(
        uiState = CourseUiState(isInternetAvailable = true),
    )
}

@Preview(showBackground = true)
@Composable
private fun CourseScreenNoInternetPreview() {
    CourseScreen(
        uiState = CourseUiState(isInternetAvailable = false),
    )
}
