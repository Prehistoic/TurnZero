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
class RulesItemContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `renders title text bullets and skips missing image`() {
        composeRule.setContent {
            RulesItemContent(
                RuleItem(
                    title = "T",
                    text = listOf("para"),
                    bullets = listOf("bul"),
                    image = "rulebook/missing-page.png",
                )
            )
        }

        composeRule.onNodeWithText("T").assertIsDisplayed()
        composeRule.onNodeWithText("para").assertIsDisplayed()
        composeRule.onNodeWithText("• bul").assertIsDisplayed()
    }

    @Test
    fun `bold markers render without literal asterisks`() {
        composeRule.setContent {
            RulesItemContent(RuleItem(text = listOf("has **bold** inside")))
        }

        composeRule.onNodeWithText("has bold inside").assertIsDisplayed()
        composeRule.onNodeWithText("has **bold** inside").assertDoesNotExist()
    }
}
