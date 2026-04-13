package com.dokar.sheets.test

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.dokar.sheets.BottomSheetState
import com.dokar.sheets.BottomSheetValue
import com.dokar.sheets.rememberBottomSheetState
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class BottomSheetStateTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun confirmValueChangeUpdatesAfterRecomposition() {
        lateinit var state: BottomSheetState

        composeTestRule.setContent {
            var allowExpand by remember { mutableStateOf(false) }
            val canExpand = allowExpand
            val scope = rememberCoroutineScope()
            state = rememberBottomSheetState(
                confirmValueChange = { canExpand },
            )

            Column {
                Button(onClick = { allowExpand = true }) {
                    Text(text = "Allow")
                }
                Button(onClick = { scope.launch { state.expand(animate = false) } }) {
                    Text(text = "Expand")
                }
            }
        }

        composeTestRule.onNodeWithText("Allow").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Expand").performClick()
        composeTestRule.waitForIdle()

        assertEquals(BottomSheetValue.Expanded, state.value)
    }
}
