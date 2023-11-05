package com.dokar.sheets

import android.view.WindowManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
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
    val currentDragHandle by rememberUpdatedState(dragHandle)
    val currentContent by rememberUpdatedState(content)
    val dialogId = rememberSaveable { UUID.randomUUID() }

    val layoutDirection = LocalLayoutDirection.current

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

    val (finalModifier, finalBehaviors) = remember(modifier, behaviors, showAboveKeyboard) {
        if (showAboveKeyboard) {
            @Suppress("DEPRECATION")
            modifier.imePadding() to behaviors.copy(
                dialogWindowSoftInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE,
            )
        } else {
            if (behaviors.dialogWindowSoftInputMode ==
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED
            ) {
                modifier to behaviors.copy(
                    dialogWindowSoftInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN,
                )
            } else {
                modifier to behaviors
            }
        }
    }

    val dialog = remember(view, state) {
        DialogWrapper(
            onDismissRequest = onDismissRequest,
            behaviors = finalBehaviors,
            composeView = view,
            layoutDirection = layoutDirection,
            dialogId = dialogId,
        )
    }.apply {
        setContent(composition) {
            DialogLayout {
                CoreBottomSheetLayout(
                    state = currentState,
                    modifier = finalModifier,
                    skipPeeked = currentSkipPeek,
                    peekHeight = currentPeekHeight,
                    shape = currentShape,
                    backgroundColor = currentBackgroundColor,
                    dimColor = currentDimColor,
                    maxDimAmount = currentMaxDimAmount,
                    behaviors = finalBehaviors,
                    contentAlignment = contentAlignment,
                    dragHandle = currentDragHandle,
                    content = currentContent
                )
            }
        }
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

    SideEffect {
        dialog.updateParameters(
            onDismissRequest = onDismissRequest,
            behaviors = finalBehaviors,
            layoutDirection = layoutDirection
        )
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
 * @param contentAlignment The alignment of the content. Only works when the content width is
 * smaller than the screen width.
 * @param dragHandle Bottom sheet drag handle. A round bar was displayed by default.
 * @param content Sheet content.
 */
@Composable
fun CoreBottomSheetLayout(
    state: BottomSheetState,
    modifier: Modifier = Modifier,
    skipPeeked: Boolean = false,
    peekHeight: PeekHeight = PeekHeight.fraction(0.5f),
    shape: Shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
    backgroundColor: Color = Color.White,
    dimColor: Color = Color.Black,
    maxDimAmount: Float = CoreBottomSheetDefaults.MaxDimAmount,
    behaviors: SheetBehaviors = SheetBehaviors(),
    contentAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    dragHandle: @Composable () -> Unit = { CoreBottomSheetDragHandle() },
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val nestedScrollConnection = remember(state, coroutineScope) {
        SheetNestedScrollConnection(state, coroutineScope)
    }

    val dimAlpha = min(maxDimAmount, max(0f, state.dimAmount))

    val density = LocalDensity.current

    val initialOffsetY = with(density) { 180.dp.toPx() }

    val contentAlpha = remember(state) { Animatable(0f) }

    var gestureDownPos by remember { mutableStateOf(Offset.Zero) }

    val sheetLayoutState = rememberSheetContentLayoutState()

    val topInset = WindowInsets.Companion.statusBars

    var contentRect by remember { mutableStateOf(Rect.Zero) }

    SideEffect {
        state.peekHeight = peekHeight
        state.forceSkipPeeked = skipPeeked
        state.maxDimAmount = maxDimAmount
    }

    LaunchedEffect(state) {
        launch {
            snapshotFlow { state.value }
                .distinctUntilChanged()
                .filter { it == BottomSheetValue.Collapsed }
                .collect { contentAlpha.snapTo(0f) }
        }
        launch {
            snapshotFlow { state.isAnimating }
                .filter { it }
                .collect { contentAlpha.snapTo(1f) }
        }
    }

    DisposableEffect(state) {
        onDispose {
            state.visible = false
            state.contentHeight = 0
            coroutineScope.launch {
                state.stopAnimations()
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
                onDown = {
                    state.resetVelocity()
                    gestureDownPos =
                        it.copy(y = it.y - topInset.getTop(density))
                },
                onPositionChanged = { state.addVelocity(it.y) },
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
                .alpha(contentAlpha.value.coerceIn(0f, 1f)),
        ) {
            if (state.offsetY < 0f) {
                // When a spring animation is running, sheet content may leave the bottom edge,
                // so we show a background to cover the content behind the bottom sheet.
                Spacer(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .width(with(density) { contentRect.width.toDp() })
                        .offset { IntOffset(x = contentRect.left.toInt(), 0) }
                        .height(36.dp)
                        .background(color = backgroundColor, shape = shape)
                )
            }

            SheetContentLayout(
                state = sheetLayoutState,
                contentOffsetY = { size ->
                    computeContentOffsetY(
                        state = state,
                        coroutineScope = coroutineScope,
                        density = density,
                        initialOffsetY = initialOffsetY,
                        contentAlpha = contentAlpha,
                        size = size,
                    )
                },
                alignment = contentAlignment,
            ) {
                Column(
                    modifier = modifier
                        .fillMaxWidth()
                        .sheetBackgroundWithInsets(
                            navigationBarInsets = WindowInsets.navigationBars,
                            backgroundColor = backgroundColor,
                            backgroundShape = shape,
                            extendsIntoNavigationBar = behaviors.extendsIntoNavigationBar,
                        )
                        .onGloballyPositioned {
                            val offset = it.positionInRoot()
                            contentRect = Rect(offset, it.size.toSize())
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

private fun computeContentOffsetY(
    state: BottomSheetState,
    coroutineScope: CoroutineScope,
    density: Density,
    initialOffsetY: Float,
    contentAlpha: Animatable<Float, AnimationVector1D>,
    size: IntSize,
): Int {
    if (state.contentHeight == size.height) {
        // The content height in the state is the latest height
        return state.offsetY.toInt()
    }

    // Height updated

    state.swipeToDismissDy = min(
        size.height / 3f,
        with(density) { 160.dp.toPx() }
    )

    val isAnimating = state.isAnimating
    state.contentHeight = size.height

    fun expand(offsetY: Int) = coroutineScope.launch {
        state.setOffsetY(
            offsetY,
            updateDimAmount = !isAnimating
        )
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

    fun peek(offsetY: Int) = coroutineScope.launch {
        state.setOffsetY(
            offsetY,
            updateDimAmount = !isAnimating
        )
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

    return when (state.value) {
        BottomSheetValue.Expanded -> {
            val offsetY = if (isAnimating) {
                min(size.height, initialOffsetY.toInt())
            } else {
                0
            }
            expand(offsetY)
            offsetY
        }

        BottomSheetValue.Peeked -> {
            val offsetY = if (isAnimating) {
                size.height
            } else {
                size.height - state.getPeekHeightInPx().toInt()
            }
            peek(offsetY)
            offsetY
        }

        BottomSheetValue.Collapsed -> {
            val offsetY = size.height
            coroutineScope.launch { state.addOffsetY(offsetY) }
            offsetY
        }
    }
}

@Composable
private fun rememberSheetContentLayoutState(): SheetContentLayoutState {
    return remember { SheetContentLayoutState() }
}

private class SheetContentLayoutState {
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
private fun SheetContentLayout(
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
