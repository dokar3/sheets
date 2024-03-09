package com.dokar.sheets.sample

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dokar.sheets.BottomSheetState

@Composable
expect fun IntentPickerSheetContent(
    state: BottomSheetState,
    modifier: Modifier = Modifier,
)