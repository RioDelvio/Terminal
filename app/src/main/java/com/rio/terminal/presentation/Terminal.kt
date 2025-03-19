package com.rio.terminal.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import com.rio.terminal.data.Bar
import kotlin.math.roundToInt

private const val MIN_BARS_COUNT = 20

@Composable
fun Terminal(bars: List<Bar>) {

    var visibleBarCount by remember {
        mutableIntStateOf(100)
    }

    var barWidth by remember {
        mutableFloatStateOf(0f)
    }

    var terminalWidth by remember {
        mutableFloatStateOf(0f)
    }

    var scrolledBy by remember {
        mutableFloatStateOf(0f)
    }

    val transformableState = TransformableState { zoomChange, panChange, _ ->
        visibleBarCount = (visibleBarCount / zoomChange).roundToInt()
            .coerceIn(MIN_BARS_COUNT, bars.size)

        scrolledBy = (scrolledBy + panChange.x)
            .coerceIn(0f, barWidth * bars.size - terminalWidth)
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .transformable(transformableState)
    ) {
        terminalWidth = size.width
        val max = bars.maxOf { it.high }
        val min = bars.minOf { it.low }
        val pxPerPoint = size.height / (max - min)
        barWidth = size.width / visibleBarCount
        translate(left = scrolledBy){
            bars.forEachIndexed { index, bar ->
                val offsetX = size.width - index * barWidth
                drawLine(
                    color = Color.White,
                    start = Offset(offsetX, size.height - ((bar.low - min) * pxPerPoint)),
                    end = Offset(offsetX, size.height - ((bar.high - min) * pxPerPoint)),
                    strokeWidth = 1f
                )
                drawLine(
                    color = if (bar.open > bar.close) Color.Red else Color.Green,
                    start = Offset(offsetX, size.height - ((bar.open - min) * pxPerPoint)),
                    end = Offset(offsetX, size.height - ((bar.close - min) * pxPerPoint)),
                    strokeWidth = barWidth / 2
                )
            }
        }

    }
}