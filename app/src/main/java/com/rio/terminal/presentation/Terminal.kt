package com.rio.terminal.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rio.terminal.data.Bar
import kotlin.math.roundToInt

private const val MIN_BARS_COUNT = 20

@Composable
fun Terminal(
    modifier: Modifier = Modifier,
    bars: List<Bar>
) {

    var terminalState by rememberTerminalState(bars)

    Chart(
        modifier = modifier,
        terminalState = terminalState,
        onTerminalStateChange = {
            terminalState = it
        }
    )

    terminalState.visibleBars.firstOrNull()?.let {
        Prices(
            modifier = modifier,
            max = terminalState.max,
            min = terminalState.min,
            pxPerPoint = terminalState.pxPerPoint,
            lastPrice = it.close
        )
    }
}

@Composable
fun Chart(
    modifier: Modifier = Modifier,
    terminalState: TerminalState,
    onTerminalStateChange: (TerminalState) -> Unit
){
    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        val visibleBarCount = (terminalState.visibleBarCount / zoomChange).roundToInt()
            .coerceIn(MIN_BARS_COUNT, terminalState.barList.size)

        val scrolledBy = (terminalState.scrolledBy + panChange.x)
            .coerceIn(0f, terminalState.barWidth * terminalState.barList.size - terminalState.terminalWidth)

        onTerminalStateChange(
            terminalState.copy(
                visibleBarCount = visibleBarCount,
                scrolledBy = scrolledBy
            )
        )
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(
                top = 36.dp,
                bottom = 36.dp,
                end = 36.dp
            )
            .clipToBounds()
            .transformable(transformableState)
            .onSizeChanged {
                onTerminalStateChange( terminalState.copy(
                    terminalWidth = it.width.toFloat(),
                    terminalHeight = it.height.toFloat()
                ))
            }
    ) {
        val min = terminalState.min
        val pxPerPoint = terminalState.pxPerPoint
        translate(left = terminalState.scrolledBy) {
            terminalState.barList.forEachIndexed { index, bar ->
                val offsetX = size.width - index * terminalState.barWidth
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
                    strokeWidth = terminalState.barWidth / 2
                )
            }
        }
    }
}

@Composable
fun Prices(
    modifier: Modifier = Modifier,
    max: Float,
    min: Float,
    pxPerPoint: Float,
    lastPrice: Float
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .padding(vertical = 36.dp)
    ) {
        drawPrices(max, min, pxPerPoint, lastPrice, textMeasurer)
    }
}

private fun DrawScope.drawPrices(
    max: Float,
    min: Float,
    pxPerPoint: Float,
    lastPrice: Float,
    textMeasurer: TextMeasurer
) {
    //max
    drawDashedLine(
        start = Offset(0f, 0f),
        end = Offset(size.width, 0f)
    )
    drawTextPrice(
        price = max,
        offsetY = 0f,
        textMeasurer = textMeasurer
    )

    //lastPrice
    val lastPriceOffsetY = size.height - ((lastPrice - min) * pxPerPoint)
    drawDashedLine(
        start = Offset(0f, lastPriceOffsetY),
        end = Offset(size.width, lastPriceOffsetY)
    )
    drawTextPrice(
        price = lastPrice,
        offsetY = lastPriceOffsetY,
        textMeasurer = textMeasurer
    )

    //min
    drawDashedLine(
        start = Offset(0f, size.height),
        end = Offset(size.width, size.height)
    )
    drawTextPrice(
        price = min,
        offsetY = size.height,
        textMeasurer = textMeasurer
    )
}

private fun DrawScope.drawTextPrice(
    price: Float,
    offsetY: Float,
    textMeasurer: TextMeasurer
) {
    val textLayoutResult = textMeasurer.measure(
        text = price.toString(),
        style = TextStyle(
            color = Color.White,
            fontSize = 12.sp
        )
    )

    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(size.width - textLayoutResult.size.width - 10.dp.toPx(), offsetY)
    )
}

private fun DrawScope.drawDashedLine(
    color: Color = Color.White,
    start: Offset,
    end: Offset,
    strokeWidth: Float = 1f
) {
    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = strokeWidth,
        pathEffect = PathEffect.dashPathEffect(
            floatArrayOf(4.dp.toPx(), 4.dp.toPx())
        )
    )
}