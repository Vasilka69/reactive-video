package ru.vasili4.reactivevideo.ui.common.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.max
import ru.vasili4.reactivevideo.data.model.RecognitionLabel

class AnnotationOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF5722")
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 36f
    }

    private val labelBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CCFF5722")
        style = Paint.Style.FILL
    }

    private var sourceWidth: Int = 1
    private var sourceHeight: Int = 1
    private var annotations: List<RecognitionLabel> = emptyList()
    private var annotationsVisible: Boolean = true

    fun setSourceSize(width: Int, height: Int) {
        sourceWidth = max(width, 1)
        sourceHeight = max(height, 1)
        invalidate()
    }

    fun setAnnotations(value: List<RecognitionLabel>) {
        annotations = value
        invalidate()
    }

    fun toggleVisibility(): Boolean {
        annotationsVisible = !annotationsVisible
        invalidate()
        return annotationsVisible
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!annotationsVisible) {
            return
        }
        val scaleX = width / sourceWidth.toFloat()
        val scaleY = height / sourceHeight.toFloat()
        annotations.forEach { label ->
            if (label.coords.size < 4) {
                return@forEach
            }
            val left = label.coords[0] * scaleX
            val top = label.coords[1] * scaleY
            val right = label.coords[2] * scaleX
            val bottom = label.coords[3] * scaleY
            val rect = RectF(left, top, right, bottom)
            canvas.drawRect(rect, boxPaint)

            val text = label.name
            val textWidth = textPaint.measureText(text)
            val textHeight = textPaint.textSize
            val bgRect = RectF(left, max(0f, top - textHeight - 18f), left + textWidth + 24f, top)
            canvas.drawRoundRect(bgRect, 10f, 10f, labelBgPaint)
            canvas.drawText(text, left + 12f, bgRect.bottom - 10f, textPaint)
        }
    }
}
