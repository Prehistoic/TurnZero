package com.turnzero.tools.rules

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.turnzero.core.ui.components.LayoutMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w480dp-h1600dp")
class RulesMainPanelTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `tiles render in fixed sub tool order`() {
        composeRule.setContent {
            RulesMainPanel(
                subTools = RuleSubTool.entries,
                layoutMode = LayoutMode.LIST,
                onSubToolClick = {},
            )
        }

        val expectedTitles = listOf("Setup", "Turn Structure", "Win Condition", "Keywords", "Rulebook")
        val tops = expectedTitles.map { title ->
            composeRule.onNodeWithText(title).assertIsDisplayed()
            composeRule.onNodeWithText(title).fetchSemanticsNode().boundsInRoot.top
        }

        assertTrue("Expected $expectedTitles in order, got top offsets $tops", tops.zipWithNext().all { (a, b) -> a < b })
    }

    @Test
    fun `tile click reports the sub tool`() {
        var clicked: RuleSubTool? = null

        composeRule.setContent {
            RulesMainPanel(
                subTools = RuleSubTool.entries,
                layoutMode = LayoutMode.GRID,
                onSubToolClick = { clicked = it },
            )
        }

        composeRule.onNodeWithText("Win Condition").performClick()

        assertEquals(RuleSubTool.WIN, clicked)
    }
}
