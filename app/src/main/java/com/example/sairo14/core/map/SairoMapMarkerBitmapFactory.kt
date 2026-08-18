package com.example.sairo14.core.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.example.sairo14.R

/** Figma의 핀 리소스 위에 방문 순서를 그린 카카오 지도용 Bitmap을 만든다. */
internal class SairoMapMarkerBitmapFactory(
    private val context: Context,
) {
    private val markerBitmaps = mutableMapOf<Int, Bitmap>()
    private val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    fun create(order: Int): Bitmap = markerBitmaps.getOrPut(order) {
        val markerDrawable = checkNotNull(
            context.resources.getDrawable(R.drawable.ic_location_large, context.theme),
        )
        val width = markerDrawable.intrinsicWidth.coerceAtLeast(1)
        val height = markerDrawable.intrinsicHeight.coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        markerDrawable.setBounds(0, 0, width, height)
        markerDrawable.draw(canvas)

        numberPaint.textSize = width * MarkerNumberTextSizeRatio
        val textCenterY = height * MarkerNumberCenterYRatio
        val baseline = textCenterY - (numberPaint.descent() + numberPaint.ascent()) / 2f
        canvas.drawText(order.toString(), width / 2f, baseline, numberPaint)

        bitmap
    }
}

private const val MarkerNumberTextSizeRatio = 0.28f
private const val MarkerNumberCenterYRatio = 0.42f
