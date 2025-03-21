package com.rio.terminal.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.terminal.data.ApiFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TerminalViewModel : ViewModel() {

    private val apiService = ApiFactory.apiService

    private val _state = MutableStateFlow<TerminalScreenState>(TerminalScreenState.Initial)
    val state = _state.asStateFlow()

    private var lastState: TerminalScreenState = TerminalScreenState.Initial

    init {
        loadBarList()
    }

    fun loadBarList(timeFrame: TimeFrame = TimeFrame.H1) {
        lastState = _state.value
        _state.value = TerminalScreenState.Loading
        viewModelScope.launch {
            try {
                val barList = apiService.loadBars(timeFrame.value)
                _state.value = TerminalScreenState.Content(barList, timeFrame)
            } catch (e: Exception) {
                _state.value = lastState
            }

        }
    }
}