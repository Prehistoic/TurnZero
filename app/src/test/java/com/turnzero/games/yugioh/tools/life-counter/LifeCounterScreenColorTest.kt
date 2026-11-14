package com.turnzero.games.yugioh.tools.lifecounter

import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Regression tests for the LP text color behavior: only calculator-driven LP
 * edits may color the LP text (green for increase, red for decrease). Reset,
 * undo (Cancel) and option changes (starting LP, timer) must not color it.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class LifeCounterScreenColorTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var viewModel: LifeCounterViewModel

    private fun setScreen() {
        composeRule.setContent {
            val vm = remember { viewModel }
            val uiState by vm.uiState.collectAsStateWithLifecycle()
            LifeCounterScreen(
                uiState = uiState,
                onStartPauseTimer = vm::startPauseTimer,
                onPauseTimer = vm::pauseTimer,
                onResetAll = vm::resetAll,
                onUndoLastChange = vm::undoLastChange,
                onOpenCalc = vm::openCalc,
                onConsumedFlashZero = vm::consumedFlashZero,
                onCloseCalc = vm::closeCalc,
                onCalcAction = LifeCounterCalcActions(
                    onDigit = vm::inputDigit,
                    onSetOperation = vm::setOperation,
                    onClear = vm::clearInput,
                    onDivideBy2 = vm::divideBy2,
                    onAddZeros = vm::addZeros,
                    onApply = vm::applyCalcChange
                ),
                modifier = Modifier
            )
        }
    }

    private fun assertLpDirection(tag: String, expected: Int) {
        composeRule.onNodeWithTag(tag, useUnmergedTree = true).assert(
            SemanticsMatcher.expectValue(LpColorDirectionKey, expected)
        )
    }

    private fun applySubtract300ToPlayer1() {
        viewModel.openCalc(Player.PLAYER_1)
        viewModel.inputDigit(3) // 300 with auto-multiply
        viewModel.applyCalcChange()
    }

    @Test
    fun `calcChange_colorsOnlyEditedPlayerLp`() {
        viewModel = LifeCounterViewModel(SavedStateHandle())
        composeRule.mainClock.autoAdvance = false
        setScreen()
        composeRule.mainClock.advanceTimeBy(16)

        applySubtract300ToPlayer1() // 8000 -> 7700
        composeRule.mainClock.advanceTimeBy(32)

        assertLpDirection("player-1-lp", -1)
        assertLpDirection("player-2-lp", 0)
    }

    @Test
    fun `resetAll_doesNotColorLp`() {
        viewModel = LifeCounterViewModel(SavedStateHandle())
        composeRule.mainClock.autoAdvance = false
        setScreen()
        composeRule.mainClock.advanceTimeBy(16)

        applySubtract300ToPlayer1() // 8000 -> 7700, red flash starts
        composeRule.mainClock.advanceTimeBy(32)
        assertLpDirection("player-1-lp", -1)

        viewModel.resetAll() // 7700 -> 8000, must NOT flash green
        composeRule.mainClock.advanceTimeBy(32)
        assertLpDirection("player-1-lp", 0)
        assertLpDirection("player-2-lp", 0)

        composeRule.mainClock.advanceTimeBy(3000)
        assertLpDirection("player-1-lp", 0)
        assertLpDirection("player-2-lp", 0)
    }

    @Test
    fun `undoLastChange_doesNotColorLp`() {
        viewModel = LifeCounterViewModel(SavedStateHandle())
        composeRule.mainClock.autoAdvance = false
        setScreen()
        composeRule.mainClock.advanceTimeBy(16)

        applySubtract300ToPlayer1() // 8000 -> 7700
        composeRule.mainClock.advanceTimeBy(32)
        assertLpDirection("player-1-lp", -1)

        viewModel.undoLastChange() // 7700 -> 8000, must NOT flash green
        composeRule.mainClock.advanceTimeBy(32)
        assertLpDirection("player-1-lp", 0)

        composeRule.mainClock.advanceTimeBy(3000)
        assertLpDirection("player-1-lp", 0)
    }

    @Test
    fun `setStartingLp_doesNotColorLp`() {
        viewModel = LifeCounterViewModel(SavedStateHandle())
        composeRule.mainClock.autoAdvance = false
        setScreen()
        composeRule.mainClock.advanceTimeBy(16)

        applySubtract300ToPlayer1() // 8000 -> 7700
        composeRule.mainClock.advanceTimeBy(32)
        assertLpDirection("player-1-lp", -1)

        viewModel.setStartingLp(4000) // must NOT flash red
        composeRule.mainClock.advanceTimeBy(32)
        assertLpDirection("player-1-lp", 0)
        assertLpDirection("player-2-lp", 0)

        composeRule.mainClock.advanceTimeBy(3000)
        assertLpDirection("player-1-lp", 0)
        assertLpDirection("player-2-lp", 0)
    }
}
