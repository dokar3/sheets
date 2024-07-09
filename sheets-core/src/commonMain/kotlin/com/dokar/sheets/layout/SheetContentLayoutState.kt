package com.dokar.sheets.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates

class SheetContentLayoutState {
    var coordinates: LayoutCoordinates? = null

    var contentX = 0
    var contentY = 0

    fun isWithinContentBounds(offset: Offset): Boolean {
        val coordinates = this.coordinates ?: return offset.y >= contentY
        val offsetInRoot = coordinates.localToRoot(Offset.Zero)
        val x = offset.x
        val y = offset.y
        return x >= offsetInRoot.x + contentX &&
                x <= offsetInRoot.x + contentX + coordinates.size.width &&
                y >= offsetInRoot.y + contentY &&
                y <= offsetInRoot.y + contentY + coordinates.size.height
    }
}

@Composable
fun rememberSheetContentLayoutState(): SheetContentLayoutState {
    return remember { SheetContentLayoutState() }
}
