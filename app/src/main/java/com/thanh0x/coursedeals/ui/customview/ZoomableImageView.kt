package com.thanh0x.coursedeals.ui.customview

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView

private const val MIN_SCALE = 0.5f
private const val MAX_SCALE = 4f
private const val MATRIX_SIZE = 9

class ZoomableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {
    private var mode = ACTION_MODE.NONE
    private val viewMatrix: Matrix = Matrix()
    private val lastPoint = PointF()
    private var matrixValue = FloatArray(MATRIX_SIZE)
    private var saveScale = 1f
    private var right = 0f
    private var bottom = 0f
    private var originalBitmapWidth = 0f
    private var originalBitmapHeight = 0f
    private var mScaleDetector: ScaleGestureDetector? = null

    private var clickCount = 0
    private var startTime: Long = 0
    private var duration: Long = 0

    init {
        super.setClickable(true)
        mScaleDetector = ScaleGestureDetector(context, ScaleListener())
        matrixValue = FloatArray(MATRIX_SIZE)
        imageMatrix = viewMatrix
        scaleType = ScaleType.MATRIX
    }

    private fun fitCenter() {
        val drawable = drawable
        val bmWidth = drawable?.intrinsicWidth ?: 0
        val bmHeight = drawable?.intrinsicHeight ?: 0
        val width = measuredWidth.toFloat()
        val height = measuredHeight.toFloat()
        val scale = if (width > height) height / bmHeight else width / bmWidth
        viewMatrix.setScale(scale, scale)
        saveScale = 1f
        originalBitmapWidth = scale * bmWidth
        originalBitmapHeight = scale * bmHeight
        val redundantYSpace = height - originalBitmapHeight
        val redundantXSpace = width - originalBitmapWidth
        viewMatrix.postTranslate(redundantXSpace / 2, redundantYSpace / 2)
        imageMatrix = viewMatrix
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        fitCenter()
    }

    @Suppress("CyclomaticComplexMethod", "NestedBlockDepth")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        mScaleDetector!!.onTouchEvent(event)
        viewMatrix.getValues(matrixValue)
        val x = matrixValue[Matrix.MTRANS_X]
        val y = matrixValue[Matrix.MTRANS_Y]
        val currentPoint = PointF(event.x, event.y)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> handleActionDown(currentPoint)
            MotionEvent.ACTION_POINTER_DOWN -> {
                lastPoint.set(currentPoint)
                mode = ACTION_MODE.ZOOM
            }
            MotionEvent.ACTION_MOVE -> handleActionMove(currentPoint, x, y)
            MotionEvent.ACTION_UP -> handleActionUp()
            MotionEvent.ACTION_POINTER_UP -> mode = ACTION_MODE.NONE
        }
        imageMatrix = viewMatrix
        invalidate()
        return true
    }

    private fun handleActionDown(point: PointF) {
        startTime = System.currentTimeMillis()
        clickCount++
        lastPoint.set(point)
        mode = ACTION_MODE.DRAG
    }

    private fun handleActionMove(currentPoint: PointF, x: Float, y: Float) {
        if (mode == ACTION_MODE.ZOOM || (mode == ACTION_MODE.DRAG && saveScale > MIN_SCALE)) {
            var deltaX = currentPoint.x - lastPoint.x
            var deltaY = currentPoint.y - lastPoint.y
            val scaleWidth = Math.round(originalBitmapWidth * saveScale).toFloat()
            val scaleHeight = Math.round(originalBitmapHeight * saveScale).toFloat()

            val limitX: Boolean
            val limitY: Boolean

            if (scaleWidth < width && scaleHeight < height) {
                limitX = false
                limitY = false
            } else if (scaleWidth < width) {
                deltaX = 0f
                limitX = false
                limitY = true
            } else if (scaleHeight < height) {
                deltaY = 0f
                limitX = true
                limitY = false
            } else {
                limitX = true
                limitY = true
            }

            val finalDeltaX = if (limitX) calculateLimit(x, deltaX, right) else deltaX
            val finalDeltaY = if (limitY) calculateLimit(y, deltaY, bottom) else deltaY

            if (saveScale > 1.0f) {
                viewMatrix.postTranslate(finalDeltaX, finalDeltaY)
            }
            lastPoint.set(currentPoint)
        }
    }

    private fun calculateLimit(current: Float, delta: Float, limit: Float): Float {
        return if (current + delta > 0) {
            -current * 2
        } else if (current + delta < -limit) {
            -(current + limit) * 2
        } else {
            delta
        }
    }

    private fun handleActionUp() {
        val time = System.currentTimeMillis() - startTime
        duration += time
        if (clickCount == 2) {
            if (duration <= MAX_DURATION) {
                fitCenter()
            }
            clickCount = 0
            duration = 0
        }
        mode = ACTION_MODE.NONE
    }

    private enum class ACTION_MODE {
        NONE, DRAG, ZOOM
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            mode = ACTION_MODE.ZOOM
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactor = detector.scaleFactor
            val newScale = saveScale * scaleFactor
            if (newScale < MAX_SCALE && newScale > MIN_SCALE) {
                saveScale = newScale
                val width = width.toFloat()
                val height = height.toFloat()
                val scaledBitmapWidth = originalBitmapWidth * saveScale
                val scaledBitmapHeight = originalBitmapHeight * saveScale
                right = scaledBitmapWidth - width
                bottom = scaledBitmapHeight - height
                if (scaledBitmapWidth <= width || scaledBitmapHeight <= height) {
                    viewMatrix.postScale(scaleFactor, scaleFactor, width / 2, height / 2)
                } else {
                    viewMatrix.postScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)
                }
            }
            return true
        }
    }

    companion object {
        private const val MAX_DURATION = 200
    }
}
