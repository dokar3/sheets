package com.dokar.sheets

import android.view.WindowManager
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.SecureFlagPolicy

@Immutable
class DialogSheetBehaviors(
    collapseOnBackPress: Boolean,
    collapseOnClickOutside: Boolean,
    fitsSystemWindows: Boolean,
    val dialogSecurePolicy: SecureFlagPolicy = SecureFlagPolicy.Inherit,
    val dialogWindowSoftInputMode: Int = WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED,
    val lightStatusBar: Boolean = false,
    val lightNavigationBar: Boolean = false,
    val statusBarColor: Color = Color.Transparent,
    val navigationBarColor: Color = Color.Transparent,
) : SheetBehaviors(
    collapseOnBackPress = collapseOnBackPress,
    collapseOnClickOutside = collapseOnClickOutside,
    fitsSystemWindows = fitsSystemWindows,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as DialogSheetBehaviors

        if (dialogSecurePolicy != other.dialogSecurePolicy) return false
        if (dialogWindowSoftInputMode != other.dialogWindowSoftInputMode) return false
        if (lightStatusBar != other.lightStatusBar) return false
        if (lightNavigationBar != other.lightNavigationBar) return false
        if (statusBarColor != other.statusBarColor) return false
        if (navigationBarColor != other.navigationBarColor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + dialogSecurePolicy.hashCode()
        result = 31 * result + dialogWindowSoftInputMode
        result = 31 * result + lightStatusBar.hashCode()
        result = 31 * result + lightNavigationBar.hashCode()
        result = 31 * result + statusBarColor.hashCode()
        result = 31 * result + navigationBarColor.hashCode()
        return result
    }
}

@Immutable
open class SheetBehaviors(
    val collapseOnBackPress: Boolean,
    val collapseOnClickOutside: Boolean,
    val fitsSystemWindows: Boolean,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SheetBehaviors

        if (collapseOnBackPress != other.collapseOnBackPress) return false
        if (collapseOnClickOutside != other.collapseOnClickOutside) return false
        if (fitsSystemWindows != other.fitsSystemWindows) return false

        return true
    }

    override fun hashCode(): Int {
        var result = collapseOnBackPress.hashCode()
        result = 31 * result + collapseOnClickOutside.hashCode()
        result = 31 * result + fitsSystemWindows.hashCode()
        return result
    }
}
