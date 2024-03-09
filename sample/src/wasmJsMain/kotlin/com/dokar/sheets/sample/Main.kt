package com.dokar.sheets.sample

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        var dark by remember { mutableStateOf(false) }

        SampleScreen(
            isDarkTheme = dark,
            onUpdateDarkTheme = { dark = it },
        )
    }
}