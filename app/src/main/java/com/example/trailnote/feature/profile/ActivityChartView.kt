package com.example.trailnote.feature.profile

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.trailnote.R

class ActivityChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.trail_line)
        strokeWidth = 1f
    }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.profile_chart_line)
        strokeWidth = 4f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.trail_muted)
        textSize = 22f
    }

    private val values = mutableListOf(25, 14, 58, 74, 28, 16, 36, 40)
    private val xLabels = listOf("4/20", "4/27", "5/4", "5/11", "5/18")
    private val yLabels = listOf(100, 75, 50, 25, 0)

    fun setValues(nextValues: List<Int>) {
        values.clear()
        values.addAll(nextValues.ifEmpty { listOf(0) })
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width == 0 || height == 0) return

        val left = 58f
        val right = width - 16f
        val top = 20f
        val bottom = height - 30f
        val graphHeight = bottom - top

        yLabels.forEachIndexed { index, label ->
            val y = top + graphHeight * index / (yLabels.size - 1)
            canvas.drawLine(left, y, right, y, gridPaint)
            canvas.drawText(label.toString(), 8f, y + 7f, labelPaint)
        }

        xLabels.forEachIndexed { index, label ->
            val x = left + (right - left) * index / (xLabels.size - 1)
            canvas.drawText(label, x - 18f, height - 4f, labelPaint)
        }

        if (values.size < 2) return
        val path = Path()
        values.forEachIndexed { index, value ->
            val x = left + (right - left) * index / (values.size - 1)
            val normalized = value.coerceIn(0, 100) / 100f
            val y = bottom - graphHeight * normalized
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        canvas.drawPath(path, linePaint)
    }
}
