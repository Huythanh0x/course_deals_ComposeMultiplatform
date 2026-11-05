package com.thanh0x.coursedeals.web

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.jetbrains.skia.Image as SkiaImage
import kotlin.math.roundToInt

// Exact Material 3 Color Roles from core/ui/theme/Theme.kt
internal val LightPrimary = Color(0xFF0B57D0)
internal val LightOnPrimary = Color(0xFFFFFFFF)
internal val LightPrimaryContainer = Color(0xFFD3E3FD)
internal val LightOnPrimaryContainer = Color(0xFF041E49)
internal val LightTertiaryContainer = Color(0xFFFFD8E4)
internal val LightOnTertiaryContainer = Color(0xFF31111D)
internal val LightSurface = Color(0xFFFDFCFF)
internal val LightSurfaceContainerLow = Color(0xFFF7F9FF)
internal val LightSurfaceContainer = Color(0xFFF1F3FB)
internal val LightSurfaceContainerHigh = Color(0xFFEBEDF6)
internal val LightOnSurface = Color(0xFF1A1C1E)
internal val LightOnSurfaceVariant = Color(0xFF44474E)
internal val LightOutline = Color(0xFF74777F)
internal val LightOutlineVariant = Color(0xFFC4C6D0)
internal val LightErrorContainer = Color(0xFFFFDAD6)
internal val LightOnErrorContainer = Color(0xFF410002)
internal val LightError = Color(0xFFBA1A1A)

internal val DarkPrimary = Color(0xFFA8C7FA)
internal val DarkOnPrimary = Color(0xFF00315C)
internal val DarkPrimaryContainer = Color(0xFF00458F)
internal val DarkOnPrimaryContainer = Color(0xFFD3E3FD)
internal val DarkTertiaryContainer = Color(0xFF633B48)
internal val DarkOnTertiaryContainer = Color(0xFFFFD8E4)
internal val DarkSurface = Color(0xFF111318)
internal val DarkSurfaceContainerLow = Color(0xFF1A1C20)
internal val DarkSurfaceContainer = Color(0xFF1E2025)
internal val DarkSurfaceContainerHigh = Color(0xFF282A2F)
internal val DarkOnSurface = Color(0xFFE2E2E6)
internal val DarkOnSurfaceVariant = Color(0xFFC4C6D0)
internal val DarkOutline = Color(0xFF8E9099)
internal val DarkOutlineVariant = Color(0xFF44474E)
internal val DarkErrorContainer = Color(0xFF93000A)
internal val DarkOnErrorContainer = Color(0xFFFFDAD6)
internal val DarkError = Color(0xFFFFB4AB)

internal val RatingAmber = Color(0xFFE8A33D)

internal val CmpLightColorScheme =
    lightColorScheme(
        primary = LightPrimary,
        onPrimary = LightOnPrimary,
        primaryContainer = LightPrimaryContainer,
        onPrimaryContainer = LightOnPrimaryContainer,
        tertiaryContainer = LightTertiaryContainer,
        onTertiaryContainer = LightOnTertiaryContainer,
        surface = LightSurface,
        background = LightSurfaceContainerLow,
        surfaceContainerLow = LightSurfaceContainerLow,
        surfaceContainer = LightSurfaceContainer,
        surfaceContainerHigh = LightSurfaceContainerHigh,
        onSurface = LightOnSurface,
        onBackground = LightOnSurface,
        onSurfaceVariant = LightOnSurfaceVariant,
        outline = LightOutline,
        outlineVariant = LightOutlineVariant,
        error = LightError,
        errorContainer = LightErrorContainer,
        onErrorContainer = LightOnErrorContainer,
    )

internal val CmpDarkColorScheme =
    darkColorScheme(
        primary = DarkPrimary,
        onPrimary = DarkOnPrimary,
        primaryContainer = DarkPrimaryContainer,
        onPrimaryContainer = DarkOnPrimaryContainer,
        tertiaryContainer = DarkTertiaryContainer,
        onTertiaryContainer = DarkOnTertiaryContainer,
        surface = DarkSurface,
        background = DarkSurfaceContainerLow,
        surfaceContainerLow = DarkSurfaceContainerLow,
        surfaceContainer = DarkSurfaceContainer,
        surfaceContainerHigh = DarkSurfaceContainerHigh,
        onSurface = DarkOnSurface,
        onBackground = DarkOnSurface,
        onSurfaceVariant = DarkOnSurfaceVariant,
        outline = DarkOutline,
        outlineVariant = DarkOutlineVariant,
        error = DarkError,
        errorContainer = DarkErrorContainer,
        onErrorContainer = DarkOnErrorContainer,
    )

@Composable
fun CourseDealsWebTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) CmpDarkColorScheme else CmpLightColorScheme,
        content = content,
    )
}

private inline fun ImageVector.Builder.materialPath(
    pathBuilder: PathBuilder.() -> Unit,
): ImageVector.Builder =
    path(
        fill = SolidColor(Color.Black),
        fillAlpha = 1.0f,
        stroke = null,
        strokeAlpha = 1.0f,
        strokeLineWidth = 1.0f,
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Bevel,
        strokeLineMiter = 1.0f,
        pathBuilder = pathBuilder,
    )

internal object CmpIcons {
    val LogoMark: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_logo_mark",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f,
        ).apply {
            addPath(
                pathData =
                    PathParser().parsePathString(
                        "M 152,52 L 360,52 Q 396,52 396,88 L 396,332 L 256,444 L 116,332 L 116,88 Q 116,52 152,52 Z " +
                            "M 256,56 A 30,30 0 0,1 256,116 A 30,30 0 0,1 256,56 Z",
                    ).toNodes(),
                pathFillType = PathFillType.EvenOdd,
                fill = SolidColor(Color(0xFF0B57D0)),
            )
            addPath(
                pathData = PathParser().parsePathString("M 228,56 A 36,36 0 0,1 284,56").toNodes(),
                stroke = SolidColor(Color(0xFF0B57D0)),
                strokeLineWidth = 10f,
                strokeLineCap = StrokeCap.Round,
            )
            addPath(
                pathData = PathParser().parsePathString("M 226,86 a 30,30 0 1,1 60,0 a 30,30 0 1,1 -60,0").toNodes(),
                stroke = SolidColor(Color.White),
                strokeLineWidth = 6f,
            )
            addPath(
                pathData = PathParser().parsePathString("M 204,196 L 204,320 L 330,258 Z").toNodes(),
                fill = SolidColor(Color.White),
            )
        }.build()
    }

    val Google: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_google",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData =
                    PathParser().parsePathString(
                        "m22.766,12.248c0,-0.724 -0.064,-1.412 -0.174,-2.081l-10.358,0l0,4.134l5.931,0" +
                            "c-0.266,1.357 -1.045,2.503 -2.2,3.282l0,2.75l3.538,0" +
                            "c2.072,-1.916 3.263,-4.739 3.263,-8.085z",
                    ).toNodes(),
                fill = SolidColor(Color(0xFF4285F4)),
            )
            addPath(
                pathData =
                    PathParser().parsePathString(
                        "m12.234,23c2.97,0 5.454,-0.99 7.269,-2.668l-3.538,-2.75" +
                            "c-0.99,0.66 -2.246,1.063 -3.731,1.063 -2.869,0 -5.298,-1.934 -6.169,-4.547" +
                            "l-3.648,0l0,2.832c1.806,3.593 5.518,6.068 9.818,6.068z",
                    ).toNodes(),
                fill = SolidColor(Color(0xFF34A853)),
            )
            addPath(
                pathData =
                    PathParser().parsePathString(
                        "m6.065,14.099c-0.229,-0.66 -0.348,-1.366 -0.348,-2.099 0,-0.733 0.128,-1.439 0.348,-2.099" +
                            "l0,-2.832l-3.648,0c-0.752,1.485 -1.183,3.153 -1.183,4.932 " +
                            "0,1.778 0.431,3.447 1.183,4.932z",
                    ).toNodes(),
                fill = SolidColor(Color(0xFFFBBC05)),
            )
            addPath(
                pathData =
                    PathParser().parsePathString(
                        "m12.234,5.354c1.623,0 3.071,0.559 4.217,1.65l3.135,-3.135" +
                            "c-1.898,-1.778 -4.382,-2.869 -7.352,-2.869 -4.299,0 -8.012,2.475 -9.818,6.068" +
                            "l3.648,2.832c0.871,-2.612 3.3,-4.547 6.169,-4.547z",
                    ).toNodes(),
                fill = SolidColor(Color(0xFFEA4335)),
            )
        }.build()
    }

    val FilterList: ImageVector by lazy {
        ImageVector.Builder("Filled.FilterList", 24.dp, 24.dp, 24f, 24f).apply {
            materialPath {
                moveTo(10.0f, 18.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(3.0f, 6.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(18.0f)
                lineTo(21.0f, 6.0f)
                lineTo(3.0f, 6.0f)
                close()
                moveTo(6.0f, 13.0f)
                horizontalLineToRelative(12.0f)
                verticalLineToRelative(-2.0f)
                lineTo(6.0f, 11.0f)
                verticalLineToRelative(2.0f)
                close()
            }
        }.build()
    }

    val Group: ImageVector by lazy {
        ImageVector.Builder("Filled.Group", 24.dp, 24.dp, 24f, 24f).apply {
            materialPath {
                moveTo(16.0f, 11.0f)
                curveToRelative(1.66f, 0.0f, 2.99f, -1.34f, 2.99f, -3.0f)
                reflectiveCurveTo(17.66f, 5.0f, 16.0f, 5.0f)
                curveToRelative(-1.66f, 0.0f, -3.0f, 1.34f, -3.0f, 3.0f)
                reflectiveCurveToRelative(1.34f, 3.0f, 3.0f, 3.0f)
                close()
                moveTo(8.0f, 11.0f)
                curveToRelative(1.66f, 0.0f, 2.99f, -1.34f, 2.99f, -3.0f)
                reflectiveCurveTo(9.66f, 5.0f, 8.0f, 5.0f)
                curveTo(6.34f, 5.0f, 5.0f, 6.34f, 5.0f, 8.0f)
                reflectiveCurveToRelative(1.34f, 3.0f, 3.0f, 3.0f)
                close()
                moveTo(8.0f, 13.0f)
                curveToRelative(-2.33f, 0.0f, -7.0f, 1.17f, -7.0f, 3.5f)
                lineTo(1.0f, 19.0f)
                horizontalLineToRelative(14.0f)
                verticalLineToRelative(-2.5f)
                curveToRelative(0.0f, -2.33f, -4.67f, -3.5f, -7.0f, -3.5f)
                close()
                moveTo(16.0f, 13.0f)
                curveToRelative(-0.29f, 0.0f, -0.62f, 0.02f, -0.97f, 0.05f)
                curveToRelative(1.16f, 0.84f, 1.97f, 1.97f, 1.97f, 3.45f)
                lineTo(17.0f, 19.0f)
                horizontalLineToRelative(6.0f)
                verticalLineToRelative(-2.5f)
                curveToRelative(0.0f, -2.33f, -4.67f, -3.5f, -7.0f, -3.5f)
                close()
            }
        }.build()
    }

    val ChatBubbleOutline: ImageVector by lazy {
        ImageVector.Builder("Filled.ChatBubbleOutline", 24.dp, 24.dp, 24f, 24f).apply {
            materialPath {
                moveTo(20.0f, 2.0f)
                lineTo(4.0f, 2.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(18.0f)
                lineToRelative(4.0f, -4.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(22.0f, 4.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(20.0f, 16.0f)
                lineTo(6.0f, 16.0f)
                lineToRelative(-2.0f, 2.0f)
                lineTo(4.0f, 4.0f)
                horizontalLineToRelative(16.0f)
                verticalLineToRelative(12.0f)
                close()
            }
        }.build()
    }

    val Schedule: ImageVector by lazy {
        ImageVector.Builder("Filled.Schedule", 24.dp, 24.dp, 24f, 24f).apply {
            materialPath {
                moveTo(11.99f, 2.0f)
                curveTo(6.47f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.47f, 10.0f, 9.99f, 10.0f)
                curveTo(17.52f, 22.0f, 22.0f, 17.52f, 22.0f, 12.0f)
                reflectiveCurveTo(17.52f, 2.0f, 11.99f, 2.0f)
                close()
                moveTo(12.0f, 20.0f)
                curveToRelative(-4.42f, 0.0f, -8.0f, -3.58f, -8.0f, -8.0f)
                reflectiveCurveToRelative(3.58f, -8.0f, 8.0f, -8.0f)
                reflectiveCurveToRelative(8.0f, 3.58f, 8.0f, 8.0f)
                reflectiveCurveToRelative(-3.58f, 8.0f, -8.0f, 8.0f)
                close()
            }
            materialPath {
                moveTo(12.5f, 7.0f)
                horizontalLineTo(11.0f)
                verticalLineToRelative(6.0f)
                lineToRelative(5.25f, 3.15f)
                lineToRelative(0.75f, -1.23f)
                lineToRelative(-4.5f, -2.67f)
                close()
            }
        }.build()
    }

    val Timer: ImageVector by lazy {
        ImageVector.Builder("Filled.Timer", 24.dp, 24.dp, 24f, 24f).apply {
            materialPath {
                moveTo(9.0f, 1.0f)
                horizontalLineToRelative(6.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-6.0f)
                close()
            }
            materialPath {
                moveTo(19.03f, 7.39f)
                lineToRelative(1.42f, -1.42f)
                curveToRelative(-0.43f, -0.51f, -0.9f, -0.99f, -1.41f, -1.41f)
                lineToRelative(-1.42f, 1.42f)
                curveTo(16.07f, 4.74f, 14.12f, 4.0f, 12.0f, 4.0f)
                curveToRelative(-4.97f, 0.0f, -9.0f, 4.03f, -9.0f, 9.0f)
                curveToRelative(0.0f, 4.97f, 4.02f, 9.0f, 9.0f, 9.0f)
                reflectiveCurveToRelative(9.0f, -4.03f, 9.0f, -9.0f)
                curveTo(21.0f, 10.88f, 20.26f, 8.93f, 19.03f, 7.39f)
                close()
                moveTo(13.0f, 14.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineTo(8.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(14.0f)
                close()
            }
        }.build()
    }

    val LocalFireDepartment: ImageVector by lazy {
        ImageVector.Builder("Filled.LocalFireDepartment", 24.dp, 24.dp, 24f, 24f).apply {
            materialPath {
                moveTo(12.0f, 12.9f)
                lineToRelative(-2.13f, 2.09f)
                curveTo(9.31f, 15.55f, 9.0f, 16.28f, 9.0f, 17.06f)
                curveTo(9.0f, 18.68f, 10.35f, 20.0f, 12.0f, 20.0f)
                reflectiveCurveToRelative(3.0f, -1.32f, 3.0f, -2.94f)
                curveToRelative(0.0f, -0.78f, -0.31f, -1.52f, -0.87f, -2.07f)
                lineTo(12.0f, 12.9f)
                close()
            }
            materialPath {
                moveTo(16.0f, 6.0f)
                lineToRelative(-0.44f, 0.55f)
                curveTo(14.38f, 8.02f, 12.0f, 7.19f, 12.0f, 5.3f)
                verticalLineTo(2.0f)
                curveToRelative(0.0f, 0.0f, -8.0f, 4.0f, -8.0f, 11.0f)
                curveToRelative(0.0f, 2.92f, 1.56f, 5.47f, 3.89f, 6.86f)
                curveTo(7.33f, 19.07f, 7.0f, 18.1f, 7.0f, 17.06f)
                curveToRelative(0.0f, -1.32f, 0.52f, -2.56f, 1.47f, -3.5f)
                lineTo(12.0f, 10.1f)
                lineToRelative(3.53f, 3.47f)
                curveToRelative(0.95f, 0.93f, 1.47f, 2.17f, 1.47f, 3.5f)
                curveToRelative(0.0f, 1.02f, -0.31f, 1.96f, -0.85f, 2.75f)
                curveToRelative(1.89f, -1.15f, 3.29f, -3.06f, 3.71f, -5.3f)
                curveTo(20.52f, 10.97f, 18.79f, 7.62f, 16.0f, 6.0f)
                close()
            }
        }.build()
    }

    val SchoolFilled: ImageVector by lazy {
        ImageVector.Builder("Filled.School", 24.dp, 24.dp, 24f, 24f).apply {
            materialPath {
                moveTo(5.0f, 13.18f)
                verticalLineToRelative(4.0f)
                lineTo(12.0f, 21.0f)
                lineToRelative(7.0f, -3.82f)
                verticalLineToRelative(-4.0f)
                lineTo(12.0f, 17.0f)
                lineToRelative(-7.0f, -3.82f)
                close()
                moveTo(12.0f, 3.0f)
                lineTo(1.0f, 9.0f)
                lineToRelative(11.0f, 6.0f)
                lineToRelative(9.0f, -4.91f)
                verticalLineTo(17.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(9.0f)
                lineTo(12.0f, 3.0f)
                close()
            }
        }.build()
    }

    val SchoolOutlined: ImageVector by lazy {
        ImageVector.Builder("Outlined.School", 24.dp, 24.dp, 24f, 24f).apply {
            materialPath {
                moveTo(12.0f, 3.0f)
                lineTo(1.0f, 9.0f)
                lineToRelative(4.0f, 2.18f)
                verticalLineToRelative(6.0f)
                lineTo(12.0f, 21.0f)
                lineToRelative(7.0f, -3.82f)
                verticalLineToRelative(-6.0f)
                lineToRelative(2.0f, -1.09f)
                lineTo(21.0f, 17.0f)
                horizontalLineToRelative(2.0f)
                lineTo(23.0f, 9.0f)
                lineTo(12.0f, 3.0f)
                close()
                moveTo(18.82f, 9.0f)
                lineTo(12.0f, 12.72f)
                lineTo(5.18f, 9.0f)
                lineTo(12.0f, 5.28f)
                lineTo(18.82f, 9.0f)
                close()
                moveTo(17.0f, 15.99f)
                lineToRelative(-5.0f, 2.73f)
                lineToRelative(-5.0f, -2.73f)
                verticalLineToRelative(-3.72f)
                lineTo(12.0f, 15.0f)
                lineToRelative(5.0f, -2.73f)
                verticalLineToRelative(3.72f)
                close()
            }
        }.build()
    }

    val Bookmark: ImageVector by lazy {
        ImageVector.Builder("Filled.Bookmark", 24.dp, 24.dp, 24f, 24f).apply {
            materialPath {
                moveTo(17.0f, 3.0f)
                horizontalLineTo(7.0f)
                curveToRelative(-1.1f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
                lineTo(5.0f, 21.0f)
                lineToRelative(7.0f, -3.0f)
                lineToRelative(7.0f, 3.0f)
                verticalLineTo(5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
            }
        }.build()
    }

    val BookmarkBorder: ImageVector by lazy {
        ImageVector.Builder("Filled.BookmarkBorder", 24.dp, 24.dp, 24f, 24f).apply {
            materialPath {
                moveTo(17.0f, 3.0f)
                lineTo(7.0f, 3.0f)
                curveToRelative(-1.1f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
                lineTo(5.0f, 21.0f)
                lineToRelative(7.0f, -3.0f)
                lineToRelative(7.0f, 3.0f)
                lineTo(19.0f, 5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(17.0f, 18.0f)
                lineToRelative(-5.0f, -2.18f)
                lineTo(7.0f, 18.0f)
                lineTo(7.0f, 5.0f)
                horizontalLineToRelative(10.0f)
                verticalLineToRelative(13.0f)
                close()
            }
        }.build()
    }

    val ContentCopy: ImageVector by lazy {
        ImageVector.Builder("Filled.ContentCopy", 24.dp, 24.dp, 24f, 24f).apply {
            materialPath {
                moveTo(16.0f, 1.0f)
                lineTo(4.0f, 1.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(14.0f)
                horizontalLineToRelative(2.0f)
                lineTo(4.0f, 3.0f)
                horizontalLineToRelative(12.0f)
                lineTo(16.0f, 1.0f)
                close()
                moveTo(19.0f, 5.0f)
                lineTo(8.0f, 5.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(11.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(21.0f, 7.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(19.0f, 21.0f)
                lineTo(8.0f, 21.0f)
                lineTo(8.0f, 7.0f)
                horizontalLineToRelative(11.0f)
                verticalLineToRelative(14.0f)
                close()
            }
        }.build()
    }

    val Flag: ImageVector by lazy {
        ImageVector.Builder("Outlined.Flag", 24.dp, 24.dp, 24f, 24f).apply {
            materialPath {
                moveTo(12.36f, 6.0f)
                lineToRelative(0.4f, 2.0f)
                horizontalLineTo(18.0f)
                verticalLineToRelative(6.0f)
                horizontalLineToRelative(-3.36f)
                lineToRelative(-0.4f, -2.0f)
                horizontalLineTo(7.0f)
                verticalLineTo(6.0f)
                horizontalLineToRelative(5.36f)
                moveTo(14.0f, 4.0f)
                horizontalLineTo(5.0f)
                verticalLineToRelative(17.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(-7.0f)
                horizontalLineToRelative(5.6f)
                lineToRelative(0.4f, 2.0f)
                horizontalLineToRelative(7.0f)
                verticalLineTo(6.0f)
                horizontalLineToRelative(-5.6f)
                lineTo(14.0f, 4.0f)
                close()
            }
        }.build()
    }

    val Link: ImageVector by lazy {
        ImageVector.Builder("Filled.Link", 24.dp, 24.dp, 24f, 24f).apply {
            materialPath {
                moveTo(3.9f, 12.0f)
                curveToRelative(0.0f, -1.71f, 1.39f, -3.1f, 3.1f, -3.1f)
                horizontalLineToRelative(4.0f)
                lineTo(11.0f, 7.0f)
                lineTo(7.0f, 7.0f)
                curveToRelative(-2.76f, 0.0f, -5.0f, 2.24f, -5.0f, 5.0f)
                reflectiveCurveToRelative(2.24f, 5.0f, 5.0f, 5.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(-1.9f)
                lineTo(7.0f, 15.1f)
                curveToRelative(-1.71f, 0.0f, -3.1f, -1.39f, -3.1f, -3.1f)
                close()
                moveTo(8.0f, 13.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(-2.0f)
                lineTo(8.0f, 11.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(17.0f, 7.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineToRelative(1.9f)
                horizontalLineToRelative(4.0f)
                curveToRelative(1.71f, 0.0f, 3.1f, 1.39f, 3.1f, 3.1f)
                reflectiveCurveToRelative(-1.39f, 3.1f, -3.1f, 3.1f)
                horizontalLineToRelative(-4.0f)
                lineTo(13.0f, 17.0f)
                horizontalLineToRelative(4.0f)
                curveToRelative(2.76f, 0.0f, 5.0f, -2.24f, 5.0f, -5.0f)
                reflectiveCurveToRelative(-2.24f, -5.0f, -5.0f, -5.0f)
                close()
            }
        }.build()
    }

    val BrokenImage: ImageVector by lazy {
        ImageVector.Builder("Filled.BrokenImage", 24.dp, 24.dp, 24f, 24f).apply {
            materialPath {
                moveTo(21.0f, 5.0f)
                verticalLineToRelative(6.59f)
                lineToRelative(-3.0f, -3.01f)
                lineToRelative(-4.0f, 4.01f)
                lineToRelative(-4.0f, -4.0f)
                lineToRelative(-4.0f, 4.0f)
                lineToRelative(-3.0f, -3.01f)
                lineTo(3.0f, 5.0f)
                curveToRelative(0.0f, -1.1f, 0.9f, -2.0f, 2.0f, -2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, 0.9f, 2.0f, 2.0f)
                close()
                moveTo(18.0f, 11.42f)
                lineToRelative(3.0f, 3.01f)
                lineTo(21.0f, 19.0f)
                curveToRelative(0.0f, 1.1f, -0.9f, 2.0f, -2.0f, 2.0f)
                lineTo(5.0f, 21.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, -0.9f, -2.0f, -2.0f)
                verticalLineToRelative(-6.58f)
                lineToRelative(3.0f, 2.99f)
                lineToRelative(4.0f, -4.0f)
                lineToRelative(4.0f, 4.0f)
                lineToRelative(4.0f, -3.99f)
                close()
            }
        }.build()
    }
}

private val webImageCache = mutableMapOf<String, ImageBitmap>()
private const val MAX_IMAGE_CACHE_SIZE = 120

@Composable
fun WebNetworkImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    var bitmap by remember(url) { mutableStateOf(if (url != null) webImageCache[url] else null) }
    var failed by remember(url) { mutableStateOf(false) }

    LaunchedEffect(url) {
        if (url.isNullOrBlank()) {
            failed = true
            return@LaunchedEffect
        }
        if (bitmap == null) {
            val loaded =
                runCatching {
                    val bytes = fetchUrlBytes(url)
                    SkiaImage.makeFromEncoded(bytes).toComposeImageBitmap()
                }.getOrNull()
            if (loaded != null) {
                if (webImageCache.size >= MAX_IMAGE_CACHE_SIZE) {
                    webImageCache.clear()
                }
                webImageCache[url] = loaded
                bitmap = loaded
            } else {
                failed = true
            }
        }
    }

    val currentBitmap = bitmap
    if (currentBitmap != null) {
        Image(
            bitmap = currentBitmap,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier,
        )
    } else {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (failed) CmpIcons.BrokenImage else CmpIcons.LogoMark,
                contentDescription = contentDescription,
                tint =
                    if (failed) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                    } else {
                        Color.Unspecified
                    },
                modifier = Modifier.size(32.dp),
            )
        }
    }
}

private const val MILLIS_THRESHOLD = 10_000_000_000L
private const val MILLIS_PER_SECOND = 1_000L
private const val MILLIS_PER_MINUTE = 60_000L
private const val MILLIS_PER_HOUR = 3_600_000L
private const val MILLIS_PER_DAY = 86_400_000L

internal enum class WebExpiryUrgency {
    NORMAL,
    WARNING,
    CRITICAL,
}

internal data class WebCountdownState(
    val displayText: String,
    val urgency: WebExpiryUrgency,
    val isExpired: Boolean,
    val isUnknown: Boolean,
)

internal fun formatWebExpiry(expiredDate: Long?): WebCountdownState {
    if (expiredDate == null || expiredDate <= 0L) {
        return WebCountdownState("unknown", WebExpiryUrgency.NORMAL, isExpired = false, isUnknown = true)
    }
    val targetMillis = if (expiredDate > MILLIS_THRESHOLD) expiredDate else expiredDate * MILLIS_PER_SECOND
    val diffMillis = targetMillis - currentTimeMillis()
    if (diffMillis <= 0L) {
        return WebCountdownState("expired", WebExpiryUrgency.CRITICAL, isExpired = true, isUnknown = false)
    }
    val totalMinutes = diffMillis / MILLIS_PER_MINUTE
    val totalHours = diffMillis / MILLIS_PER_HOUR
    val totalDays = diffMillis / MILLIS_PER_DAY

    return when {
        diffMillis < MILLIS_PER_HOUR -> {
            val text =
                if (totalMinutes >= 1L) {
                    "Expiring soon: ${totalMinutes}m"
                } else {
                    "Expiring soon: ${(diffMillis / MILLIS_PER_SECOND).coerceAtLeast(1L)}s"
                }
            WebCountdownState(text, WebExpiryUrgency.CRITICAL, isExpired = false, isUnknown = false)
        }
        diffMillis < MILLIS_PER_DAY -> {
            val minutes = (diffMillis % MILLIS_PER_HOUR) / MILLIS_PER_MINUTE
            val hh = if (totalHours < 10) "0$totalHours" else "$totalHours"
            val mm = if (minutes < 10) "0$minutes" else "$minutes"
            WebCountdownState("${hh}h ${mm}m remaining", WebExpiryUrgency.WARNING, isExpired = false, isUnknown = false)
        }
        else -> {
            val text = if (totalDays <= 1L) "1 day left" else "$totalDays days left"
            WebCountdownState(text, WebExpiryUrgency.NORMAL, isExpired = false, isUnknown = false)
        }
    }
}

@Composable
fun WebCountdownTicker(
    expiredDate: Long?,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 10.sp,
) {
    var state by remember(expiredDate) { mutableStateOf(formatWebExpiry(expiredDate)) }

    LaunchedEffect(expiredDate) {
        while (!state.isExpired && !state.isUnknown) {
            delay(30_000L)
            state = formatWebExpiry(expiredDate)
        }
    }

    if (state.isUnknown) return

    val (containerColor, contentColor) =
        when {
            state.isExpired -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
            state.urgency == WebExpiryUrgency.CRITICAL ->
                MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
            state.urgency == WebExpiryUrgency.WARNING ->
                MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
            else -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        }

    val iconVector =
        when {
            state.isExpired -> CmpIcons.Schedule
            state.urgency == WebExpiryUrgency.CRITICAL -> CmpIcons.LocalFireDepartment
            state.urgency == WebExpiryUrgency.WARNING -> CmpIcons.Timer
            else -> CmpIcons.Schedule
        }

    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(8.dp))
                .background(containerColor)
                .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size((fontSize.value + 2).dp),
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = state.displayText,
                color = contentColor,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}

private const val RATING_TENTHS = 10
private const val THRESHOLD_THOUSANDS = 1000
private const val MINUTES_PER_HOUR = 60

internal fun formatRatingValue(rating: Float): String {
    val tenths = (rating * RATING_TENTHS).roundToInt()
    return "${tenths / RATING_TENTHS}.${tenths % RATING_TENTHS}"
}

internal fun formatStudentsCount(students: Int): String =
    when {
        students <= 1 -> "$students student"
        students > THRESHOLD_THOUSANDS -> "${students / THRESHOLD_THOUSANDS}K students"
        else -> "$students students"
    }

internal fun formatReviewsCount(reviews: Int): String =
    when {
        reviews <= 1 -> "$reviews review"
        reviews > THRESHOLD_THOUSANDS -> "${reviews / THRESHOLD_THOUSANDS}K reviews"
        else -> "$reviews reviews"
    }

internal fun formatContentLength(contentLength: Int?): String =
    when {
        contentLength == null -> "unknown"
        contentLength == 0 -> "Practice Test"
        contentLength < MINUTES_PER_HOUR -> "${contentLength}m"
        else -> {
            val hours = contentLength.toDouble() / MINUTES_PER_HOUR
            val formatted =
                if ((hours % 1.0) == 0.0) {
                    hours.toInt().toString()
                } else {
                    ((hours * RATING_TENTHS).roundToInt() / 10.0).toString()
                }
            "${formatted}h"
        }
    }

private const val MINUTES_PER_HOUR_LONG = 60L
private const val MINUTES_PER_DAY_LONG = 1440L

internal fun formatTimeAgo(timestampMillis: Long?): String {
    if (timestampMillis == null || timestampMillis <= 0L) return "just now"
    val normalizedMillis =
        if (timestampMillis > MILLIS_THRESHOLD) timestampMillis else timestampMillis * MILLIS_PER_SECOND
    val diffMinutes = ((currentTimeMillis() - normalizedMillis) / MILLIS_PER_MINUTE).coerceAtLeast(0L)
    return when {
        diffMinutes <= 1L -> "just now"
        diffMinutes <= MINUTES_PER_HOUR_LONG -> "$diffMinutes minutes ago"
        diffMinutes <= MINUTES_PER_DAY_LONG -> "${diffMinutes / MINUTES_PER_HOUR_LONG} hours ago"
        else -> "${diffMinutes / MINUTES_PER_DAY_LONG} days ago"
    }
}

internal fun stripHtmlToPlainText(html: String?): String {
    if (html.isNullOrBlank()) return ""
    return html
        .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("</p>", RegexOption.IGNORE_CASE), "\n\n")
        .replace(Regex("<li>", RegexOption.IGNORE_CASE), "• ")
        .replace(Regex("</li>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("<[^>]*>"), "")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&nbsp;", " ")
        .trim()
}
