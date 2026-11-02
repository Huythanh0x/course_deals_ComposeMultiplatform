package androidx.compose.material.icons.outlined

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

private inline fun ImageVector.Builder.materialPath(
    fillAlpha: Float = 1.0f,
    strokeAlpha: Float = 1.0f,
    pathBuilder: PathBuilder.() -> Unit
): ImageVector.Builder = path(
    fill = SolidColor(Color.Black),
    fillAlpha = fillAlpha,
    stroke = null,
    strokeAlpha = strokeAlpha,
    strokeLineWidth = 1.0f,
    strokeLineCap = StrokeCap.Butt,
    strokeLineJoin = StrokeJoin.Bevel,
    strokeLineMiter = 1.0f,
    pathBuilder = pathBuilder
)

private var _school: ImageVector? = null
public val Icons.Outlined.School: ImageVector
    get() {
        if (_school != null) return _school!!
        val builder = ImageVector.Builder(
            name = "Outlined.School",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f
        )
        builder.materialPath {
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
        return builder.build().also { _school = it }
    }

private var _contentCopy: ImageVector? = null
public val Icons.Outlined.ContentCopy: ImageVector
    get() {
        if (_contentCopy != null) return _contentCopy!!
        val builder = ImageVector.Builder(
            name = "Outlined.ContentCopy",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f
        )
        builder.materialPath {
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
        return builder.build().also { _contentCopy = it }
    }

private var _flag: ImageVector? = null
public val Icons.Outlined.Flag: ImageVector
    get() {
        if (_flag != null) return _flag!!
        val builder = ImageVector.Builder(
            name = "Outlined.Flag",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f
        )
        builder.materialPath {
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
        return builder.build().also { _flag = it }
    }

