package com.thanh0x.coursedeals.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LightPrimary = Color(0xFF0B57D0)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFD3E3FD)
val LightOnPrimaryContainer = Color(0xFF041E49)

val LightTertiaryContainer = Color(0xFFFFD8E4)
val LightOnTertiaryContainer = Color(0xFF31111D)

val LightSurface = Color(0xFFFDFCFF)
val LightSurfaceContainerLow = Color(0xFFF7F9FF)
val LightSurfaceContainer = Color(0xFFF1F3FB)
val LightSurfaceContainerHigh = Color(0xFFEBEDF6)

val LightOnSurface = Color(0xFF1A1C1E)
val LightOnSurfaceVariant = Color(0xFF44474E)

val LightOutline = Color(0xFF74777F)
val LightOutlineVariant = Color(0xFFC4C6D0)

val LightErrorContainer = Color(0xFFFFDAD6)
val LightOnErrorContainer = Color(0xFF410002)
val LightError = Color(0xFFBA1A1A)

// Dark colors
val DarkPrimary = Color(0xFFA8C7FA)
val DarkOnPrimary = Color(0xFF00315C)
val DarkPrimaryContainer = Color(0xFF00458F)
val DarkOnPrimaryContainer = Color(0xFFD3E3FD)

val DarkTertiaryContainer = Color(0xFF633B48)
val DarkOnTertiaryContainer = Color(0xFFFFD8E4)

val DarkSurface = Color(0xFF111318)
val DarkSurfaceContainerLow = Color(0xFF1A1C20)
val DarkSurfaceContainer = Color(0xFF1E2025)
val DarkSurfaceContainerHigh = Color(0xFF282A2F)

val DarkOnSurface = Color(0xFFE2E2E6)
val DarkOnSurfaceVariant = Color(0xFFC4C6D0)

val DarkOutline = Color(0xFF8E9099)
val DarkOutlineVariant = Color(0xFF44474E)

val DarkErrorContainer = Color(0xFF93000A)
val DarkOnErrorContainer = Color(0xFFFFDAD6)
val DarkError = Color(0xFFFFB4AB)

val RatingAmber = Color(0xFFE8A33D)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    surface = LightSurface,
    surfaceContainerLow = LightSurfaceContainerLow,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    error = LightError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    surface = DarkSurface,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = DarkError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
)

@Composable
fun CourseDealsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
