package com.dokar.sheets

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.util.VelocityTracker
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max

/**
 * Create and remember a [BottomSheetState].
 *
 * @param initialValue The initial value of the sheet state.
 */
@Composable
fun rememberBottomSheetState(
    initialValue: BottomSheetValue = BottomSheetValue.Collapsed,
): BottomSheetState {
    return rememberSaveable(
        inputs = emptyArray(),
        saver = Saver(
            save = { BottomSheetState.save(it) },
            restore = { BottomSheetState.restore(it) }
        ),
    ) {
        BottomSheetState(
            initialValue = initialValue,
        )
    }
}

/**
 *  Provides the access to the bottom sheet states ([visible], [value], etc) and the
 *  ability to manually change the [value] by calling [peek], [expand] and [collapse].
 */
@Stable
class BottomSheetState(
    initialValue: BottomSheetValue = BottomSheetValue.Collapsed,
) {
    /**
     * The visible state of the sheet.
     */
    var visible by mutableStateOf(initialValue != BottomSheetValue.Collapsed)
        internal set

    internal var contentHeight by mutableStateOf(0)

    internal var maxDimAmount = BottomSheetDefaults.MaxDimAmount

    internal var dimAmount by mutableStateOf(0f)

    private val offsetYAnimatable = Animatable(0f)

    internal val offsetY by offsetYAnimatable.asState()

    internal var swipeToDismissDy by mutableStateOf(0f)

    private var dimAnim: Job? = null

    private var dragYAnim: Job? = null

    internal var expandAnimationSpec: AnimationSpec<Float>? = null

    internal var peekAnimationSpec: AnimationSpec<Float>? = null

    internal lateinit var peekHeight: PeekHeight

    internal var forceSkipPeek: Boolean = false

    private var pendingToStartAnimation = false

    internal val isAnimating: Boolean
        get() = offsetYAnimatable.isRunning || pendingToStartAnimation

    private var onDragStoppedJob: Job? = null

    internal var dragVelocity = 0f
        private set

    private val velocityTracker = VelocityTracker()

    /**
     * The current value of the sheet.
     */
    var value by mutableStateOf(initialValue)
        private set

    private var animState by mutableStateOf(AnimState.None)

    /**
     * Be true if the sheet is peeking.
     */
    val isPeeking: Boolean get() = animState == AnimState.Peeking

    /**
     * Be true if the sheet is expanding.
     */
    val isExpanding: Boolean get() = animState == AnimState.Expanding

    /**
     * Be true if the sheet is collapsing.
     */
    val isCollapsing: Boolean get() = animState == AnimState.Collapsing

    /**
     * The drag progress value of the sheet. Be 0.0 if the sheet is
     * invisible, and be 1.0 if it's fully expanded.
     */
    val dragProgress: Float
        get() {
            return if (contentHeight != 0) {
                ((contentHeight - offsetYAnimatable.value) / contentHeight).coerceIn(0f, 1f)
            } else {
                0f
            }
        }

    internal suspend fun addOffsetY(dy: Int) {
        addOffsetY(dy.toFloat())
    }

    internal suspend fun addOffsetY(dy: Float) {
        val y = this.offsetYAnimatable.value + dy
        setOffsetY(y)
    }

    internal suspend fun setOffsetY(
        y: Int,
        updateDimAmount: Boolean = true,
    ) {
        setOffsetY(y.toFloat(), updateDimAmount)
    }

    private suspend fun setOffsetY(
        y: Float,
        updateDimAmount: Boolean = true,
    ) {
        stopAnimation()
        offsetYAnimatable.snapTo(max(0f, y))
        if (updateDimAmount) {
            updateDimAmount()
        }
    }

    private fun updateDimAmount() {
        val progress = if (contentHeight != 0) {
            (contentHeight - offsetY) / contentHeight
        } else {
            0f
        }
        dimAmount = maxDimAmount * progress.coerceIn(0f, 1f)
    }

    internal fun addVelocity(y: Float) {
        velocityTracker.addPosition(System.currentTimeMillis(), Offset(0f, y))
        dragVelocity = calcVelocityY()
    }

    internal fun resetVelocity() {
        velocityTracker.resetTracking()
        dragVelocity = calcVelocityY()
    }

    private fun calcVelocityY(): Float {
        return velocityTracker.calculateVelocity().y
    }

    internal suspend fun stopAnimation() {
        dragYAnim?.cancel()
        dimAnim?.cancel()
        if (offsetYAnimatable.isRunning) {
            offsetYAnimatable.stop()
        }
    }

    /**
     * Collapse the sheet.
     *
     * @param animate Set to false to disable the animation.
     * @param animationSpec The [AnimationSpec] of the animation.
     */
    suspend fun collapse(
        animate: Boolean = true,
        animationSpec: AnimationSpec<Float> = collapseTween(),
    ) {
        setValue(BottomSheetValue.Collapsed, animate, animationSpec)
        visible = false
        dragVelocity = 0f
    }

    /**
     * Expand the sheet.
     *
     * @param animate Set to false to disable the animation.
     * @param animationSpec The [AnimationSpec] of the animation.
     */
    suspend fun expand(
        animate: Boolean = true,
        animationSpec: AnimationSpec<Float> = spring(dampingRatio = 0.85f, stiffness = 370f),
    ) {
        expandAnimationSpec = animationSpec
        visible = true
        setValue(BottomSheetValue.Expanded, animate, animationSpec)
        dragVelocity = 0f
    }

    /**
     * Peek the sheet.
     *
     * @param animate Set to false to disable the animation.
     * @param animationSpec The [AnimationSpec] of the animation.
     */
    suspend fun peek(
        animate: Boolean = true,
        animationSpec: AnimationSpec<Float> = spring(dampingRatio = 0.85f, stiffness = 370f)
    ) {
        peekAnimationSpec = animationSpec
        visible = true
        setValue(BottomSheetValue.Peeked, animate, animationSpec)
        dragVelocity = 0f
    }

    private fun collapseTween(): AnimationSpec<Float> {
        val duration = CollapseAnimDuration
        val velocityFactor = (abs(dragVelocity) / 25000f).coerceIn(0f, 1f)
        val newDuration = (duration - duration * 0.7f * velocityFactor).toInt()
        return tween(durationMillis = newDuration)
    }

    private suspend fun setValue(
        value: BottomSheetValue,
        animate: Boolean = true,
        animationSpec: AnimationSpec<Float>
    ) {
        this.animState = when (value) {
            BottomSheetValue.Expanded -> {
                AnimState.Expanding
            }

            BottomSheetValue.Peeked -> {
                AnimState.Peeking
            }

            BottomSheetValue.Collapsed -> {
                AnimState.Collapsing
            }
        }
        stopAnimation()
        if (animate) {
            animateToValue(value, animationSpec)
        } else {
            jumpToValue(value)
        }
        this.animState = AnimState.None
        this.value = value
    }

    private fun calcTargetAnimValues(value: BottomSheetValue): AnimValues {
        return when (value) {
            BottomSheetValue.Expanded -> {
                AnimValues(offsetY = 0f, dimAmount = maxDimAmount)
            }

            BottomSheetValue.Peeked -> {
                val peekHeightPx = getPeekHeightInPx()
                val offsetY = contentHeight.toFloat() - peekHeightPx
                val dim = peekHeightPx / contentHeight.toFloat() * maxDimAmount
                AnimValues(offsetY = offsetY, dimAmount = dim)
            }

            BottomSheetValue.Collapsed -> {
                AnimValues(offsetY = contentHeight.toFloat(), dimAmount = 0f)
            }
        }
    }

    private suspend fun animateToValue(
        newValue: BottomSheetValue,
        animationSpec: AnimationSpec<Float>,
    ) = coroutineScope {
        val initialVelocity = dragVelocity.coerceIn(-1000f, 1000f)

        pendingToStartAnimation = contentHeight == 0
        if (pendingToStartAnimation) {
            this@BottomSheetState.animState = AnimState.None
            this@BottomSheetState.value = newValue
            // Run a dummy animation to suspend the caller coroutine. Make it looks like
            // we are running animations in the caller coroutine, but we won't start
            // animations until the content height is available
            Animatable(0f).animateTo(
                targetValue = 1f,
                initialVelocity = initialVelocity,
                animationSpec = animationSpec
            )
            return@coroutineScope
        }

        val targetAnimValues = calcTargetAnimValues(newValue)

        val dragY = launch {
            offsetYAnimatable.animateTo(
                targetValue = targetAnimValues.offsetY,
                initialVelocity = initialVelocity,
                animationSpec = animationSpec
            )
        }

        val dimAmount = launch {
            val animatable = Animatable(dimAmount)
            launch {
                snapshotFlow { animatable.value }
                    .distinctUntilChanged()
                    .collect {
                        dimAmount = it.coerceIn(0f, 1f)
                    }
            }
            animatable.animateTo(
                targetValue = targetAnimValues.dimAmount,
                animationSpec = animationSpec,
            )
        }

        dragYAnim = dragY

        dragY.join()
        dimAmount.cancel()

        dimAnim = null
        dragYAnim = null
    }

    private suspend fun jumpToValue(newValue: BottomSheetValue) {
        addOffsetY(calcTargetAnimValues(newValue).offsetY)
    }

    internal suspend fun onDragStopped() = coroutineScope {
        onDragStoppedJob?.cancel()
        onDragStoppedJob = launch {
            when (nextValue()) {
                BottomSheetValue.Expanded -> {
                    expand(animationSpec = spring())
                }

                BottomSheetValue.Collapsed -> {
                    collapse()
                }

                BottomSheetValue.Peeked -> {
                    peek(animationSpec = spring())
                }
            }
            resetVelocity()
        }
    }

    private fun nextValue(): BottomSheetValue {
        if (dragVelocity >= 20000f) {
            // Quick pull down
            return BottomSheetValue.Collapsed
        }

        val dy = offsetYAnimatable.value

        if (dragVelocity >= 1000f) {
            if (value == BottomSheetValue.Expanded || isExpanding) {
                if (shouldSkipPeekState()) {
                    val shouldCollapse = dy >= swipeToDismissDy ||
                            dy > swipeToDismissDy * 0.7f && dragVelocity >= 2000f ||
                            dy > swipeToDismissDy * 0.5f && dragVelocity >= 3000f
                    if (shouldCollapse) {
                        return BottomSheetValue.Collapsed
                    }
                } else {
                    return BottomSheetValue.Peeked
                }
            } else if (value == BottomSheetValue.Peeked) {
                return BottomSheetValue.Collapsed
            }
        }

        if (dragVelocity <= -1000f) {
            return BottomSheetValue.Expanded
        }

        if (shouldSkipPeekState()) {
            return if (offsetYAnimatable.value >= swipeToDismissDy) {
                BottomSheetValue.Collapsed
            } else {
                BottomSheetValue.Expanded
            }
        }

        val peekHeightPx = getPeekHeightInPx()
        val peekCy = contentHeight - peekHeightPx
        val peekTop = peekCy - peekCy / 2f
        val peekBottom = peekCy + peekHeightPx / 2.5f

        if (offsetYAnimatable.value in peekTop..peekBottom) {
            return BottomSheetValue.Peeked
        }

        if (offsetYAnimatable.value < peekTop) {
            return BottomSheetValue.Expanded
        }

        return BottomSheetValue.Collapsed
    }

    internal fun shouldSkipPeekState(): Boolean {
        if (contentHeight == 0 || !::peekHeight.isInitialized) {
            return false
        }
        return forceSkipPeek || getPeekHeightInPx() >= contentHeight
    }

    internal fun getPeekHeightInPx(): Float {
        if (!::peekHeight.isInitialized) {
            return contentHeight.toFloat()
        }
        return when (val height = peekHeight) {
            is PeekHeight.Px -> {
                height.value.coerceIn(0, contentHeight).toFloat()
            }

            is PeekHeight.Fraction -> {
                height.value.coerceIn(0f, 1f) * contentHeight
            }
        }
    }

    private class AnimValues(
        val offsetY: Float,
        val dimAmount: Float,
    )

    internal enum class AnimState {
        None,
        Peeking,
        Expanding,
        Collapsing
    }

    companion object {
        private const val CollapseAnimDuration = 275

        internal fun save(state: BottomSheetState): Any {
            return when (state.value) {
                BottomSheetValue.Expanded -> {
                    BottomSheetValue.Expanded
                }

                BottomSheetValue.Peeked -> {
                    BottomSheetValue.Peeked
                }

                BottomSheetValue.Collapsed -> {
                    BottomSheetValue.Collapsed
                }
            }
        }

        internal fun restore(saveable: Any): BottomSheetState {
            val value = saveable as? BottomSheetValue
            return if (value != null) {
                BottomSheetState(initialValue = value)
            } else {
                BottomSheetState()
            }
        }
    }
}
