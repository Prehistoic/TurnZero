package com.turnzero.tools.dice

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The dice must show the value 1 before the first roll, never the die name
 * ("d6", "d20", ...) as their face value.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class DiceAnimationInitialValueTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `dice show 1 as face value before the first roll`() {
        composeRule.setContent {
            DiceAnimation(
                dice = listOf(
                    Die("a", 6, DiceColor.WHITE),
                    Die("b", 20, DiceColor.BLACK),
                ),
                results = null,
                isRolling = false,
                onAnimationComplete = {},
                dieSize = 60.dp,
            )
        }

        // Every die shows "1" as its face value...
        composeRule.onAllNodesWithText("1").assertCountEquals(2)
        // ...and the die name only appears once per die (the label), so with
        // two dice there is exactly one node per distinct label.
        composeRule.onAllNodesWithText("d6").assertCountEquals(1)
        composeRule.onAllNodesWithText("d20").assertCountEquals(1)
    }
}
