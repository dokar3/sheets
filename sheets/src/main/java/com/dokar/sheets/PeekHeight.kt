package com.dokar.sheets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * The height of a sheet when it's fully peeked, could be pixels or friction.
 *
 * To create a peek height, use [PeekHeight.px], [PeekHeight.dp], [PeekHeight.fraction]
 * or use constructors directly.
 */
@Immutable
sealed interface PeekHeight {
    @Immutable
    @JvmInline
    value class Px(val value: Int) : PeekHeight

    @Immutable
    @JvmInline
    value class Fraction(val value: Float) : PeekHeight

    companion object {
        fun px(value: Int): PeekHeight = Px(value)

        fun fraction(value: Float): PeekHeight = Fraction(value)

        @Composable
        fun dp(value: Int): PeekHeight {
            return dp(value.toDouble())
        }

        @Composable
        fun dp(value: Float): PeekHeight {
            return dp(value.toDouble())
        }

        @Composable
        fun dp(value: Double): PeekHeight {
            return Px(with(LocalDensity.current) { value.dp.roundToPx() })
        }
    }
}
