package com.rio.terminal.presentation

enum class TimeFrame(
    val title: String,
    val value: String
) {
    M5("M5", "5/minute"),
    M15("M15", "5/minute"),
    M30("M30", "5/minute"),
    H1("H1", "1/hour"),
    H4("H4", "4/hour")
}