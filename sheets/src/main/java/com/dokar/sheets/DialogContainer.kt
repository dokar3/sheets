// Based on androidx.compose.ui.window.AndroidDialog.android.kt

package com.dokar.sheets

import android.content.Context
import android.os.Build
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentDialog
import androidx.activity.addCallback
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.ViewRootForInspector
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMaxBy
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.core.view.WindowCompat
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import java.util.UUID

@Suppress("ViewConstructor")
internal class SheetLayout(
    context: Context,
    override val window: Window,
) : AbstractComposeView(context), DialogWindowProvider {
    private var content: @Composable () -> Unit by mutableStateOf({})

    override var shouldCreateCompositionOnAttachedToWindow: Boolean = false
        private set

    @Composable
    override fun Content() {
        content()
    }

    fun setContent(parent: CompositionContext, content: @Composable () -> Unit) {
        setParentCompositionContext(parent)
        this.content = content
        shouldCreateCompositionOnAttachedToWindow = true
        createComposition()
    }
}

@Suppress("deprecation")
internal class DialogWrapper(
    private var onDismissRequest: () -> Unit,
    private var behaviors: DialogSheetBehaviors,
    private val composeView: View,
    layoutDirection: LayoutDirection,
    dialogId: UUID,
) : ComponentDialog(composeView.context, R.style.DialogWindowTheme), ViewRootForInspector {
    private val sheetLayout: SheetLayout

    override val subCompositionView: AbstractComposeView get() = sheetLayout

    init {
        val window = window ?: error("Dialog has no window")
        window.requestFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawableResource(android.R.color.transparent)
        window.setDimAmount(0f)

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // It can be transparent for API 23 and above because we will handle switching the status
            // bar icons to light or dark as appropriate. For API 21 and API 22 we just set the
            // translucent status bar.
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        val edgeToEdgeFlags = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.decorView.systemUiVisibility = edgeToEdgeFlags

        sheetLayout = SheetLayout(context, window).apply {
            // Set unique id for AbstractComposeView. This allows state restoration for the state
            // defined inside the Dialog via rememberSaveable()
            setTag(R.id.compose_view_saveable_id_tag, "SheetDialog:$dialogId")
            clipChildren = false
        }

        /**
         * Disables clipping for [this] and all its descendant [ViewGroup]s until we reach a
         * [SheetLayout] (the [ViewGroup] containing the Compose hierarchy).
         */
        fun ViewGroup.disableClipping() {
            clipChildren = false
            if (this is SheetLayout) return
            for (i in 0 until childCount) {
                (getChildAt(i) as? ViewGroup)?.disableClipping()
            }
        }

        // Turn of all clipping so shadows can be drawn outside the window
        (window.decorView as? ViewGroup)?.disableClipping()
        setContentView(sheetLayout)

        sheetLayout.setViewTreeLifecycleOwner(composeView.findViewTreeLifecycleOwner())
        sheetLayout.setViewTreeViewModelStoreOwner(composeView.findViewTreeViewModelStoreOwner())
        sheetLayout.setViewTreeSavedStateRegistryOwner(
            composeView.findViewTreeSavedStateRegistryOwner()
        )

        updateParameters(onDismissRequest, behaviors, layoutDirection)

        // Due to how the onDismissRequest callback works
        // (it enforces a just-in-time decision on whether to update the state to hide the dialog)
        // we need to unconditionally add a callback here that is always enabled,
        // meaning we'll never get a system UI controlled predictive back animation
        // for these dialogs
        onBackPressedDispatcher.addCallback(this) {
            if (behaviors.collapseOnBackPress) {
                onDismissRequest()
            }
        }
    }

    fun setContent(parentComposition: CompositionContext, children: @Composable () -> Unit) {
        sheetLayout.setContent(parentComposition, children)
    }

    private fun SecureFlagPolicy.shouldApplySecureFlag(isSecureFlagSetOnParent: Boolean): Boolean {
        return when (this) {
            SecureFlagPolicy.SecureOff -> false
            SecureFlagPolicy.SecureOn -> true
            SecureFlagPolicy.Inherit -> isSecureFlagSetOnParent
        }
    }

    private fun View.isFlagSecureEnabled(): Boolean {
        val windowParams = rootView.layoutParams as? WindowManager.LayoutParams
        if (windowParams != null) {
            return (windowParams.flags and WindowManager.LayoutParams.FLAG_SECURE) != 0
        }
        return false
    }

    private fun setSecurePolicy(securePolicy: SecureFlagPolicy) {
        val secureFlagEnabled =
            securePolicy.shouldApplySecureFlag(composeView.isFlagSecureEnabled())
        window!!.setFlags(
            if (secureFlagEnabled) {
                WindowManager.LayoutParams.FLAG_SECURE
            } else {
                WindowManager.LayoutParams.FLAG_SECURE.inv()
            },
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }

    fun updateParameters(
        onDismissRequest: () -> Unit,
        behaviors: DialogSheetBehaviors,
        layoutDirection: LayoutDirection,
    ) {
        this.onDismissRequest = onDismissRequest
        this.behaviors = behaviors
        setSecurePolicy(behaviors.dialogSecurePolicy)
        setLayoutDirection(layoutDirection)
        window?.run {
            setSoftInputMode(behaviors.dialogWindowSoftInputMode)

            statusBarColor = behaviors.statusBarColor.toArgb()
            navigationBarColor = behaviors.navigationBarColor.toArgb()

            WindowCompat.getInsetsController(this, decorView).let { controller ->
                controller.isAppearanceLightStatusBars = behaviors.lightStatusBar
                controller.isAppearanceLightNavigationBars = behaviors.lightNavigationBar
            }
        }
    }

    private fun setLayoutDirection(layoutDirection: LayoutDirection) {
        sheetLayout.layoutDirection = when (layoutDirection) {
            LayoutDirection.Ltr -> android.util.LayoutDirection.LTR
            LayoutDirection.Rtl -> android.util.LayoutDirection.RTL
        }
    }

    fun disposeComposition() {
        sheetLayout.disposeComposition()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val result = super.onTouchEvent(event)
        if (result && behaviors.collapseOnBackPress) {
            onDismissRequest()
        }

        return result
    }

    override fun cancel() {
        // Prevents the dialog from dismissing itself
        return
    }
}

@Composable
internal fun DialogLayout(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val placeables = measurables.fastMap { it.measure(constraints) }
        val width = placeables.fastMaxBy { it.width }?.width ?: constraints.minWidth
        val height = placeables.fastMaxBy { it.height }?.height ?: constraints.minHeight
        layout(width, height) {
            placeables.fastForEach { it.placeRelative(0, 0) }
        }
    }
}
