package com.thanh0x.coursedeals.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale

@Composable
actual fun NetworkImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier,
    contentScale: ContentScale,
    error: Painter?,
) {
    val renderer = customNetworkImageRenderer
    if (renderer != null) {
        renderer(url, contentDescription, modifier, contentScale, error)
    } else if (error != null) {
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
