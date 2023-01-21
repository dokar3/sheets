// Based on androidx.compose.ui.window.AndroidDialog.android.kt

package com.dokar.sheets

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMaxBy
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.lifecycle.ViewTreeViewModelStoreOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import java.util.UUID

internal class SheetLayout(
    context: Context
) : AbstractComposeView(context) {
    private var content: (@Composable () -> Unit)? = null

    override var shouldCreateCompositionOnAttachedToWindow: Boolean = false
        private set

    @Composable
    override fun Content() {
        content?.invoke()
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
    dialogId: UUID,
) : Dialog(composeView.context, R.style.DialogWindowTheme) {
    private val sheetLayout: SheetLayout

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

        sheetLayout = SheetLayout(context).apply {
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
        ViewTreeLifecycleOwner.set(sheetLayout, ViewTreeLifecycleOwner.get(composeView))
        ViewTreeViewModelStoreOwner.set(sheetLayout, ViewTreeViewModelStoreOwner.get(composeView))
        sheetLayout.setViewTreeSavedStateRegistryOwner(
            composeView.findViewTreeSavedStateRegistryOwner()
        )

        updateParameters(behaviors)
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
        behaviors: DialogSheetBehaviors,
    ) {
        this.behaviors = behaviors
        setSecurePolicy(behaviors.dialogSecurePolicy)
        window!!.run {
            setSoftInputMode(behaviors.dialogWindowSoftInputMode)

            statusBarColor = behaviors.statusBarColor.toArgb()
            navigationBarColor = behaviors.navigationBarColor.toArgb()

            WindowCompat.getInsetsController(this, decorView).let { controller ->
                controller.isAppearanceLightStatusBars = behaviors.lightStatusBar
                controller.isAppearanceLightNavigationBars = behaviors.lightNavigationBar
            }
        }
    }

    fun disposeComposition() {
        sheetLayout.disposeComposition()
    }

    override fun cancel() {
        // Prevents the dialog from dismissing itself
        return
    }

    @Deprecated("")
    override fun onBackPressed() {
        if (behaviors.collapseOnBackPress) {
            onDismissRequest()
        }
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
