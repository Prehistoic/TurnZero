package com.turnzero.tools.rules

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class RulesTurnScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val phases = listOf(
        Phase(name = "A", steps = listOf(RuleItem(title = "a1"), RuleItem(title = "a2"))),
        Phase(name = "B", steps = listOf(RuleItem(title = "b1"))),
    )

    /**
     * Hosts [TurnStructureScreen] with the state transitions the ViewModel will own, so the
     * screen's three levels and its next/previous intents can be exercised through the UI.
     */
    @Composable
    private fun TestScreen() {
        var phaseIndex by remember { mutableStateOf<Int?>(null) }
        var stepIndex by remember { mutableStateOf<Int?>(null) }
        TurnStructureScreen(
            phases = phases,
            phaseIndex = phaseIndex,
            stepIndex = stepIndex,
            onOpenPhase = { index ->
                phaseIndex = index
                stepIndex = null
            },
            onOpenStep = { index -> stepIndex = index },
            onNextStep = {
                val phase = phaseIndex ?: return@TurnStructureScreen
                val step = stepIndex ?: return@TurnStructureScreen
                if (step < phases[phase].steps.lastIndex) {
                    stepIndex = step + 1
                } else if (phase < phases.lastIndex) {
                    phaseIndex = phase + 1
                    stepIndex = 0
                }
            },
            onPreviousStep = {
                val phase = phaseIndex ?: return@TurnStructureScreen
                val step = stepIndex ?: return@TurnStructureScreen
                if (step > 0) {
                    stepIndex = step - 1
                } else if (phase > 0) {
                    phaseIndex = phase - 1
                    stepIndex = phases[phase - 1].steps.lastIndex
                }
            },
        )
    }

    @Test
    fun `overview shows phases then opens step list then detail`() {
        composeRule.setContent { TestScreen() }

        composeRule.onNodeWithText("A").assertIsDisplayed()
        composeRule.onNodeWithText("B").assertIsDisplayed()

        composeRule.onNodeWithText("A").performClick()

        composeRule.onNodeWithText("a1").assertIsDisplayed()
        composeRule.onNodeWithText("a2").assertIsDisplayed()

        composeRule.onNodeWithText("a1").performClick()

        composeRule.onNodeWithText("Step 1 of 2 · A").assertIsDisplayed()
    }

    @Test
    fun `phase list shows singular step count`() {
        composeRule.setContent { TestScreen() }

        composeRule.onNodeWithText("2 steps").assertIsDisplayed()
        composeRule.onNodeWithText("1 step").assertIsDisplayed()
    }

    @Test
    fun `next advances steps and labels the boundary button without the phase name`() {
        composeRule.setContent { TestScreen() }

        composeRule.onNodeWithText("A").performClick()
        composeRule.onNodeWithText("a2").performClick()

        composeRule.onNodeWithText("Next →").assertIsDisplayed()
        composeRule.onNodeWithText("Next →").performClick()

        composeRule.onNodeWithText("Step 1 of 1 · B").assertIsDisplayed()
    }

    @Test
    fun `previous is disabled at first step and back works`() {
        composeRule.setContent { TestScreen() }

        composeRule.onNodeWithText("A").performClick()
        composeRule.onNodeWithText("a1").performClick()

        composeRule.onNodeWithText("← Prev").assertIsNotEnabled()

        composeRule.onNodeWithText("Next →").performClick()
        composeRule.onNodeWithText("Step 2 of 2 · A").assertIsDisplayed()
        composeRule.onNodeWithText("← Prev").assertIsEnabled()

        composeRule.onNodeWithText("← Prev").performClick()
        composeRule.onNodeWithText("Step 1 of 2 · A").assertIsDisplayed()
        composeRule.onNodeWithText("← Prev").assertIsNotEnabled()
    }

    @Test
    fun `next is disabled at the last step of the last phase`() {
        composeRule.setContent { TestScreen() }

        composeRule.onNodeWithText("A").performClick()
        composeRule.onNodeWithText("a2").performClick()
        composeRule.onNodeWithText("Next →").performClick()

        composeRule.onNodeWithText("Step 1 of 1 · B").assertIsDisplayed()
        composeRule.onNodeWithText("Next →").assertIsNotEnabled()
    }

    @Test
    fun `empty phases shows empty state`() {
        composeRule.setContent {
            TurnStructureScreen(
                phases = emptyList(),
                phaseIndex = null,
                stepIndex = null,
                onOpenPhase = {},
                onOpenStep = {},
                onNextStep = {},
                onPreviousStep = {},
            )
        }

        composeRule.onNodeWithText("No turn structure available").assertIsDisplayed()
    }
}
