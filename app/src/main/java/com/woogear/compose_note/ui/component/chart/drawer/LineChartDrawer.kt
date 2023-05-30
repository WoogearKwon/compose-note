package com.woogear.compose_note.ui.component.chart.drawer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.woogear.compose_note.ui.component.chart.WooChartHelper
import com.woogear.compose_note.ui.component.chart.drawer.WooChartDrawer.Companion.CHART_INDEX_0
import com.woogear.compose_note.ui.component.chart.drawer.WooChartDrawer.Companion.CHART_INDEX_1

class LineChartDrawer(
    private val pointerDiameter: Dp = 10.dp,
    private val lineThickness: Dp = 2.dp,
    private val pointerRadius: Dp = 4.dp,
    private val outerPointerColor: Color = Color.White,
    private val lineColor: Color = Color(0xFFB7B7B7),
    private val unfocusedColor: Color = Color(0xFFEFEFEF),
    private val shaderColor: Color = Color(0x1A000000),
    private val colorSafe: Color = Color(0xFF66D758),
    private val colorAlert: Color = Color(0xFFFFB430),
    private val colorBad: Color = Color(0xFFEC5C5C),
) : WooChartDrawer {

    private val linePainter = Paint().apply {
        this.color = lineColor
        this.style = PaintingStyle.Stroke
        this.isAntiAlias = true
    }

    private val pointerPaint = Paint().apply {
        style = PaintingStyle.Fill
        isAntiAlias = true
    }

    override fun drawChart(
        canvas: Canvas,
        drawScope: DrawScope,
        chartHelper: WooChartHelper,
        tapOffset: Offset?,
        pointerOffset: Offset?,
        onDrawSelectionMarker: (offset: Offset, index: Int) -> Unit,
    ) = with(drawScope) {
        val focusedIndex = if (chartHelper.focusIndex == CHART_INDEX_0) CHART_INDEX_0 else CHART_INDEX_1
        val unfocusedIndex = if (focusedIndex == CHART_INDEX_0) CHART_INDEX_1 else CHART_INDEX_0

        val offsets0 = chartHelper.offsets0
        val offsets1 = chartHelper.offsets1

        val focusedOffsets = if (focusedIndex == CHART_INDEX_0) offsets0 else offsets1
        val unfocusedOffsets = (if (focusedIndex == CHART_INDEX_0) offsets1 else offsets0)

        if (unfocusedOffsets != null) {
            val pathPair1 = chartHelper.calculatePaths(unfocusedOffsets)
            drawLineAndShader(canvas, chartHelper, pathPair1, pointerOffset, unfocusedIndex)
            drawPoints(
                canvas,
                chartHelper,
                unfocusedOffsets,
                unfocusedIndex,
                tapOffset,
                onDrawSelectionMarker
            )
        }

        if (focusedOffsets == null) return
        val pathPair2 = chartHelper.calculatePaths(focusedOffsets)
        drawLineAndShader(canvas, chartHelper, pathPair2, pointerOffset, focusedIndex)
        drawPoints(
            canvas,
            chartHelper,
            focusedOffsets,
            focusedIndex,
            tapOffset,
            onDrawSelectionMarker
        )
    }

    private fun DrawScope.drawLineAndShader(
        canvas: Canvas,
        chartHelper: WooChartHelper,
        pathPair: Pair<Path, Path>,
        pointerOffset: Offset?,
        index: Int,
    ) = with(this) {
        val lineThickness = lineThickness.toPx()
        val isFocused = chartHelper.focusIndex == index
        val lineColor = if (isFocused) lineColor else unfocusedColor
        linePainter.strokeWidth = lineThickness
        linePainter.color = lineColor

        if (isFocused) {
            drawPath(pathPair.second, shaderColor)
        }
        canvas.drawPath(pathPair.first, linePainter)
        drawSelectionMarker(canvas, chartHelper, pointerOffset, isFocused)
    }

    private fun DrawScope.drawPoints(
        canvas: Canvas,
        chartHelper: WooChartHelper,
        offsets: List<Offset?>,
        focusIndex: Int,
        tapOffset: Offset?,
        onDrawSelectionMarker: (offset: Offset, index: Int) -> Unit,
    ) = with(this) {
        val offsetsNotNull = offsets.filterNotNull()
        val focused = chartHelper.focusIndex == focusIndex
        val optimalWidth = if (pointerDiameter.toPx() > chartHelper.maxItemWidth)
            chartHelper.maxItemWidth else pointerDiameter.toPx()
        val halfWidth = optimalWidth / 2f
        val radius = pointerRadius.toPx()

        offsetsNotNull.forEachIndexed { index, offset ->
            if (focused && tapOffset != null) {
                tapOffset.checkSelection(offset, halfWidth, index, onDrawSelectionMarker)
            }

            val outerHalfWidth = halfWidth * 1.4f
            val outerRadius = radius * 1.4f
            pointerPaint.color = outerPointerColor
            canvas.drawPointer(offset, outerHalfWidth, outerRadius, pointerPaint)

            val n = index % 3
            val paintColor = when (n) {
                0 -> colorSafe
                1 -> colorAlert
                else -> colorBad
            }
            pointerPaint.color = if (focused) paintColor else unfocusedColor
            canvas.drawPointer(offset, halfWidth, radius, pointerPaint)
        }
    }

    private fun Canvas.drawPointer(offset: Offset, halfWidth: Float, radius: Float, paint: Paint) {
        drawRoundRect(
            left = offset.x - halfWidth,
            top = offset.y + halfWidth,
            right = offset.x + halfWidth,
            bottom = offset.y - halfWidth,
            radiusX = radius,
            radiusY = radius,
            paint = paint,
        )
    }
}