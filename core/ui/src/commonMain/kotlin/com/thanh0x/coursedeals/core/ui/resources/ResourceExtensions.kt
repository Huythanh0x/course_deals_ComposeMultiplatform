package com.thanh0x.coursedeals.core.ui.resources

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

fun getString(resource: StringResource): String {
    return resource.defaultText
}

@Composable
@ReadOnlyComposable
fun stringResource(resource: StringResource): String {
    return resource.defaultText
}

@Composable
@ReadOnlyComposable
fun stringResource(resource: StringResource, vararg formatArgs: Any): String {
    return formatMultiplatformString(resource.defaultText, *formatArgs)
}

fun formatMultiplatformString(template: String, vararg formatArgs: Any): String {
    var result = template
    formatArgs.forEachIndexed { index, arg ->
        val pos = index + 1
        if (result.contains("%$pos$.1f")) {
            val num = (arg as? Number)?.toDouble() ?: 0.0
            val rounded = ((num * 10.0).roundToInt() / 10.0).toString()
            result = result.replace("%$pos$.1f", rounded)
        }
        val formattedArg = when (arg) {
            is Long, is Int, is Short, is Byte -> {
                val num = (arg as Number).toLong()
                if (result.contains("%$pos\$02d")) {
                    val padded = if (num in 0..9) "0$num" else num.toString()
                    result = result.replace("%$pos\$02d", padded)
                }
                arg.toString()
            }
            else -> arg.toString()
        }
        result = result.replace("%$pos\$d", formattedArg)
        result = result.replace("%$pos\$s", formattedArg)
    }
    formatArgs.forEach { arg ->
        when {
            result.contains("%.1f") -> {
                val num = (arg as? Number)?.toDouble() ?: 0.0
                val rounded = ((num * 10.0).roundToInt() / 10.0).toString()
                result = result.replaceFirst("%.1f", rounded)
            }
            result.contains("%02d") -> {
                val num = (arg as? Number)?.toLong() ?: 0L
                val padded = if (num in 0..9) "0$num" else num.toString()
                result = result.replaceFirst("%02d", padded)
            }
            result.contains("%d") -> result = result.replaceFirst("%d", arg.toString())
            result.contains("%s") -> result = result.replaceFirst("%s", arg.toString())
        }
    }
    return result
}

@Composable
fun painterResource(resource: DrawableResource): Painter {
    val vector = remember(resource.name) {
        when (resource.name) {
            "ic_logo_mark" -> buildLogoMarkVector()
            "ic_google" -> buildGoogleVector()
            "ic_fingerprint" -> Icons.Default.Fingerprint
            "error_loading_image" -> Icons.Default.BrokenImage
            "ic_nav_home", "ic_nav_home_filled" -> Icons.Filled.Home
            "ic_nav_home_outlined" -> Icons.Outlined.Home
            "ic_nav_course", "ic_nav_course_filled" -> Icons.Filled.School
            "ic_nav_course_outlined" -> Icons.Outlined.School
            "ic_nav_profile", "ic_nav_profile_filled" -> Icons.Filled.Person
            "ic_nav_profile_outlined" -> Icons.Outlined.Person
            else -> Icons.Default.Info
        }
    }
    return rememberVectorPainter(vector)
}

private fun buildLogoMarkVector(): ImageVector {
    return ImageVector.Builder(
        name = "ic_logo_mark",
        defaultWidth = 512.dp,
        defaultHeight = 512.dp,
        viewportWidth = 512f,
        viewportHeight = 512f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString(
                "M 152,52 L 360,52 Q 396,52 396,88 L 396,332 L 256,444 L 116,332 L 116,88 Q 116,52 152,52 Z " +
                    "M 256,56 A 30,30 0 0,1 256,116 A 30,30 0 0,1 256,56 Z"
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

private fun buildGoogleVector(): ImageVector {
    return ImageVector.Builder(
        name = "ic_google",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString(
                "m22.766,12.248c0,-0.724 -0.064,-1.412 -0.174,-2.081l-10.358,0l0,4.134l5.931,0c-0.266,1.357 -1.045,2.503 -2.2,3.282l0,2.75l3.538,0c2.072,-1.916 3.263,-4.739 3.263,-8.085z"
            ).toNodes(),
            fill = SolidColor(Color(0xFF4285F4)),
        )
        addPath(
            pathData = PathParser().parsePathString(
                "m12.234,23c2.97,0 5.454,-0.99 7.269,-2.668l-3.538,-2.75c-0.99,0.66 -2.246,1.063 -3.731,1.063 -2.869,0 -5.298,-1.934 -6.169,-4.547l-3.648,0l0,2.832c1.806,3.593 5.518,6.068 9.818,6.068z"
            ).toNodes(),
            fill = SolidColor(Color(0xFF34A853)),
        )
        addPath(
            pathData = PathParser().parsePathString(
                "m6.065,14.099c-0.229,-0.66 -0.348,-1.366 -0.348,-2.099 0,-0.733 0.128,-1.439 0.348,-2.099l0,-2.832l-3.648,0c-0.752,1.485 -1.183,3.153 -1.183,4.932 0,1.778 0.431,3.447 1.183,4.932z"
            ).toNodes(),
            fill = SolidColor(Color(0xFFFBBC05)),
        )
        addPath(
            pathData = PathParser().parsePathString(
                "m12.234,5.354c1.623,0 3.071,0.559 4.217,1.65l3.135,-3.135c-1.898,-1.778 -4.382,-2.869 -7.352,-2.869 -4.299,0 -8.012,2.475 -9.818,6.068l3.648,2.832c0.871,-2.612 3.3,-4.547 6.169,-4.547z"
            ).toNodes(),
            fill = SolidColor(Color(0xFFEA4335)),
        )
    }.build()
}
