package com.dokar.sheets

import androidx.compose.runtime.Composable

expect enum class SecureFlagPolicy {
    Inherit,
}

expect val defaultWindowSoftInputMode: Int

expect val hasLightNavigationBar: Boolean

internal expect fun currentTimeMillis(): Long

@Composable
internal expect fun SheetHost(
    state: BottomSheetState,
    behaviors: DialogSheetBehaviors,
    showAboveKeyboard: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
)
