package com.dokar.sheets

import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.util.fastFirstOrNull
import kotlinx.coroutines.coroutineScope

fun Modifier.detectPointerPositionChanges(
    key: Any?,
    onPositionChanged: (Offset) -> Unit,
    onDown: ((Offset) -> Unit)? = null,
    onGestureEnd: (() -> Unit)? = null,
): Modifier {
    return pointerInput(key) {
        coroutineScope {
            forEachGesture {
                awaitPointerEventScope {
                    while (true) {
                        val downChange = awaitFirstDown(requireUnconsumed = false)
                        val downId = downChange.id
                        if (onDown != null) {
                            onDown(downChange.position)
                        }
                        while (true) {
                            val pe = awaitPointerEvent(pass = PointerEventPass.Initial)
                            val change = pe.changes.fastFirstOrNull { it.id == downId }
                            if (change != null) {
                                onPositionChanged(change.position)
                            } else {
                                break
                            }
                        }
                        if (onGestureEnd != null) {
                            onGestureEnd()
                        }
                    }
                }
            }
        }
    }
}