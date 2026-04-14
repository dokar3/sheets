package com.dokar.sheets.sample

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController {
    var dark by remember { mutableStateOf(false) }

    SampleApp(
        isDarkTheme = dark,
        onUpdateDarkTheme = { dark = it },
    )
}
