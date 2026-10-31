package com.thanh0x.coursedeals.core.ui.resources

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource as androidPainterResource
import androidx.compose.ui.res.stringResource as androidStringResource

/**
 * Compose Multiplatform string resource resolution function matching `org.jetbrains.compose.resources.stringResource`.
 */
@Composable
@ReadOnlyComposable
fun stringResource(resource: StringResource): String {
    return androidStringResource(resource.resId)
}

/**
 * Formatted string resource resolution matching `org.jetbrains.compose.resources.stringResource`.
 */
@Composable
@ReadOnlyComposable
fun stringResource(resource: StringResource, vararg formatArgs: Any): String {
    return androidStringResource(resource.resId, *formatArgs)
}

/**
 * Compose Multiplatform painter resource resolution function matching `org.jetbrains.compose.resources.painterResource`.
 */
@Composable
fun painterResource(resource: DrawableResource): Painter {
    return androidPainterResource(resource.resId)
}
