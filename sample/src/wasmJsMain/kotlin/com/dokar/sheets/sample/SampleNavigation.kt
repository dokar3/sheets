package com.dokar.sheets.sample

import androidx.compose.runtime.Composable

@Composable
internal actual fun SampleApp(
    isDarkTheme: Boolean,
    onUpdateDarkTheme: (Boolean) -> Unit,
) {
    SampleScreen(
        isDarkTheme = isDarkTheme,
        onUpdateDarkTheme = onUpdateDarkTheme,
        showImeDemoButton = false,
    )
}
