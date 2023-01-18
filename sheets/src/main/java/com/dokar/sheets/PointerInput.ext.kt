package com.dokar.sheets

import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.coroutineScope

internal fun Modifier.detectPointerPositionChanges(
    key: Any?,
    onDown: ((Offset) -> Unit)? = null,
    onUp: (() -> Unit)? = null,
    onPositionChanged: (Offset) -> Unit,
): Modifier {
    return pointerInput(key) {
        coroutineScope {
            forEachGesture {
                awaitPointerEventScope {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    onPositionChanged(down.position)
                    onDown?.invoke(down.position)
                    while (true) {
                        val pe = awaitPointerEvent(pass = PointerEventPass.Initial)
                        onPositionChanged(pe.changes.first().position)
                        if (pe.changes.all { it.changedToUp() }) {
                            break
                        }
                    }
                    onUp?.invoke()
                }
            }
        }
    }
}