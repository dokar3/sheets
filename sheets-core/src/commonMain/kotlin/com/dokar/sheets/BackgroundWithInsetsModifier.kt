package com.dokar.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

fun Modifier.sheetBackgroundWithInsets(
    navigationBarInsets: WindowInsets,
    backgroundColor: Color,
    backgroundShape: Shape,
    extendsIntoNavigationBar: Boolean,
): Modifier {
    return if (extendsIntoNavigationBar) {
        // Do not apply insets
        this.background(color = backgroundColor, shape = backgroundShape)
    } else {
        this
            // Apply 'margin' for landscape mode
            .windowInsetsPadding(navigationBarInsets.only(WindowInsetsSides.Horizontal))
            .background(color = backgroundColor, shape = backgroundShape)
            // Apply padding
            .windowInsetsPadding(navigationBarInsets)
    }
}

fun Modifier.sheetBackgroundWithInsets(
    navigationBarInsets: WindowInsets,
    backgroundColor: Brush,
    backgroundShape: Shape,
    extendsIntoNavigationBar: Boolean,
): Modifier {
    return if (extendsIntoNavigationBar) {
        // Do not apply insets
        this.background(brush = backgroundColor, shape = backgroundShape)
    } else {
        this
            // Apply 'margin' for landscape mode
            .windowInsetsPadding(navigationBarInsets.only(WindowInsetsSides.Horizontal))
            .background(brush = backgroundColor, shape = backgroundShape)
            // Apply padding
            .windowInsetsPadding(navigationBarInsets)
    }
}
