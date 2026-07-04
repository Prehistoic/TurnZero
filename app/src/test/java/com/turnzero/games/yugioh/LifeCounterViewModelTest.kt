package com.turnzero.games.yugioh

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LifeCounterViewModelTest {

    private val savedStateHandle = SavedStateHandle()

    @Test
    fun `initial state has default values`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        val state = vm.uiState.value
        assertEquals(8000, state.player1Lp)
        assertEquals(8000, state.player2Lp)
        assertEquals(TimerMode.COUNT_DOWN, state.timerMode)
        assertEquals(45, state.timerStartValueMinutes)
        assertEquals(8000, state.startingLp)
        assertFalse(state.isRunning)
        assertFalse(state.showCalcSheet)
        assertFalse(state.hasEverStarted)
        assertEquals(0L, state.elapsedSeconds)
        assertEquals(45 * 60L, state.remainingSeconds)
    }

    @Test
    fun `startPauseTimer toggles isRunning and sets hasEverStarted`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        assertFalse(vm.uiState.value.isRunning)
        vm.startPauseTimer()
        assertTrue(vm.uiState.value.isRunning)
        assertTrue(vm.uiState.value.hasEverStarted)
        vm.startPauseTimer()
        assertFalse(vm.uiState.value.isRunning)
        assertTrue(vm.uiState.value.hasEverStarted)
    }

    @Test
    fun `tick increments elapsed in countUp mode`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        vm.setTimerMode(TimerMode.COUNT_UP)
        vm.resetTimer()
        vm.tick()
        vm.tick()
        vm.tick()
        assertEquals(3, vm.uiState.value.elapsedSeconds)
    }

    @Test
    fun `tick decrements remaining in countDown mode`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        vm.setTimerStartValue(1)
        vm.resetTimer()
        assertEquals(60, vm.uiState.value.remainingSeconds)
        vm.tick()
        vm.tick()
        assertEquals(58, vm.uiState.value.remainingSeconds)
    }

    @Test
    fun `tick stops at zero and sets flashZero in countDown`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        vm.setTimerStartValue(1)
        vm.resetTimer()
        repeat(60) { vm.tick() }
        assertEquals(0, vm.uiState.value.remainingSeconds)
        assertTrue(vm.uiState.value.flashZero)
    }

    @Test
    fun `resetTimer resets elapsed, remaining, and hasEverStarted`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        vm.setTimerMode(TimerMode.COUNT_UP)
        vm.resetTimer()
        vm.startPauseTimer()
        assertTrue(vm.uiState.value.hasEverStarted)
        vm.tick()
        vm.tick()
        assertEquals(2, vm.uiState.value.elapsedSeconds)
        vm.resetTimer()
        assertEquals(0, vm.uiState.value.elapsedSeconds)
        assertFalse(vm.uiState.value.hasEverStarted)
    }

    @Test
    fun `setTimerStartValue resets timer and LP`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        vm.openCalc(Player.PLAYER_1)
        vm.inputDigit(3)
        vm.applyCalcChange()
        assertEquals(7700, vm.uiState.value.player1Lp)
        vm.setTimerStartValue(45)
        assertEquals(45, vm.uiState.value.timerStartValueMinutes)
        assertEquals(45 * 60, vm.uiState.value.remainingSeconds)
        assertEquals(8000, vm.uiState.value.player1Lp)
    }

    @Test
    fun `openCalc sets activePlayer and shows calc sheet`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        vm.openCalc(Player.PLAYER_1)
        val state = vm.uiState.value
        assertTrue(state.showCalcSheet)
        assertEquals(Player.PLAYER_1, state.activePlayer)
        assertEquals(Operation.SUBTRACT, state.calcState.operation)
    }

    @Test
    fun `closeCalc dismisses calc sheet without applying`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        vm.openCalc(Player.PLAYER_1)
        vm.closeCalc()
        assertFalse(vm.uiState.value.showCalcSheet)
    }

    @Test
    fun `setOperation changes calc operation`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        vm.openCalc(Player.PLAYER_1)
        vm.setOperation(Operation.ADD)
        assertEquals(Operation.ADD, vm.uiState.value.calcState.operation)
    }

    @Test
    fun `inputDigit builds digit string with auto-multiply for 1 digit`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        vm.openCalc(Player.PLAYER_1)
        vm.inputDigit(5)
        val cs = vm.uiState.value.calcState
        assertEquals("5", cs.digits)
        assertEquals(500, cs.previewValue)
        assertEquals(2, cs.trailingZeroCount)
    }

    @Test
    fun `inputDigit builds digit string with auto-multiply for 2 digits`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        vm.openCalc(Player.PLAYER_1)
        vm.inputDigit(5)
        vm.inputDigit(3)
        val cs = vm.uiState.value.calcState
        assertEquals("53", cs.digits)
        assertEquals(5300, cs.previewValue)
        assertEquals(2, cs.trailingZeroCount)
    }

    @Test
    fun `inputDigit builds digit string with auto-multiply for 3 digits`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        vm.openCalc(Player.PLAYER_1)
        vm.inputDigit(5)
        vm.inputDigit(3)
        vm.inputDigit(0)
        val cs = vm.uiState.value.calcState
        assertEquals("530", cs.digits)
        assertEquals(530, cs.previewValue)
        assertEquals(0, cs.trailingZeroCount)
        assertTrue(cs.lockedIn)
    }

    @Test
    fun `inputDigit builds digit string with no multiply for 4 digits`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        vm.openCalc(Player.PLAYER_1)
        "1234".forEach { vm.inputDigit(it.digitToInt()) }
        val cs = vm.uiState.value.calcState
        assertEquals("1234", cs.digits)
        assertEquals(1234, cs.previewValue)
        assertEquals(0, cs.trailingZeroCount)
    }

    @Test
    fun `inputDigit locks after pressing zero`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        vm.openCalc(Player.PLAYER_1)
        vm.inputDigit(5)
        assertFalse(vm.uiState.value.calcState.lockedIn)
        vm.inputDigit(0)
        assertTrue(vm.uiState.value.calcState.lockedIn)
        vm.inputDigit(3)
        assertEquals("503", vm.uiState.value.calcState.digits)
        assertEquals(503, vm.uiState.value.calcState.previewValue)
    }

    @Test
    fun `clearInput resets calc digits`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        vm.openCalc(Player.PLAYER_1)
        vm.inputDigit(5)
        vm.inputDigit(0)
        vm.clearInput()
        val cs = vm.uiState.value.calcState
        assertEquals("", cs.digits)
        assertEquals(0, cs.previewValue)
    }

    @Test
    fun `inputDigit 0 locks input`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        vm.openCalc(Player.PLAYER_1)
        vm.inputDigit(5)
        vm.inputDigit(0)
        val cs = vm.uiState.value.calcState
        assertEquals("50", cs.digits)
        assertEquals(50, cs.previewValue)
        assertEquals(0, cs.trailingZeroCount)
        assertTrue(cs.lockedIn)
    }

    @Test
    fun `addZeros ignores when digits empty`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        vm.openCalc(Player.PLAYER_1)
        vm.addZeros(2)
        assertEquals("", vm.uiState.value.calcState.digits)
        assertEquals(0, vm.uiState.value.calcState.previewValue)
    }

    @Test
    fun `addZeros after digit works correctly`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        vm.openCalc(Player.PLAYER_1)
        vm.inputDigit(5)
        vm.addZeros(2)
        assertEquals("500", vm.uiState.value.calcState.digits)
        assertEquals(500, vm.uiState.value.calcState.previewValue)
        assertTrue(vm.uiState.value.calcState.lockedIn)
    }

    @Test
    fun `inputDigit after addZeros uses raw value`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        vm.openCalc(Player.PLAYER_1)
        vm.inputDigit(3)
        vm.addZeros(2)
        assertEquals("300", vm.uiState.value.calcState.digits)
        assertEquals(300, vm.uiState.value.calcState.previewValue)
    }

    @Test
    fun `applyCalcChange with divide halves LP`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        vm.openCalc(Player.PLAYER_1)
        vm.divideBy2()
        vm.applyCalcChange()
        assertEquals(4000, vm.uiState.value.player1Lp)
    }

    @Test
    fun `applyCalcChange subtracts from player1 LP`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        vm.openCalc(Player.PLAYER_1)
        vm.inputDigit(5)
        vm.applyCalcChange()
        val state = vm.uiState.value
        assertEquals(7500, state.player1Lp)
        assertEquals(1, state.history.size)
        assertFalse(state.showCalcSheet)
    }

    @Test
    fun `applyCalcChange adds to player2 LP`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        vm.openCalc(Player.PLAYER_2)
        vm.setOperation(Operation.ADD)
        vm.inputDigit(3)
        vm.inputDigit(0)
        vm.inputDigit(0)
        vm.applyCalcChange()
        val state = vm.uiState.value
        assertEquals(8000, state.player1Lp)
        assertEquals(8300, state.player2Lp)
    }

    @Test
    fun `applyCalcChange does not allow LP below zero`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        vm.openCalc(Player.PLAYER_1)
        vm.inputDigit(9)
        vm.inputDigit(0)
        vm.inputDigit(0)
        vm.inputDigit(0)
        vm.applyCalcChange()
        assertEquals(0, vm.uiState.value.player1Lp)
    }

    @Test
    fun `divideBy2 sets operation to divide and requires apply`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        vm.openCalc(Player.PLAYER_1)
        vm.divideBy2()
        val state = vm.uiState.value
        assertEquals(Operation.DIVIDE, state.calcState.operation)
        assertTrue(state.calcState.lockedIn)
        assertTrue(state.showCalcSheet)
        vm.applyCalcChange()
        assertEquals(4000, vm.uiState.value.player1Lp)
        assertEquals(1, vm.uiState.value.history.size)
        assertEquals(Operation.DIVIDE, vm.uiState.value.history[0].operation)
        assertFalse(vm.uiState.value.showCalcSheet)
    }

    @Test
    fun `undoLastChange restores previous LP`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        vm.openCalc(Player.PLAYER_1)
        vm.inputDigit(5)
        vm.applyCalcChange()
        assertEquals(7500, vm.uiState.value.player1Lp)
        vm.undoLastChange()
        assertEquals(8000, vm.uiState.value.player1Lp)
        assertTrue(vm.uiState.value.history.isEmpty())
    }

    @Test
    fun `undoLastChange undoes correct player`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        vm.openCalc(Player.PLAYER_1)
        vm.inputDigit(5)
        vm.applyCalcChange()
        vm.openCalc(Player.PLAYER_2)
        vm.inputDigit(3)
        vm.applyCalcChange()
        assertEquals(7500, vm.uiState.value.player1Lp)
        assertEquals(7700, vm.uiState.value.player2Lp)
        vm.undoLastChange()
        assertEquals(8000, vm.uiState.value.player2Lp)
        assertEquals(7500, vm.uiState.value.player1Lp)
    }

    @Test
    fun `undoLastChange does nothing with empty history`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        vm.undoLastChange()
        assertEquals(8000, vm.uiState.value.player1Lp)
    }

    @Test
    fun `resetAll restores LP and resets timer`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        vm.openCalc(Player.PLAYER_1)
        vm.inputDigit(3)
        vm.applyCalcChange()
        assertEquals(7700, vm.uiState.value.player1Lp)
        vm.setTimerStartValue(30)
        vm.resetTimer()
        vm.startPauseTimer()
        assertTrue(vm.uiState.value.isRunning)
        vm.resetAll()
        assertEquals(8000, vm.uiState.value.player1Lp)
        assertEquals(8000, vm.uiState.value.player2Lp)
        assertFalse(vm.uiState.value.isRunning)
        assertEquals(0, vm.uiState.value.elapsedSeconds)
        assertEquals(30 * 60, vm.uiState.value.remainingSeconds)
    }

    @Test
    fun `setTimerMode resets LP and timer`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        vm.openCalc(Player.PLAYER_1)
        vm.inputDigit(5)
        vm.applyCalcChange()
        assertEquals(7500, vm.uiState.value.player1Lp)
        vm.setTimerMode(TimerMode.COUNT_UP)
        assertEquals(TimerMode.COUNT_UP, vm.uiState.value.timerMode)
        assertEquals(8000, vm.uiState.value.player1Lp)
        assertEquals(0, vm.uiState.value.elapsedSeconds)
    }

    @Test
    fun `pauseTimer stops running timer`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        vm.startPauseTimer()
        assertTrue(vm.uiState.value.isRunning)
        vm.pauseTimer()
        assertFalse(vm.uiState.value.isRunning)
    }

    @Test
    fun `setTimerMode switches mode`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        vm.setTimerMode(TimerMode.COUNT_UP)
        assertEquals(TimerMode.COUNT_UP, vm.uiState.value.timerMode)
    }

    @Test
    fun `setStartingLp resets both players LP and timer`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        vm.openCalc(Player.PLAYER_1)
        vm.inputDigit(3)
        vm.applyCalcChange()
        assertEquals(7700, vm.uiState.value.player1Lp)
        vm.setStartingLp(4000)
        assertEquals(4000, vm.uiState.value.player1Lp)
        assertEquals(4000, vm.uiState.value.player2Lp)
        assertEquals(4000, vm.uiState.value.startingLp)
        assertEquals(45 * 60, vm.uiState.value.remainingSeconds)
    }

    @Test
    fun `toggleHistory toggles showHistory flag`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        assertFalse(vm.uiState.value.showHistory)
        vm.toggleHistory()
        assertTrue(vm.uiState.value.showHistory)
    }
}
