package com.dokar.sheets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

actual enum class SecureFlagPolicy {
    Inherit,
}

actual val defaultWindowSoftInputMode: Int = 0

actual val hasLightNavigationBar: Boolean = false

@OptIn(ExperimentalTime::class)
internal actual fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()

@Composable
internal actual fun isImeVisible(): Boolean = false

@Composable
internal actual fun SheetHost(
    state: BottomSheetState,
    behaviors: DialogSheetBehaviors,
    showAboveKeyboard: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    SideEffect {
        state.imeVisible = false
        state.hasImeVisibilityUpdated = true
    }

    Popup(
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(
            focusable = true,
            usePlatformInsets = false,
        ),
    ) {
        content()
    }
}
