package com.thanh0x.coursedeals.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale

var customNetworkImageRenderer: (
    @Composable (
        url: String?,
        contentDescription: String?,
        modifier: Modifier,
        contentScale: ContentScale,
        error: Painter?,
    ) -> Unit
)? = null

@Composable
expect fun NetworkImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    error: Painter? = null,
)
