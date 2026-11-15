package com.turnzero.tools.rules

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class RulesSetupScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val steps = listOf(
        RuleItem(title = "One"),
        RuleItem(title = "Two"),
        RuleItem(title = "Three"),
    )

    @Test
    fun `shows first step number and swipes to second`() {
        composeRule.setContent {
            SetupCarousel(steps = steps, resetKey = 0)
        }

        composeRule.onNodeWithText("1.").assertIsDisplayed()
        composeRule.onNodeWithText("One").assertIsDisplayed()

        composeRule.onNodeWithTag("SetupCarouselPager").performTouchInput { swipeLeft() }

        composeRule.onNodeWithText("2.").assertIsDisplayed()
        composeRule.onNodeWithText("Two").assertIsDisplayed()
    }

    @Test
    fun `changing the reset key returns to the first step`() {
        var resetKey by mutableStateOf(0)

        composeRule.setContent {
            SetupCarousel(steps = steps, resetKey = resetKey)
        }

        composeRule.onNodeWithTag("SetupCarouselPager").performTouchInput { swipeLeft() }
        composeRule.onNodeWithText("2.").assertIsDisplayed()

        resetKey = 1
        composeRule.waitForIdle()

        composeRule.onNodeWithText("1.").assertIsDisplayed()
        composeRule.onNodeWithText("2.").assertDoesNotExist()
    }

    @Test
    fun `empty steps shows empty state`() {
        composeRule.setContent {
            SetupCarousel(steps = emptyList(), resetKey = 0)
        }

        composeRule.onNodeWithText("No setup steps available").assertIsDisplayed()
    }
}
