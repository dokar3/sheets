package com.dokar.sheets.sample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
internal actual fun SampleApp(
    isDarkTheme: Boolean,
    onUpdateDarkTheme: (Boolean) -> Unit,
) {
    var currentScreen by remember { mutableStateOf(SampleScreenType.Home) }

    when (currentScreen) {
        SampleScreenType.Home -> {
            SampleScreen(
                isDarkTheme = isDarkTheme,
                onUpdateDarkTheme = onUpdateDarkTheme,
                showImeDemoButton = false,
                onOpenEmbeddedSheetDemo = {
                    currentScreen = SampleScreenType.EmbeddedSheet
                },
            )
        }

        SampleScreenType.EmbeddedSheet -> {
            EmbeddedSheetDemoScreen(
                isDarkTheme = isDarkTheme,
                onBack = {
                    currentScreen = SampleScreenType.Home
                },
            )
        }
    }
}

private enum class SampleScreenType {
    Home,
    EmbeddedSheet,
}
