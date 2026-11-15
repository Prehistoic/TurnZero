package com.turnzero.tools.rules

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class RulesSpecificityUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val manifest = RulesManifest(
        specificities = listOf(
            SpecificityDef(
                id = "format",
                name = "Format",
                type = SpecificityType.SINGLE,
                options = listOf(
                    OptionDef(id = "commander", name = "Commander"),
                    OptionDef(id = "standard", name = "Standard"),
                ),
            ),
            SpecificityDef(
                id = "players",
                name = "Players",
                type = SpecificityType.NUMBER,
                min = 2,
                max = 4,
            ),
            SpecificityDef(
                id = "extras",
                name = "Extras",
                type = SpecificityType.MULTI,
                options = listOf(
                    OptionDef(id = "planechase", name = "Planechase"),
                    OptionDef(id = "archenemy", name = "Archenemy"),
                ),
            ),
        ),
    )

    private val selection = Selection(
        single = mapOf("format" to "standard"),
        number = mapOf("players" to 3),
        multi = mapOf("extras" to setOf("planechase")),
    )

    @Test
    fun `editor renders radio segmented and checkbox sections`() {
        composeRule.setContent {
            SpecificityEditor(
                manifest = manifest,
                selection = selection,
                onSelectOption = { _, _ -> },
                onNumberChange = { _, _ -> },
            )
        }

        // SINGLE section: a labeled radio row per option, reflecting the current selection.
        composeRule.onNodeWithText("Format").assertIsDisplayed()
        composeRule.onNodeWithText("Commander").assertIsDisplayed().assertIsNotSelected()
        composeRule.onNodeWithText("Standard").assertIsDisplayed().assertIsSelected()

        // NUMBER section: 2P..4P chips, the current value selected.
        composeRule.onNodeWithText("Players").assertIsDisplayed()
        composeRule.onNodeWithText("2P").assertIsDisplayed().assertIsNotSelected()
        composeRule.onNodeWithText("3P").assertIsDisplayed().assertIsSelected()
        composeRule.onNodeWithText("4P").assertIsDisplayed().assertIsNotSelected()
        composeRule.onNodeWithText("1P").assertDoesNotExist()
        composeRule.onNodeWithText("5P").assertDoesNotExist()

        // MULTI section: a checkbox row per option, checked ones from the selection.
        composeRule.onNodeWithText("Extras").assertIsDisplayed()
        composeRule.onNodeWithText("Planechase").assertIsDisplayed().assertIsOn()
        composeRule.onNodeWithText("Archenemy").assertIsDisplayed().assertIsOff()
    }

    @Test
    fun `editor reports option and number taps`() {
        val tappedOptions = mutableListOf<Pair<String, String>>()
        val tappedNumbers = mutableListOf<Pair<String, Int>>()

        composeRule.setContent {
            SpecificityEditor(
                manifest = manifest,
                selection = selection,
                onSelectOption = { specId, optionId -> tappedOptions += specId to optionId },
                onNumberChange = { specId, value -> tappedNumbers += specId to value },
            )
        }

        composeRule.onNodeWithText("Commander").performClick()
        assertEquals(listOf("format" to "commander"), tappedOptions)

        composeRule.onNodeWithText("Archenemy").performClick()
        assertEquals(listOf("format" to "commander", "extras" to "archenemy"), tappedOptions)

        composeRule.onNodeWithText("4P").performClick()
        assertEquals(listOf("players" to 4), tappedNumbers)
    }

    @Test
    fun `dialog shows Start and confirm reports clicks`() {
        val tappedOptions = mutableListOf<Pair<String, String>>()
        val tappedNumbers = mutableListOf<Pair<String, Int>>()
        var confirmed = 0

        composeRule.setContent {
            SpecificityDialog(
                manifest = manifest,
                selection = selection,
                onSelectOption = { specId, optionId -> tappedOptions += specId to optionId },
                onNumberChange = { specId, value -> tappedNumbers += specId to value },
                onConfirm = { confirmed++ },
            )
        }

        composeRule.onNodeWithText("Choose your game settings").assertIsDisplayed()
        composeRule.onNodeWithText("Start").assertIsDisplayed()

        composeRule.onNodeWithText("Commander").performClick()
        assertEquals(listOf("format" to "commander"), tappedOptions)

        composeRule.onNodeWithText("3P").performClick()
        assertEquals(listOf("players" to 3), tappedNumbers)

        composeRule.onNodeWithText("Start").performClick()
        assertEquals(1, confirmed)
    }

    @Test
    fun `pill shows text and click fires`() {
        var clicks = 0

        composeRule.setContent {
            SpecificityPill(text = "Commander · 3P", onClick = { clicks++ })
        }

        composeRule.onNodeWithText("Commander · 3P").assertIsDisplayed().performClick()
        assertEquals(1, clicks)
    }

    @Test
    fun `top bar renders title pill and forwards back pill and action clicks`() {
        var backClicks = 0
        var pillClicks = 0
        var actionClicks = 0

        composeRule.setContent {
            RulesToolTopBar(
                title = "Turn Structure",
                onBack = { backClicks++ },
                pillText = "Commander · 3P",
                onPillClick = { pillClicks++ },
                actionIcon = Icons.Filled.GridView,
                actionContentDescription = "Change layout",
                onActionClick = { actionClicks++ },
            )
        }

        composeRule.onNodeWithText("Turn Structure").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Back").performClick()
        composeRule.onNodeWithText("Commander · 3P").performClick()
        composeRule.onNodeWithContentDescription("Change layout").performClick()

        assertEquals(1, backClicks)
        assertEquals(1, pillClicks)
        assertEquals(1, actionClicks)
    }

    @Test
    fun `top bar omits pill and action when not provided`() {
        composeRule.setContent {
            RulesToolTopBar(
                title = "Rules",
                onBack = {},
                pillText = null,
                onPillClick = {},
                actionIcon = null,
                actionContentDescription = null,
                onActionClick = null,
            )
        }

        composeRule.onNodeWithText("Rules").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Back").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Change layout").assertDoesNotExist()
    }
}
