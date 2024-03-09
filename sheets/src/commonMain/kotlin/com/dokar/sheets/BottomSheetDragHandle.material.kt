package com.dokar.sheets

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A bottom sheet drag handle will be displayed as a rounded rectangle.
 */
@Composable
fun BottomSheetDragHandle(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.onSurface.copy(
        alpha = 0.4f
    ),
    height: Dp = 24.dp,
    barWidth: Dp = 32.dp,
    barHeight: Dp = 4.dp,
) {
    CoreBottomSheetDragHandle(modifier, color, height, barWidth, barHeight)
}