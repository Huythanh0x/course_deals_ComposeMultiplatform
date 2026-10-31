package com.thanh0x.coursedeals.core.ui.web

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

var customPlatformWebViewRenderer: (
    @Composable (
        url: String,
        modifier: Modifier,
        onPageStarted: () -> Unit,
        onPageFinished: () -> Unit,
        onPageError: () -> Unit,
    ) -> Unit
)? = null

@Composable
expect fun PlatformWebView(
    url: String,
    modifier: Modifier = Modifier,
    onPageStarted: () -> Unit = {},
    onPageFinished: () -> Unit = {},
    onPageError: () -> Unit = {},
)
