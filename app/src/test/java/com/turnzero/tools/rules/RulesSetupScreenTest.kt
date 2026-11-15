package com.turnzero.tools.rules

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
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
            SetupCarousel(steps = steps)
        }

        composeRule.onNodeWithText("1.").assertIsDisplayed()
        composeRule.onNodeWithText("One").assertIsDisplayed()

        composeRule.onNodeWithTag("SetupCarouselPager").performTouchInput { swipeLeft() }

        composeRule.onNodeWithText("2.").assertIsDisplayed()
        composeRule.onNodeWithText("Two").assertIsDisplayed()
    }

    @Test
    fun `empty steps shows empty state`() {
        composeRule.setContent {
            SetupCarousel(steps = emptyList())
        }

        composeRule.onNodeWithText("No setup steps available").assertIsDisplayed()
    }
}
