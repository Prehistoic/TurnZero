package com.turnzero.tools.rules

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class RulesRulebookScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val rulebooks = listOf(
        RulebookDef(id = "core", name = "Core Rulebook", asset = "rulebooks/core.pdf"),
        RulebookDef(id = "advanced", name = "Advanced Rulebook", asset = "rulebooks/advanced.pdf"),
    )

    @Test
    fun `selector dialog lists matching rulebooks and cancel dismisses`() {
        var dismissed = false

        composeRule.setContent {
            RulebookScreen(
                rulebooks = rulebooks,
                selectedRulebookId = null,
                initialPage = 0,
                showSelector = true,
                onSelectRulebook = {},
                onDismissSelector = { dismissed = true },
            )
        }

        composeRule.onNodeWithText("Choose Rules PDF").assertIsDisplayed()
        composeRule.onNodeWithText("Core Rulebook").assertIsDisplayed()
        composeRule.onNodeWithText("Advanced Rulebook").assertIsDisplayed()
        assertFalse(dismissed)

        composeRule.onNodeWithText("Cancel").performClick()

        assertTrue(dismissed)
    }

    @Test
    fun `no matching rulebooks shows empty state`() {
        composeRule.setContent {
            RulebookScreen(
                rulebooks = emptyList(),
                selectedRulebookId = null,
                initialPage = 0,
                showSelector = false,
                onSelectRulebook = {},
                onDismissSelector = {},
            )
        }

        composeRule.onNodeWithText("No rulebook available").assertIsDisplayed()
    }

    @Test
    fun `unresolved selection with multiple rulebooks shows selector`() {
        composeRule.setContent {
            RulebookScreen(
                rulebooks = rulebooks,
                selectedRulebookId = null,
                initialPage = 0,
                showSelector = false,
                onSelectRulebook = {},
                onDismissSelector = {},
            )
        }

        composeRule.onNodeWithText("Choose Rules PDF").assertIsDisplayed()
        composeRule.onNodeWithText("Core Rulebook").assertIsDisplayed()
        composeRule.onNodeWithText("Advanced Rulebook").assertIsDisplayed()
    }

    @Test
    fun `selector tap reports the chosen rulebook id`() {
        var selected: String? = null

        composeRule.setContent {
            RulebookScreen(
                rulebooks = rulebooks,
                selectedRulebookId = null,
                initialPage = 0,
                showSelector = true,
                onSelectRulebook = { selected = it },
                onDismissSelector = {},
            )
        }

        composeRule.onNodeWithText("Advanced Rulebook").performClick()

        assertEquals("advanced", selected)
    }
}
