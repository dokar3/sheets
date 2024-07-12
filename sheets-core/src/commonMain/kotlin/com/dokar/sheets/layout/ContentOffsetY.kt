package com.dokar.sheets.layout

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.dokar.sheets.BottomSheetState
import com.dokar.sheets.BottomSheetValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.min

internal fun computeContentOffsetY(
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

    if (state.animState == BottomSheetState.AnimState.Collapsing) {
        // Do nothing when collapsing
        return state.offsetY.toInt()
    }

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
