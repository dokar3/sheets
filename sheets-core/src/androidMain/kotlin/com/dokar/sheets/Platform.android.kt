package com.dokar.sheets

import android.os.Build
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import java.util.UUID

@Suppress("ACTUAL_WITHOUT_EXPECT")
actual typealias SecureFlagPolicy = androidx.compose.ui.window.SecureFlagPolicy

actual val defaultWindowSoftInputMode: Int = WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED

actual val hasLightNavigationBar: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

internal actual fun currentTimeMillis(): Long = System.currentTimeMillis()

@Composable
internal actual fun SheetHost(
    state: BottomSheetState,
    behaviors: DialogSheetBehaviors,
    showAboveKeyboard: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    val composition = rememberCompositionContext()

    val dialogId = rememberSaveable { UUID.randomUUID() }

    val layoutDirection = LocalLayoutDirection.current

    val finalBehaviors = remember(behaviors, showAboveKeyboard) {
        if (showAboveKeyboard) {
            @Suppress("DEPRECATION")
            behaviors.copy(
                dialogWindowSoftInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE,
            )
        } else {
            if (behaviors.dialogWindowSoftInputMode ==
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED
            ) {
                behaviors.copy(
                    dialogWindowSoftInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN,
                )
            } else {
                behaviors
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
                content()
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
