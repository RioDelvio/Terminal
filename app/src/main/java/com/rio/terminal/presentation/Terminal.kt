package com.rio.terminal.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
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
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.roundToInt

private const val MIN_BARS_COUNT = 20

@Composable
fun Terminal(
    modifier: Modifier = Modifier
) {

    val viewModel: TerminalViewModel = viewModel()
    val screenState = viewModel.state.collectAsState()

    when (val currentState = screenState.value) {

        is TerminalScreenState.Initial -> {}

        is TerminalScreenState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is TerminalScreenState.Content -> {

            val terminalState = rememberTerminalState(barList = currentState.barList.barList)

            Chart(
                modifier = modifier,
                terminalState = terminalState,
                onTerminalStateChange = {
                    terminalState.value = it
                }
            )

            terminalState.value.visibleBars.firstOrNull()?.let {
                Prices(
                    modifier = modifier,
                    terminalState = terminalState,
                    lastPrice = it.close
                )
            }

            TimeFrames(
                selectedTimeFrame = currentState.timeFrame,
                onTimeFrameSelected = { viewModel.loadBarList(it) }
            )
        }
    }
}

@Composable
fun Chart(
    modifier: Modifier = Modifier,
    terminalState: State<TerminalState>,
    onTerminalStateChange: (TerminalState) -> Unit
) {
    val currentState = terminalState.value

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        val visibleBarCount = (currentState.visibleBarCount / zoomChange).roundToInt()
            .coerceIn(MIN_BARS_COUNT, currentState.barList.size)

        val scrolledBy = (currentState.scrolledBy + panChange.x)
            .coerceIn(
                0f,
                currentState.barWidth * currentState.barList.size - currentState.terminalWidth
            )

        onTerminalStateChange(
            currentState.copy(
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
                onTerminalStateChange(
                    currentState.copy(
                        terminalWidth = it.width.toFloat(),
                        terminalHeight = it.height.toFloat()
                    )
                )
            }
    ) {
        val min = currentState.min
        val pxPerPoint = currentState.pxPerPoint
        translate(left = currentState.scrolledBy) {
            currentState.barList.forEachIndexed { index, bar ->
                val offsetX = size.width - index * currentState.barWidth
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
                    strokeWidth = currentState.barWidth / 2
                )
            }
        }
    }
}

@Composable
fun TimeFrames(
    selectedTimeFrame: TimeFrame,
    onTimeFrameSelected: (TimeFrame) -> Unit
) {
    Row(
        modifier = Modifier
            .wrapContentSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TimeFrame.entries.forEach { timeFrame ->
            val isSelected = selectedTimeFrame == timeFrame
            AssistChip(
                onClick = { onTimeFrameSelected(timeFrame) },
                label = { Text(text = timeFrame.title) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (isSelected) Color.White else Color.Black,
                    labelColor = if (isSelected) Color.Black else Color.White
                )
            )
        }
    }
}

@Composable
fun Prices(
    modifier: Modifier = Modifier,
    terminalState: State<TerminalState>,
    lastPrice: Float
) {

    val currentState = terminalState.value
    val textMeasurer = rememberTextMeasurer()
    val max = currentState.max
    val min = currentState.min
    val pxPerPoint = currentState.pxPerPoint

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