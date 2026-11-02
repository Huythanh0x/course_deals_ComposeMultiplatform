package androidx.compose.material.icons.filled

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

private var _schedule: ImageVector? = null
public val Icons.Filled.Schedule: ImageVector
    get() {
        if (_schedule != null) return _schedule!!
        val builder = ImageVector.Builder(
            name = "Filled.Schedule",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f
        )
        builder.materialPath {
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
        builder.materialPath {
            moveTo(12.5f, 7.0f)
            horizontalLineTo(11.0f)
            verticalLineToRelative(6.0f)
            lineToRelative(5.25f, 3.15f)
            lineToRelative(0.75f, -1.23f)
            lineToRelative(-4.5f, -2.67f)
            close()
        }
        return builder.build().also { _schedule = it }
    }

private var _localFireDepartment: ImageVector? = null
public val Icons.Filled.LocalFireDepartment: ImageVector
    get() {
        if (_localFireDepartment != null) return _localFireDepartment!!
        val builder = ImageVector.Builder(
            name = "Filled.LocalFireDepartment",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f
        )
        builder.materialPath {
            moveTo(12.0f, 12.9f)
            lineToRelative(-2.13f, 2.09f)
            curveTo(9.31f, 15.55f, 9.0f, 16.28f, 9.0f, 17.06f)
            curveTo(9.0f, 18.68f, 10.35f, 20.0f, 12.0f, 20.0f)
            reflectiveCurveToRelative(3.0f, -1.32f, 3.0f, -2.94f)
            curveToRelative(0.0f, -0.78f, -0.31f, -1.52f, -0.87f, -2.07f)
            lineTo(12.0f, 12.9f)
            close()
        }
        builder.materialPath {
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
        return builder.build().also { _localFireDepartment = it }
    }

private var _timer: ImageVector? = null
public val Icons.Filled.Timer: ImageVector
    get() {
        if (_timer != null) return _timer!!
        val builder = ImageVector.Builder(
            name = "Filled.Timer",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f
        )
        builder.materialPath {
            moveTo(9.0f, 1.0f)
            horizontalLineToRelative(6.0f)
            verticalLineToRelative(2.0f)
            horizontalLineToRelative(-6.0f)
            close()
        }
        builder.materialPath {
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
        return builder.build().also { _timer = it }
    }

private var _fingerprint: ImageVector? = null
public val Icons.Filled.Fingerprint: ImageVector
    get() {
        if (_fingerprint != null) return _fingerprint!!
        val builder = ImageVector.Builder(
            name = "Filled.Fingerprint",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f
        )
        builder.materialPath {
            moveTo(17.81f, 4.47f)
            curveToRelative(-0.08f, 0.0f, -0.16f, -0.02f, -0.23f, -0.06f)
            curveTo(15.66f, 3.42f, 14.0f, 3.0f, 12.01f, 3.0f)
            curveToRelative(-1.98f, 0.0f, -3.86f, 0.47f, -5.57f, 1.41f)
            curveToRelative(-0.24f, 0.13f, -0.54f, 0.04f, -0.68f, -0.2f)
            curveToRelative(-0.13f, -0.24f, -0.04f, -0.55f, 0.2f, -0.68f)
            curveTo(7.82f, 2.52f, 9.86f, 2.0f, 12.01f, 2.0f)
            curveToRelative(2.13f, 0.0f, 3.99f, 0.47f, 6.03f, 1.52f)
            curveToRelative(0.25f, 0.13f, 0.34f, 0.43f, 0.21f, 0.67f)
            curveToRelative(-0.09f, 0.18f, -0.26f, 0.28f, -0.44f, 0.28f)
            close()
            moveTo(3.5f, 9.72f)
            curveToRelative(-0.1f, 0.0f, -0.2f, -0.03f, -0.29f, -0.09f)
            curveToRelative(-0.23f, -0.16f, -0.28f, -0.47f, -0.12f, -0.7f)
            curveToRelative(0.99f, -1.4f, 2.25f, -2.5f, 3.75f, -3.27f)
            curveTo(9.98f, 4.04f, 14.0f, 4.03f, 17.15f, 5.65f)
            curveToRelative(1.5f, 0.77f, 2.76f, 1.86f, 3.75f, 3.25f)
            curveToRelative(0.16f, 0.22f, 0.11f, 0.54f, -0.12f, 0.7f)
            curveToRelative(-0.23f, 0.16f, -0.54f, 0.11f, -0.7f, -0.12f)
            curveToRelative(-0.9f, -1.26f, -2.04f, -2.25f, -3.39f, -2.94f)
            curveToRelative(-2.87f, -1.47f, -6.54f, -1.47f, -9.4f, 0.01f)
            curveToRelative(-1.36f, 0.7f, -2.5f, 1.7f, -3.4f, 2.96f)
            curveToRelative(-0.08f, 0.14f, -0.23f, 0.21f, -0.39f, 0.21f)
            close()
            moveTo(9.75f, 21.79f)
            curveToRelative(-0.13f, 0.0f, -0.26f, -0.05f, -0.35f, -0.15f)
            curveToRelative(-0.87f, -0.87f, -1.34f, -1.43f, -2.01f, -2.64f)
            curveToRelative(-0.69f, -1.23f, -1.05f, -2.73f, -1.05f, -4.34f)
            curveToRelative(0.0f, -2.97f, 2.54f, -5.39f, 5.66f, -5.39f)
            reflectiveCurveToRelative(5.66f, 2.42f, 5.66f, 5.39f)
            curveToRelative(0.0f, 0.28f, -0.22f, 0.5f, -0.5f, 0.5f)
            reflectiveCurveToRelative(-0.5f, -0.22f, -0.5f, -0.5f)
            curveToRelative(0.0f, -2.42f, -2.09f, -4.39f, -4.66f, -4.39f)
            curveToRelative(-2.57f, 0.0f, -4.66f, 1.97f, -4.66f, 4.39f)
            curveToRelative(0.0f, 1.44f, 0.32f, 2.77f, 0.93f, 3.85f)
            curveToRelative(0.64f, 1.15f, 1.08f, 1.64f, 1.85f, 2.42f)
            curveToRelative(0.19f, 0.2f, 0.19f, 0.51f, 0.0f, 0.71f)
            curveToRelative(-0.11f, 0.1f, -0.24f, 0.15f, -0.37f, 0.15f)
            close()
            moveTo(16.92f, 19.94f)
            curveToRelative(-1.19f, 0.0f, -2.24f, -0.3f, -3.1f, -0.89f)
            curveToRelative(-1.49f, -1.01f, -2.38f, -2.65f, -2.38f, -4.39f)
            curveToRelative(0.0f, -0.28f, 0.22f, -0.5f, 0.5f, -0.5f)
            reflectiveCurveToRelative(0.5f, 0.22f, 0.5f, 0.5f)
            curveToRelative(0.0f, 1.41f, 0.72f, 2.74f, 1.94f, 3.56f)
            curveToRelative(0.71f, 0.48f, 1.54f, 0.71f, 2.54f, 0.71f)
            curveToRelative(0.24f, 0.0f, 0.64f, -0.03f, 1.04f, -0.1f)
            curveToRelative(0.27f, -0.05f, 0.53f, 0.13f, 0.58f, 0.41f)
            curveToRelative(0.05f, 0.27f, -0.13f, 0.53f, -0.41f, 0.58f)
            curveToRelative(-0.57f, 0.11f, -1.07f, 0.12f, -1.21f, 0.12f)
            close()
            moveTo(14.91f, 22.0f)
            curveToRelative(-0.04f, 0.0f, -0.09f, -0.01f, -0.13f, -0.02f)
            curveToRelative(-1.59f, -0.44f, -2.63f, -1.03f, -3.72f, -2.1f)
            curveToRelative(-1.4f, -1.39f, -2.17f, -3.24f, -2.17f, -5.22f)
            curveToRelative(0.0f, -1.62f, 1.38f, -2.94f, 3.08f, -2.94f)
            curveToRelative(1.7f, 0.0f, 3.08f, 1.32f, 3.08f, 2.94f)
            curveToRelative(0.0f, 1.07f, 0.93f, 1.94f, 2.08f, 1.94f)
            reflectiveCurveToRelative(2.08f, -0.87f, 2.08f, -1.94f)
            curveToRelative(0.0f, -3.77f, -3.25f, -6.83f, -7.25f, -6.83f)
            curveToRelative(-2.84f, 0.0f, -5.44f, 1.58f, -6.61f, 4.03f)
            curveToRelative(-0.39f, 0.81f, -0.59f, 1.76f, -0.59f, 2.8f)
            curveToRelative(0.0f, 0.78f, 0.07f, 2.01f, 0.67f, 3.61f)
            curveToRelative(0.1f, 0.26f, -0.03f, 0.55f, -0.29f, 0.64f)
            curveToRelative(-0.26f, 0.1f, -0.55f, -0.04f, -0.64f, -0.29f)
            curveToRelative(-0.49f, -1.31f, -0.73f, -2.61f, -0.73f, -3.96f)
            curveToRelative(0.0f, -1.2f, 0.23f, -2.29f, 0.68f, -3.24f)
            curveToRelative(1.33f, -2.79f, 4.28f, -4.6f, 7.51f, -4.6f)
            curveToRelative(4.55f, 0.0f, 8.25f, 3.51f, 8.25f, 7.83f)
            curveToRelative(0.0f, 1.62f, -1.38f, 2.94f, -3.08f, 2.94f)
            reflectiveCurveToRelative(-3.08f, -1.32f, -3.08f, -2.94f)
            curveToRelative(0.0f, -1.07f, -0.93f, -1.94f, -2.08f, -1.94f)
            reflectiveCurveToRelative(-2.08f, 0.87f, -2.08f, 1.94f)
            curveToRelative(0.0f, 1.71f, 0.66f, 3.31f, 1.87f, 4.51f)
            curveToRelative(0.95f, 0.94f, 1.86f, 1.46f, 3.27f, 1.85f)
            curveToRelative(0.27f, 0.07f, 0.42f, 0.35f, 0.35f, 0.61f)
            curveToRelative(-0.05f, 0.23f, -0.26f, 0.38f, -0.47f, 0.38f)
            close()
        }
        return builder.build().also { _fingerprint = it }
    }

private var _brokenImage: ImageVector? = null
public val Icons.Filled.BrokenImage: ImageVector
    get() {
        if (_brokenImage != null) return _brokenImage!!
        val builder = ImageVector.Builder(
            name = "Filled.BrokenImage",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f
        )
        builder.materialPath {
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
        return builder.build().also { _brokenImage = it }
    }

private var _school: ImageVector? = null
public val Icons.Filled.School: ImageVector
    get() {
        if (_school != null) return _school!!
        val builder = ImageVector.Builder(
            name = "Filled.School",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f
        )
        builder.materialPath {
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
        return builder.build().also { _school = it }
    }

private var _contentCopy: ImageVector? = null
public val Icons.Filled.ContentCopy: ImageVector
    get() {
        if (_contentCopy != null) return _contentCopy!!
        val builder = ImageVector.Builder(
            name = "Filled.ContentCopy",
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

private var _group: ImageVector? = null
public val Icons.Filled.Group: ImageVector
    get() {
        if (_group != null) return _group!!
        val builder = ImageVector.Builder(
            name = "Filled.Group",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f
        )
        builder.materialPath {
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
        return builder.build().also { _group = it }
    }

private var _chatBubbleOutline: ImageVector? = null
public val Icons.Filled.ChatBubbleOutline: ImageVector
    get() {
        if (_chatBubbleOutline != null) return _chatBubbleOutline!!
        val builder = ImageVector.Builder(
            name = "Filled.ChatBubbleOutline",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f
        )
        builder.materialPath {
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
        return builder.build().also { _chatBubbleOutline = it }
    }

private var _bookmark: ImageVector? = null
public val Icons.Filled.Bookmark: ImageVector
    get() {
        if (_bookmark != null) return _bookmark!!
        val builder = ImageVector.Builder(
            name = "Filled.Bookmark",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f
        )
        builder.materialPath {
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
        return builder.build().also { _bookmark = it }
    }

private var _bookmarkBorder: ImageVector? = null
public val Icons.Filled.BookmarkBorder: ImageVector
    get() {
        if (_bookmarkBorder != null) return _bookmarkBorder!!
        val builder = ImageVector.Builder(
            name = "Filled.BookmarkBorder",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f
        )
        builder.materialPath {
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
        return builder.build().also { _bookmarkBorder = it }
    }

private var _filterList: ImageVector? = null
public val Icons.Filled.FilterList: ImageVector
    get() {
        if (_filterList != null) return _filterList!!
        val builder = ImageVector.Builder(
            name = "Filled.FilterList",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f
        )
        builder.materialPath {
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
        return builder.build().also { _filterList = it }
    }

private var _history: ImageVector? = null
public val Icons.Filled.History: ImageVector
    get() {
        if (_history != null) return _history!!
        val builder = ImageVector.Builder(
            name = "Filled.History",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f
        )
        builder.materialPath {
            moveTo(13.0f, 3.0f)
            curveToRelative(-4.97f, 0.0f, -9.0f, 4.03f, -9.0f, 9.0f)
            lineTo(1.0f, 12.0f)
            lineToRelative(3.89f, 3.89f)
            lineToRelative(0.07f, 0.14f)
            lineTo(9.0f, 12.0f)
            lineTo(6.0f, 12.0f)
            curveToRelative(0.0f, -3.87f, 3.13f, -7.0f, 7.0f, -7.0f)
            reflectiveCurveToRelative(7.0f, 3.13f, 7.0f, 7.0f)
            reflectiveCurveToRelative(-3.13f, 7.0f, -7.0f, 7.0f)
            curveToRelative(-1.93f, 0.0f, -3.68f, -0.79f, -4.94f, -2.06f)
            lineToRelative(-1.42f, 1.42f)
            curveTo(8.27f, 19.99f, 10.51f, 21.0f, 13.0f, 21.0f)
            curveToRelative(4.97f, 0.0f, 9.0f, -4.03f, 9.0f, -9.0f)
            reflectiveCurveToRelative(-4.03f, -9.0f, -9.0f, -9.0f)
            close()
            moveTo(12.0f, 8.0f)
            verticalLineToRelative(5.0f)
            lineToRelative(4.28f, 2.54f)
            lineToRelative(0.72f, -1.21f)
            lineToRelative(-3.5f, -2.08f)
            lineTo(13.5f, 8.0f)
            lineTo(12.0f, 8.0f)
            close()
        }
        return builder.build().also { _history = it }
    }

private var _link: ImageVector? = null
public val Icons.Filled.Link: ImageVector
    get() {
        if (_link != null) return _link!!
        val builder = ImageVector.Builder(
            name = "Filled.Link",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f
        )
        builder.materialPath {
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
        return builder.build().also { _link = it }
    }

