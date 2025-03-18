package com.rio.terminal.presentation

import android.util.Log
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

    init {
        loadBarList()
    }

    private fun loadBarList() {
        viewModelScope.launch {
            try {
                val barList = apiService.loadBars()
                _state.value = TerminalScreenState.Content(barList)
            } catch (e: Exception) {
                Log.d("TerminalViewModel", "Exception caught: $e")
            }

        }
    }
}