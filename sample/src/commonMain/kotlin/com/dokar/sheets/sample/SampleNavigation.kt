package com.dokar.sheets.sample

import androidx.compose.runtime.Composable

@Composable
internal expect fun SampleApp(
    isDarkTheme: Boolean,
    onUpdateDarkTheme: (Boolean) -> Unit,
)
