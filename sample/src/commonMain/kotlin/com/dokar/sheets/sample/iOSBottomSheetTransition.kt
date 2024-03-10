package com.dokar.sheets.sample

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.dokar.sheets.BottomSheetState

fun Modifier.iosBottomSheetTransitions(
    state: BottomSheetState,
    statusBarInsets: WindowInsets,
): Modifier = graphicsLayer {
    val progress = (state.dragProgress - 0.5f) / 0.5f
    if (progress <= 0f) {
        return@graphicsLayer
    }

    val minScale = if (size.width > size.height) 0.95f else 0.92f

    val scale = 1f - (1f - minScale) * progress
    scaleX = scale
    scaleY = scale

    val statusBarHeight = statusBarInsets.getTop(this)
    val scaledTopSpacing = size.height * (1f - minScale) / 2f
    translationY = progress * (statusBarHeight +
            16.dp.toPx() - scaledTopSpacing)

    clip = true
    shape = RoundedCornerShape(progress * 16.dp)
}