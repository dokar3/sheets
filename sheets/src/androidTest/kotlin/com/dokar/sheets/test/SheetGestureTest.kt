package com.dokar.sheets.test

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.dokar.sheets.CoreBottomSheet
import com.dokar.sheets.rememberBottomSheetState
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.Test

class SheetGestureTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @OptIn(ExperimentalComposeUiApi::class)
    @Test
    fun clickOutsideToCollapseAfterSwitchingToAnotherApp() {
        val sheetContentText = "Bottom sheet"

        composeTestRule.setContent {
            val scope = rememberCoroutineScope()

            val state = rememberBottomSheetState()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics { testTagsAsResourceId = true },
            ) {
                Button(onClick = { scope.launch { state.expand() } }) {
                    Text(text = "Show")
                }
                CoreBottomSheet(
                    state = state,
                    modifier = Modifier.testTag("contentRoot"),
                    skipPeeked = true,
                ) {
                    Box(modifier = Modifier.padding(32.dp)) {
                        Text(text = sheetContentText)
                    }
                }
            }
        }

        composeTestRule.onNodeWithText("Show").performClick()
        composeTestRule.onNodeWithText(sheetContentText).assertIsDisplayed()

        composeTestRule.onNodeWithTag("contentRoot").assertExists().performTouchInput {
            val pointerId = 0
            val (width, height) = visibleSize
            // Simulate the swipe gesture on the navigation bar
            down(pointerId, Offset(width * 0.5f, height * 0.9f))
            moveTo(pointerId, Offset(width * 0.1f, height * 1.1f))
            // Important: Verify that cancel events are handled properly
            cancel()
        }

        composeTestRule.onNodeWithTag("contentRoot").assertExists().performTouchInput {
            // Tap outside of the sheet content
            click(Offset(0f, -10f))
        }

        composeTestRule.onNodeWithText(sheetContentText).assertDoesNotExist()
    }
}