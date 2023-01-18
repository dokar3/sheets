package com.dokar.sheets

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.max
import kotlin.math.min

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
 * @param dragHandle Bottom sheet drag handle. A round bar was displayed by default.
 * @param content Sheet content.
 */
@Composable
fun BottomSheet(
    state: BottomSheetState,
    modifier: Modifier = Modifier,
    skipPeeked: Boolean = false,
    peekHeight: PeekHeight = PeekHeight.fraction(0.5f),
    shape: Shape = MaterialTheme.shapes.medium.copy(
        bottomStart = CornerSize(0.dp),
        bottomEnd = CornerSize(0.dp)
    ),
    backgroundColor: Color = MaterialTheme.colors.surface,
    dimColor: Color = Color.Black,
    maxDimAmount: Float = BottomSheetDefaults.MaxDimAmount,
    behaviors: DialogSheetBehaviors = BottomSheetDefaults.dialogSheetBehaviors(),
    dragHandle: @Composable () -> Unit = { BottomSheetDragHandle() },
    content: @Composable () -> Unit
) {
    if (!state.visible) {
        return
    }
    val view = LocalView.current
    val composition = rememberCompositionContext()
    val scope = rememberCoroutineScope()

    val currentState by rememberUpdatedState(state)
    val currentSkipPeek by rememberUpdatedState(skipPeeked)
    val currentPeekHeight by rememberUpdatedState(peekHeight)
    val currentShape by rememberUpdatedState(shape)
    val currentBackgroundColor by rememberUpdatedState(backgroundColor)
    val currentDimColor by rememberUpdatedState(dimColor)
    val currentMaxDimAmount by rememberUpdatedState(maxDimAmount)
    val currentProperties by rememberUpdatedState(behaviors)
    val currentDragHandle by rememberUpdatedState(dragHandle)
    val currentContent by rememberUpdatedState(content)
    val dialogId = rememberSaveable { UUID.randomUUID() }

    val dialog = remember(view, state) {
        DialogWrapper(
            onDismissRequest = {
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
            },
            behaviors = behaviors,
            composeView = view,
            dialogId = dialogId,
        )
    }.apply {
        setContent(composition) {
            DialogLayout {
                BottomSheetLayout(
                    state = currentState,
                    modifier = modifier,
                    skipPeeked = currentSkipPeek,
                    peekHeight = currentPeekHeight,
                    shape = currentShape,
                    backgroundColor = currentBackgroundColor,
                    dimColor = currentDimColor,
                    maxDimAmount = currentMaxDimAmount,
                    behaviors = currentProperties,
                    dragHandle = currentDragHandle,
                    content = currentContent
                )
            }
        }
    }

    LaunchedEffect(behaviors) {
        dialog.updateParameters(behaviors)
    }

    LaunchedEffect(state.visible, dialog.isShowing) {
        if (state.visible && !dialog.isShowing) {
            dialog.show()
        }
    }

    DisposableEffect(state) {
        onDispose {
            state.contentHeight = 0
            dialog.dismiss()
            dialog.disposeComposition()
        }
    }
}

/**
 * A bottom sheet layout will be embedded in the composable directly.
 *
 * @param state The bottom sheet state. Call [rememberBottomSheetState] to create one.
 * @param modifier Modifier for bottom sheet content.
 * @param skipPeeked Skip the peeked state if set to true. Defaults to false.
 * @param peekHeight Peek height, could be a dp, px, or fraction value.
 * @param backgroundColor Background color for sheet content.
 * @param dimColor Dim color. Defaults to [Color.Black].
 * @param maxDimAmount Maximum dim amount. Defaults to 0.45f.
 * @param behaviors Dialog sheet behaviors. Including system bars, clicking, window input mode, etc.
 * @param dragHandle Bottom sheet drag handle. A round bar was displayed by default.
 * @param content Sheet content.
 */
@Composable
fun BottomSheetLayout(
    state: BottomSheetState,
    modifier: Modifier = Modifier,
    skipPeeked: Boolean = false,
    peekHeight: PeekHeight = PeekHeight.fraction(0.5f),
    shape: Shape = MaterialTheme.shapes.medium.copy(
        bottomStart = CornerSize(0.dp),
        bottomEnd = CornerSize(0.dp)
    ),
    backgroundColor: Color = MaterialTheme.colors.surface,
    dimColor: Color = Color.Black,
    maxDimAmount: Float = BottomSheetDefaults.MaxDimAmount,
    behaviors: SheetBehaviors = SheetBehaviors(),
    dragHandle: @Composable () -> Unit = { BottomSheetDragHandle() },
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val nestedScrollConnection = remember(state, coroutineScope) {
        SheetNestedScrollConnection(state, coroutineScope)
    }

    val dimAlpha = min(maxDimAmount, max(0f, state.dimAmount))

    val density = LocalDensity.current

    val initialOffsetY = remember { with(density) { 180.dp.toPx() } }

    val contentAlpha = remember(state) { Animatable(0f) }

    var gestureDownPos by remember { mutableStateOf(Offset.Zero) }

    val sheetLayoutState = rememberSheetContentLayoutState()

    val topInset = WindowInsets.Companion.statusBars

    SideEffect {
        state.peekHeight = peekHeight
        state.forceSkipPeeked = skipPeeked
        state.maxDimAmount = maxDimAmount
    }

    LaunchedEffect(state) {
        snapshotFlow { state.value }
            .map { it == BottomSheetValue.Collapsed }
            .distinctUntilChanged()
            .collect {
                contentAlpha.snapTo(0f)
            }
    }

    DisposableEffect(state) {
        onDispose {
            state.visible = false
            state.contentHeight = 0
            coroutineScope.launch {
                state.stopAnimation()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind { drawRect(color = dimColor.copy(alpha = dimAlpha)) }
            .nestedScroll(nestedScrollConnection)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    if (!behaviors.collapseOnClickOutside) {
                        return@clickable
                    }
                    if (state.dragVelocity < 1000f
                        && sheetLayoutState.isWithinContentBounds(gestureDownPos)
                    ) {
                        // Swiping up but the tap event was triggered, ignore
                        return@clickable
                    }
                    coroutineScope.launch { state.collapse() }
                },
            )
            .detectPointerPositionChanges(
                key = state,
                onDown = { gestureDownPos = it.copy(y = it.y - topInset.getTop(density)) },
                onPositionChanged = { state.addVelocity(it.y) },
                onGestureEnd = { state.resetVelocity() },
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .run {
                    if (!behaviors.extendsIntoStatusBar) {
                        windowInsetsPadding(WindowInsets.statusBars)
                    } else {
                        this
                    }
                }
                .alpha(contentAlpha.value.coerceIn(0f, 1f))
        ) {
            if (state.offsetY < 0f) {
                // When a spring animation is running, sheet content may leave the bottom edge,
                // so we show a background to cover the content behind the bottom sheet.
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .align(Alignment.BottomCenter)
                        .background(MaterialTheme.colors.surface)
                )
            }

            SheetContentLayout(
                state = sheetLayoutState,
                contentOffsetY = { size ->
                    if (state.contentHeight == size.height) {
                        return@SheetContentLayout state.offsetY.toInt()
                    }

                    state.swipeToDismissDy = min(
                        size.height / 3f,
                        with(density) { 160.dp.toPx() }
                    )

                    val isAnimating = state.isAnimating
                    state.contentHeight = size.height

                    when (state.value) {
                        BottomSheetValue.Expanded -> {
                            val offsetY = if (isAnimating) {
                                min(size.height, initialOffsetY.toInt())
                            } else {
                                0
                            }
                            coroutineScope.launch {
                                state.setOffsetY(offsetY, updateDimAmount = !isAnimating)
                                if (isAnimating) {
                                    val animSpec = state.expandAnimationSpec
                                    if (animSpec != null) {
                                        coroutineScope.launch {
                                            contentAlpha.animateTo(
                                                targetValue = 1f,
                                                animationSpec = animSpec,
                                            )
                                        }
                                        state.expand(animationSpec = animSpec)
                                    } else {
                                        coroutineScope.launch {
                                            contentAlpha.animateTo(1f)
                                        }
                                        state.expand()
                                    }
                                } else {
                                    contentAlpha.snapTo(1f)
                                }
                            }
                            return@SheetContentLayout offsetY
                        }

                        BottomSheetValue.Peeked -> {
                            val offsetY = if (isAnimating) {
                                size.height
                            } else {
                                size.height - state.getPeekHeightInPx().toInt()
                            }
                            coroutineScope.launch {
                                state.setOffsetY(offsetY, updateDimAmount = !isAnimating)
                                if (isAnimating) {
                                    val animSpec = state.peekAnimationSpec
                                    if (animSpec != null) {
                                        coroutineScope.launch {
                                            contentAlpha.animateTo(
                                                targetValue = 1f,
                                                animationSpec = animSpec,
                                            )
                                        }
                                        state.peek(animationSpec = animSpec)
                                    } else {
                                        coroutineScope.launch {
                                            contentAlpha.snapTo(1f)
                                        }
                                        state.peek()
                                    }
                                } else {
                                    contentAlpha.snapTo(1f)
                                }
                            }
                            return@SheetContentLayout offsetY
                        }

                        BottomSheetValue.Collapsed -> {
                            val offsetY = size.height
                            coroutineScope.launch {
                                state.addOffsetY(offsetY)
                            }
                            return@SheetContentLayout offsetY
                        }
                    }
                },
            ) {
                Column(
                    modifier = modifier
                        .fillMaxWidth()
                        .background(backgroundColor, shape = shape)
                        .run {
                            if (!behaviors.extendsIntoNavigationBar) {
                                windowInsetsPadding(WindowInsets.navigationBars)
                            } else {
                                this
                            }
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {},
                        )
                        .draggable(
                            state = rememberDraggableState { dy ->
                                coroutineScope.launch {
                                    state.addOffsetY(dy)
                                }
                            },
                            orientation = Orientation.Vertical,
                            onDragStarted = {},
                            onDragStopped = { state.onDragStopped() },
                        ),
                ) {
                    dragHandle()
                    content()
                }
            }
        }
    }
}

@Composable
private fun rememberSheetContentLayoutState(): SheetContentLayoutState {
    return remember { SheetContentLayoutState() }
}

private class SheetContentLayoutState {
    var contentY = 0

    fun isWithinContentBounds(offset: Offset): Boolean {
        return offset.y >= contentY
    }
}

@Composable
private fun SheetContentLayout(
    modifier: Modifier = Modifier,
    state: SheetContentLayoutState = rememberSheetContentLayoutState(),
    contentOffsetY: (IntSize) -> Int = { 0 },
    content: @Composable () -> Unit,
) {
    Layout(
        content = content,
        modifier = modifier,
    ) { measurables, constraints ->
        val placeable = measurables[0].measure(constraints)

        val width = max(constraints.minWidth, placeable.measuredWidth)
        val height = max(constraints.minHeight, placeable.measuredHeight)

        layout(width = constraints.maxWidth, height = constraints.maxHeight) {
            val offsetY = contentOffsetY(IntSize(width, height))
            val y = constraints.maxHeight - height + offsetY
            state.contentY = y
            placeable.placeRelative(x = 0, y = y)
        }
    }
}
