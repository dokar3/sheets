package com.dokar.sheets.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import kotlin.math.max

@Composable
internal fun SheetContentLayout(
    modifier: Modifier = Modifier,
    state: SheetContentLayoutState = rememberSheetContentLayoutState(),
    alignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    contentOffsetY: (IntSize) -> Int = { 0 },
    content: @Composable () -> Unit,
) {
    Layout(
        content = content,
        modifier = modifier.onGloballyPositioned { state.coordinates = it },
    ) { measurables, constraints ->
        val placeable = measurables[0].measure(constraints)

        val width = max(constraints.minWidth, placeable.measuredWidth)
        val height = max(constraints.minHeight, placeable.measuredHeight)

        layout(width = width, height = constraints.maxHeight) {
            val offsetY = contentOffsetY(IntSize(width, height))
            val y = constraints.maxHeight - height + offsetY
            val x = alignment.align(
                size = width,
                space = constraints.maxWidth,
                layoutDirection = layoutDirection,
            )
            state.contentX = x
            state.contentY = y
            placeable.placeRelative(x = x, y = y)
        }
    }
}
