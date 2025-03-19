package com.rio.terminal.presentation

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import com.rio.terminal.data.Bar
import kotlinx.parcelize.Parcelize
import kotlin.math.roundToInt

@Parcelize
data class TerminalState(
    val barList: List<Bar>,
    val visibleBarCount: Int = 100,
    val terminalWidth: Float = 0f,
    val scrolledBy: Float = 0f
) : Parcelable {

    val barWidth: Float
        get() {
            return terminalWidth / visibleBarCount
        }

    val visibleBars: List<Bar>
        get() {
            val startIndex = (scrolledBy / barWidth).roundToInt().coerceAtLeast(0)
            val endIndex = (startIndex + visibleBarCount).coerceAtMost(barList.size)
            return barList.subList(startIndex, endIndex)
        }
}

@Composable
fun rememberTerminalState(barList: List<Bar>): MutableState<TerminalState> {
    return rememberSaveable {
        mutableStateOf(TerminalState(barList))
    }
}
