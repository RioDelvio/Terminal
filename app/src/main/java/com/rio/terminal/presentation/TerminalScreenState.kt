package com.rio.terminal.presentation

import com.rio.terminal.data.Result

sealed class TerminalScreenState {

    data object Initial : TerminalScreenState()
    data class Content(val barList: Result) : TerminalScreenState()
}