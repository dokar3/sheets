package com.dokar.sheets

import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * A bottom sheet will be displayed in a dialog window.
 *
 * @param state The bottom sheet state. Call [rememberBottomSheetState] to create one.
 * @param modifier Modifier for bottom sheet content.
 * @param skipPeeked Skip the peeked state if set to true. Defaults to false.
 * @param peekHeight Peek height, could be a dp, px, or fraction value.
 * @param backgroundColor Background color for sheet content.
 * @param dimColor Dim color. Defaults to [Color.Black].
 * @param maxDimAmount Maximum dim amount. Defaults to 0.45f.
 * @param behaviors Dialog sheet behaviors. Including system bars, clicking, window input mode, etc.
 * @param contentAlignment The alignment of the content. Only works when the content width is
 * smaller than the screen width.
 * @param showAboveKeyboard Controls whether the whole bottom sheet should show above the soft
 * keyboard when the keyboard is shown. Defaults to false.
 * @param dragHandle Bottom sheet drag handle. A round bar was displayed by default.
 * @param content Sheet content.
 */
@Composable
fun CoreBottomSheet(
    state: BottomSheetState,
    modifier: Modifier = Modifier,
    skipPeeked: Boolean = false,
    peekHeight: PeekHeight = PeekHeight.fraction(0.5f),
    shape: Shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
    backgroundColor: Color = Color.White,
    dimColor: Color = Color.Black,
    maxDimAmount: Float = CoreBottomSheetDefaults.MaxDimAmount,
    behaviors: DialogSheetBehaviors = CoreBottomSheetDefaults.dialogSheetBehaviors(),
    contentAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    showAboveKeyboard: Boolean = false,
    dragHandle: @Composable () -> Unit = { CoreBottomSheetDragHandle() },
    content: @Composable () -> Unit
) {
    if (!state.visible) {
        return
    }
    val scope = rememberCoroutineScope()

    val currentState by rememberUpdatedState(state)
    val currentSkipPeek by rememberUpdatedState(skipPeeked)
    val currentPeekHeight by rememberUpdatedState(peekHeight)
    val currentShape by rememberUpdatedState(shape)
    val currentBackgroundColor by rememberUpdatedState(backgroundColor)
    val currentDimColor by rememberUpdatedState(dimColor)
    val currentMaxDimAmount by rememberUpdatedState(maxDimAmount)
    val currentDragHandle by rememberUpdatedState(dragHandle)
    val currentContent by rememberUpdatedState(content)

    val onDismissRequest: () -> Unit = remember(state) {
        {
            if (state.value == BottomSheetValue.Expanded
                && !state.shouldSkipPeekedState()
                && !state.isPeeking
            ) {
                scope.launch {
                    state.peek()
                }
            } else if (state.value != BottomSheetValue.Collapsed) {
                scope.launch {
                    state.collapse()
                }
            }
        }
    }

    SheetHost(
        state = state,
        behaviors = behaviors,
        showAboveKeyboard = showAboveKeyboard,
        onDismissRequest = onDismissRequest,
    ) {
        CoreBottomSheetLayout(
            state = currentState,
            modifier = if (showAboveKeyboard) modifier.imePadding() else modifier,
            skipPeeked = currentSkipPeek,
            peekHeight = currentPeekHeight,
            shape = currentShape,
            backgroundColor = currentBackgroundColor,
            dimColor = currentDimColor,
            maxDimAmount = currentMaxDimAmount,
            behaviors = behaviors,
            contentAlignment = contentAlignment,
            dragHandle = currentDragHandle,
            content = currentContent
        )
    }
}


/**
 * A bottom sheet will be displayed in a dialog window.
 *
 * @param state The bottom sheet state. Call [rememberBottomSheetState] to create one.
 * @param modifier Modifier for bottom sheet content.
 * @param skipPeeked Skip the peeked state if set to true. Defaults to false.
 * @param peekHeight Peek height, could be a dp, px, or fraction value.
 * @param backgroundColor Background color for sheet content.
 * @param dimColor Dim color. Defaults to [Color.Black].
 * @param maxDimAmount Maximum dim amount. Defaults to 0.45f.
 * @param behaviors Dialog sheet behaviors. Including system bars, clicking, window input mode, etc.
 * @param contentAlignment The alignment of the content. Only works when the content width is
 * smaller than the screen width.
 * @param showAboveKeyboard Controls whether the whole bottom sheet should show above the soft
 * keyboard when the keyboard is shown. Defaults to false.
 * @param dragHandle Bottom sheet drag handle. A round bar was displayed by default.
 * @param content Sheet content.
 */
@Composable
fun CoreBottomSheet(
    state: BottomSheetState,
    modifier: Modifier = Modifier,
    skipPeeked: Boolean = false,
    peekHeight: PeekHeight = PeekHeight.fraction(0.5f),
    shape: Shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
    backgroundColor: Brush,
    dimColor: Color = Color.Black,
    maxDimAmount: Float = CoreBottomSheetDefaults.MaxDimAmount,
    behaviors: DialogSheetBehaviors = CoreBottomSheetDefaults.dialogSheetBehaviors(),
    contentAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    showAboveKeyboard: Boolean = false,
    dragHandle: @Composable () -> Unit = { CoreBottomSheetDragHandle() },
    content: @Composable () -> Unit
) {
    if (!state.visible) {
        return
    }
    val scope = rememberCoroutineScope()

    val currentState by rememberUpdatedState(state)
    val currentSkipPeek by rememberUpdatedState(skipPeeked)
    val currentPeekHeight by rememberUpdatedState(peekHeight)
    val currentShape by rememberUpdatedState(shape)
    val currentBackgroundColor by rememberUpdatedState(backgroundColor)
    val currentDimColor by rememberUpdatedState(dimColor)
    val currentMaxDimAmount by rememberUpdatedState(maxDimAmount)
    val currentDragHandle by rememberUpdatedState(dragHandle)
    val currentContent by rememberUpdatedState(content)

    val onDismissRequest: () -> Unit = remember(state) {
        {
            if (state.value == BottomSheetValue.Expanded
                && !state.shouldSkipPeekedState()
                && !state.isPeeking
            ) {
                scope.launch {
                    state.peek()
                }
            } else if (state.value != BottomSheetValue.Collapsed) {
                scope.launch {
                    state.collapse()
                }
            }
        }
    }

    SheetHost(
        state = state,
        behaviors = behaviors,
        showAboveKeyboard = showAboveKeyboard,
        onDismissRequest = onDismissRequest,
    ) {
        CoreBottomSheetLayout(
            state = currentState,
            modifier = if (showAboveKeyboard) modifier.imePadding() else modifier,
            skipPeeked = currentSkipPeek,
            peekHeight = currentPeekHeight,
            shape = currentShape,
            backgroundColor = currentBackgroundColor,
            dimColor = currentDimColor,
            maxDimAmount = currentMaxDimAmount,
            behaviors = behaviors,
            contentAlignment = contentAlignment,
            dragHandle = currentDragHandle,
            content = currentContent
        )
    }
}
