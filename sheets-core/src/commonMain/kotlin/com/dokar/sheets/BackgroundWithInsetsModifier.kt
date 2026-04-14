package com.dokar.sheets

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope

internal fun Modifier.sheetBackgroundWithInsets(
    navigationBarInsets: WindowInsets,
    backgroundShape: Shape,
    extendsIntoNavigationBar: Boolean,
    sheetBackground: DrawScope.(outline: Outline) -> Unit,
): Modifier {
    return if (extendsIntoNavigationBar) {
        // Do not apply insets
        this.drawSheetBackground(backgroundShape, sheetBackground)
    } else {
        this
            // Apply 'margin' for landscape mode
            .windowInsetsPadding(navigationBarInsets.only(WindowInsetsSides.Horizontal))
            .drawSheetBackground(backgroundShape, sheetBackground)
            // Apply padding
            .windowInsetsPadding(navigationBarInsets)
    }
}

internal fun Modifier.drawSheetBackground(
    backgroundShape: Shape,
    sheetBackground: DrawScope.(outline: Outline) -> Unit,
): Modifier {
    return drawWithCache {
        // Resolve the outline once per size change and reuse the radius while drawing.
        val outline = backgroundShape
            .createOutline(size, layoutDirection, this)
        onDrawBehind { sheetBackground(outline) }
    }
}

