package com.dokar.sheets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Immutable
sealed class PeekHeight {
    @Immutable
    class Px(val value: Int) : PeekHeight() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Px

            if (value != other.value) return false

            return true
        }

        override fun hashCode(): Int {
            return value
        }
    }

    @Immutable
    class Fraction(val value: Float) : PeekHeight() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Fraction

            if (value != other.value) return false

            return true
        }

        override fun hashCode(): Int {
            return value.hashCode()
        }
    }

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
