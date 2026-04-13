package com.dokar.sheets.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material3.Text
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay

@Composable
internal actual fun SampleApp(
    isDarkTheme: Boolean,
    onUpdateDarkTheme: (Boolean) -> Unit,
) {
    val backStack = remember { mutableStateListOf<SampleRoute>(SampleRoute.Home) }

    NavDisplay(
        modifier = Modifier.fillMaxSize(),
        backStack = backStack,
        entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
        entryProvider = entryProvider {
            entry<SampleRoute.Home> {
                Box {
                    SampleScreen(
                        isDarkTheme = isDarkTheme,
                        onUpdateDarkTheme = onUpdateDarkTheme,
                        onOpenImeDemo = {
                            backStack.add(SampleRoute.ChatDemo)
                        },
                    )
                }
            }

            entry<SampleRoute.ChatDemo> {
                ImeExpandDemoScreen(
                    isDarkTheme = isDarkTheme,
                    onBack = {
                        if (backStack.size > 1) {
                            backStack.removeAt(backStack.lastIndex)
                        }
                    },
                )
            }
        },
    )
}

private sealed interface SampleRoute {
    data object Home : SampleRoute
    data object ChatDemo : SampleRoute
}
