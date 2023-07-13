package com.dokar.sheets

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class SheetNestedScrollConnection(
    private val state: BottomSheetState,
    private val scope: CoroutineScope
) : NestedScrollConnection {
    private var preSheetValue: BottomSheetValue? = null

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        preSheetValue = state.value
        return if (source == NestedScrollSource.Drag && available.y < 0f) {
            onScroll(available)
        } else {
            Offset.Zero
        }
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        return if (source == NestedScrollSource.Drag && available.y > 0f) {
            onScroll(available)
        } else {
            Offset.Zero
        }
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        if (!state.isAnimating && state.offsetY != 0f) {
            state.onDragStopped()
        }
        val shouldConsumeFling = preSheetValue != BottomSheetValue.Expanded ||
                state.value == BottomSheetValue.Peeked
        return if (shouldConsumeFling) {
            available
        } else {
            Velocity.Zero
        }
    }

    private fun onScroll(available: Offset): Offset {
        val newOffset = state.offsetY + available.y
        return if (newOffset >= 0f) {
            scope.launch {
                state.addOffsetY(available.y)
            }
            Offset(0f, available.y)
        } else {
            Offset.Zero
        }
    }
}