package com.dokar.sheets

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.dokar.sheets.layout.SheetContentLayout
import com.dokar.sheets.layout.computeContentOffsetY
import com.dokar.sheets.layout.rememberSheetContentLayoutState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

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
    CoreBottomSheetLayout(
        state = state,
        modifier = modifier,
        skipPeeked = skipPeeked,
        peekHeight = peekHeight,
        shape = shape,
        sheetBackground = { outline ->
            drawOutline(outline = outline, color = backgroundColor)
        },
        dimColor = dimColor,
        maxDimAmount = maxDimAmount,
        behaviors = behaviors,
        contentAlignment = contentAlignment,
        dragHandle = dragHandle,
        content = content,
    )
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
    backgroundColor: Brush,
    dimColor: Color = Color.Black,
    maxDimAmount: Float = CoreBottomSheetDefaults.MaxDimAmount,
    behaviors: SheetBehaviors = SheetBehaviors(),
    contentAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    dragHandle: @Composable () -> Unit = { CoreBottomSheetDragHandle() },
    content: @Composable () -> Unit
) {
    CoreBottomSheetLayout(
        state = state,
        modifier = modifier,
        skipPeeked = skipPeeked,
        peekHeight = peekHeight,
        shape = shape,
        sheetBackground = { outline ->
            drawOutline(outline = outline, brush = backgroundColor)
        },
        dimColor = dimColor,
        maxDimAmount = maxDimAmount,
        behaviors = behaviors,
        contentAlignment = contentAlignment,
        dragHandle = dragHandle,
        content = content,
    )
}

/**
 * A bottom sheet layout will be embedded in the composable directly.
 *
 * This shared overload lets callers provide the sheet surface drawing while keeping the
 * same layout, inset handling, and animation behavior used by the color and brush wrappers.
 *
 * @param state The bottom sheet state. Call [rememberBottomSheetState] to create one.
 * @param modifier Modifier for bottom sheet content.
 * @param skipPeeked Skip the peeked state if set to true. Defaults to false.
 * @param peekHeight Peek height, could be a dp, px, or fraction value.
 * @param shape Sheet shape used to resolve the content corner radius.
 * @param sheetBackground Drawing lambda for the sheet surface background.
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
    sheetBackground: DrawScope.(outline: Outline) -> Unit,
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
    val imeVisible = isImeVisible()

    SideEffect {
        state.peekHeight = peekHeight
        state.forceSkipPeeked = skipPeeked
        state.maxDimAmount = maxDimAmount
        state.imeVisible = imeVisible
        state.hasImeVisibilityUpdated = true
    }

    UpdateContentAlpha(state = state, contentAlpha = contentAlpha)

    DisposableEffect(state) {
        onDispose {
            state.visible = false
            state.contentHeight = 0
            state.imeVisible = false
            state.hasImeVisibilityUpdated = false
            coroutineScope.launch {
                state.stopAnimations()
            }
        }
    }

    Box(
        modifier = Modifier
            .nestedScroll(nestedScrollConnection)
            .then(
                if (behaviors.allowOutsideInteraction) {
                    Modifier
                } else {
                    Modifier
                        .fillMaxSize()
                        .drawBehind { drawRect(color = dimColor.copy(alpha = dimAlpha)) }
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
                        )
                }
            )
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
                .graphicsLayer { alpha = contentAlpha.value.coerceIn(0f, 1f) },
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
                        .drawSheetBackground(shape, sheetBackground)
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
                            backgroundShape = shape,
                            extendsIntoNavigationBar = behaviors.extendsIntoNavigationBar,
                            sheetBackground = sheetBackground,
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

@Composable
private fun UpdateContentAlpha(
    state: BottomSheetState,
    contentAlpha: Animatable<Float, AnimationVector1D>,
) {
    LaunchedEffect(state, contentAlpha) {
        var previousAnimState = state.animState
        snapshotFlow { state.value to state.animState }
            .distinctUntilChanged()
            .collectLatest { (value, animState) ->
                val oldAnimState = previousAnimState
                previousAnimState = animState
                val targetValue = oldAnimState.targetValue()
                // Skip update if the previous animation was interrupted before reaching its target.
                // This avoids incorrect alpha changes when, e.g., collapsing while expanding.
                if (targetValue != null &&
                    animState == BottomSheetState.AnimState.None &&
                    value != targetValue
                ) {
                    return@collectLatest
                }
                contentAlpha.updateBySheetState(
                    value = value,
                    animState = animState,
                    state = state,
                )
            }
    }
}

private fun BottomSheetState.AnimState.targetValue(): BottomSheetValue? {
    return when (this) {
        BottomSheetState.AnimState.Expanding -> BottomSheetValue.Expanded
        BottomSheetState.AnimState.Peeking -> BottomSheetValue.Peeked
        BottomSheetState.AnimState.Collapsing -> BottomSheetValue.Collapsed
        BottomSheetState.AnimState.None -> null
    }
}

private suspend fun Animatable<Float, AnimationVector1D>.updateBySheetState(
    value: BottomSheetValue,
    animState: BottomSheetState.AnimState,
    state: BottomSheetState,
) {
    when {
        animState == BottomSheetState.AnimState.Expanding -> {
            showContent(
                animate = state.isTransitionAnimated,
                animationSpec = state.expandAnimationSpec,
                animateWithDefaultSpec = true,
            )
        }

        value == BottomSheetValue.Expanded -> {
            snapTo(1f)
        }

        animState == BottomSheetState.AnimState.Peeking -> {
            showContent(
                animate = state.isTransitionAnimated,
                animationSpec = state.peekAnimationSpec,
                animateWithDefaultSpec = false,
            )
        }

        value == BottomSheetValue.Peeked -> {
            snapTo(1f)
        }

        animState == BottomSheetState.AnimState.Collapsing -> {}

        value == BottomSheetValue.Collapsed -> {
            snapTo(0f)
        }
    }
}

private suspend fun Animatable<Float, AnimationVector1D>.showContent(
    animate: Boolean,
    animationSpec: AnimationSpec<Float>?,
    animateWithDefaultSpec: Boolean,
) {
    if (!animate) {
        snapTo(1f)
    } else if (animationSpec != null) {
        animateTo(
            targetValue = 1f,
            animationSpec = animationSpec,
        )
    } else if (animateWithDefaultSpec) {
        animateTo(1f)
    } else {
        snapTo(1f)
    }
}
