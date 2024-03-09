package com.dokar.sheets.sample

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "compose-sonner") {
        App()
    }
}

@Composable
fun App() {
    var dark by remember { mutableStateOf(false) }

    SampleScreen(
        isDarkTheme = dark,
        onUpdateDarkTheme = { dark = it },
    )
}

@Preview
@Composable
fun AppDesktopPreview() {
    App()
}