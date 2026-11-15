package com.turnzero.tools.rules

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class RulesWinScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `renders each condition title`() {
        val conditions = listOf(
            RuleItem(title = "Way A"),
            RuleItem(title = "Way B"),
        )

        composeRule.setContent {
            WinConditionScreen(conditions = conditions)
        }

        composeRule.onNodeWithText("Way A").assertIsDisplayed()
        composeRule.onNodeWithText("Way B").assertIsDisplayed()
    }

    @Test
    fun `empty list shows empty state`() {
        composeRule.setContent {
            WinConditionScreen(conditions = emptyList())
        }

        composeRule.onNodeWithText("No win conditions available").assertIsDisplayed()
    }
}
