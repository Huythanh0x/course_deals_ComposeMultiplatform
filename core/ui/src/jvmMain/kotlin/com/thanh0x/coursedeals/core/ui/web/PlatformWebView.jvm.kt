package com.thanh0x.coursedeals.core.ui.web

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier

@Composable
actual fun PlatformWebView(
    url: String,
    modifier: Modifier,
    onPageStarted: () -> Unit,
    onPageFinished: () -> Unit,
    onPageError: () -> Unit,
) {
    val renderer = customPlatformWebViewRenderer
    if (renderer != null) {
        renderer(url, modifier, onPageStarted, onPageFinished, onPageError)
    } else {
        LaunchedEffect(url) {
            onPageStarted()
            onPageFinished()
        }
        Box(modifier = modifier)
    }
}
