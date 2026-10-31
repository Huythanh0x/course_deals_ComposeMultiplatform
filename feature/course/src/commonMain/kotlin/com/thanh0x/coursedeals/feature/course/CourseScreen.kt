package com.thanh0x.coursedeals.feature.course

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thanh0x.coursedeals.core.common.Constant
import com.thanh0x.coursedeals.core.ui.web.PlatformWebView

@Composable
fun CourseRoute(
    viewModel: CourseViewModel,
    modifier: Modifier = Modifier,
    onPageStarted: () -> Unit = {},
    onPageFinished: () -> Unit = {},
    onPageError: () -> Unit = {},
    onWebViewCreated: (Any) -> Unit = {},
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
    onWebViewCreated: (Any) -> Unit = {},
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

@Suppress("UnusedParameter")
@Composable
fun CourseWebView(
    url: String,
    modifier: Modifier = Modifier,
    onPageStarted: () -> Unit = {},
    onPageFinished: () -> Unit = {},
    onPageError: () -> Unit = {},
    onWebViewCreated: (Any) -> Unit = {},
) {
    PlatformWebView(
        url = url,
        modifier = modifier,
        onPageStarted = onPageStarted,
        onPageFinished = onPageFinished,
        onPageError = onPageError,
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
