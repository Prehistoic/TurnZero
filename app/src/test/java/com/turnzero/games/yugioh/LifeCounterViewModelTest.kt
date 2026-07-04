package com.turnzero.games.yugioh

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LifeCounterViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var savedStateHandle: SavedStateHandle

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        savedStateHandle = SavedStateHandle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default values`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        val state = vm.uiState.value
        assertEquals(8000, state.player1Lp)
        assertEquals(8000, state.player2Lp)
        assertEquals(TimerMode.COUNT_DOWN, state.timerMode)
        assertEquals(30, state.timerStartValueMinutes)
        assertEquals(8000, state.startingLp)
        assertFalse(state.isRunning)
        assertFalse(state.showCalcSheet)
        assertEquals(0L, state.elapsedSeconds)
        assertEquals(0L, state.remainingSeconds)
    }

    @Test
    fun `startPauseTimer toggles isRunning`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        assertFalse(vm.uiState.value.isRunning)
        vm.startPauseTimer()
        assertTrue(vm.uiState.value.isRunning)
        vm.startPauseTimer()
        assertFalse(vm.uiState.value.isRunning)
    }

    @Test
    fun `timer increments in countUp mode`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        vm.setTimerMode(TimerMode.COUNT_UP)
        vm.resetTimer()
        vm.startPauseTimer()
        advanceTimeBy(3500)
        assertEquals(3, vm.uiState.value.elapsedSeconds)
    }

    @Test
    fun `timer decrements in countDown mode`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        vm.setTimerStartValue(1)
        vm.resetTimer()
        assertEquals(60, vm.uiState.value.remainingSeconds)
        vm.startPauseTimer()
        advanceTimeBy(1200)
        assertEquals(58, vm.uiState.value.remainingSeconds)
    }

    @Test
    fun `countdown stops at zero and sets flashZero`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        vm.setTimerStartValue(1)
        vm.resetTimer()
        assertEquals(60, vm.uiState.value.remainingSeconds)
        vm.startPauseTimer()
        advanceTimeBy(61_000)
        assertEquals(0, vm.uiState.value.remainingSeconds)
        assertFalse(vm.uiState.value.isRunning)
        assertTrue(vm.uiState.value.flashZero)
    }

    @Test
    fun `resetTimer resets elapsed and remaining based on mode`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        vm.setTimerMode(TimerMode.COUNT_UP)
        vm.resetTimer()
        vm.startPauseTimer()
        advanceTimeBy(2000)
        assertEquals(2, vm.uiState.value.elapsedSeconds)
        vm.resetTimer()
        assertEquals(0, vm.uiState.value.elapsedSeconds)
    }

    @Test
    fun `setTimerStartValue updates remainingSeconds on reset`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        vm.setTimerStartValue(45)
        vm.resetTimer()
        assertEquals(45 * 60, vm.uiState.value.remainingSeconds)
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
        assertEquals(5300, cs.previewValue)
        assertEquals(1, cs.trailingZeroCount)
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
    fun `addZeros appends zeros with correct auto-multiply`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        vm.openCalc(Player.PLAYER_1)
        vm.addZeros(2)
        assertEquals("00", vm.uiState.value.calcState.digits)
        assertEquals(0, vm.uiState.value.calcState.previewValue)
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
        assertEquals(11000, state.player2Lp)
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
    fun `divideBy2 immediately halves LP`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        vm.openCalc(Player.PLAYER_1)
        vm.divideBy2()
        val state = vm.uiState.value
        assertEquals(4000, state.player1Lp)
        assertEquals(1, state.history.size)
        assertEquals(Operation.DIVIDE, state.history[0].operation)
        assertFalse(state.showCalcSheet)
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
    fun `setTimerMode switches mode`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        vm.setTimerMode(TimerMode.COUNT_UP)
        assertEquals(TimerMode.COUNT_UP, vm.uiState.value.timerMode)
    }

    @Test
    fun `setStartingLp updates both players on reset`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        vm.setStartingLp(4000)
        vm.resetAll()
        assertEquals(4000, vm.uiState.value.player1Lp)
        assertEquals(4000, vm.uiState.value.startingLp)
    }

    @Test
    fun `toggleHistory toggles showHistory flag`() = runTest {
        val vm = LifeCounterViewModel(savedStateHandle)
        assertFalse(vm.uiState.value.showHistory)
        vm.toggleHistory()
        assertTrue(vm.uiState.value.showHistory)
    }
}
