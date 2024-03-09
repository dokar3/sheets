package com.dokar.sheets.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dokar.sheets.BottomSheetState

@Composable
actual fun IntentPickerSheetContent(
    state: BottomSheetState,
    modifier: Modifier,
) {
    Box(modifier = modifier.padding(32.dp)) {
        Text("Not supported yet.")
    }
}