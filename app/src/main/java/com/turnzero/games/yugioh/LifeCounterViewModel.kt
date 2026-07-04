package com.turnzero.games.yugioh

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LifeCounterViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        LifeCounterUiState(remainingSeconds = 45 * 60L)
    )
    val uiState: StateFlow<LifeCounterUiState> = _uiState.asStateFlow()

    internal fun tick() {
        _uiState.update { current ->
            when (current.timerMode) {
                TimerMode.COUNT_UP -> {
                    current.copy(elapsedSeconds = current.elapsedSeconds + 1)
                }
                TimerMode.COUNT_DOWN -> {
                    val newRemaining = current.remainingSeconds - 1
                    if (newRemaining <= 0) {
                        timerJob?.cancel()
                        timerJob = null
                        current.copy(
                            remainingSeconds = 0,
                            isRunning = false,
                            flashZero = true
                        )
                    } else {
                        current.copy(remainingSeconds = newRemaining)
                    }
                }
            }
        }
    }

    private var timerJob: Job? = null

    fun startPauseTimer() {
        val state = _uiState.value
        if (state.isRunning) {
            _uiState.update { it.copy(isRunning = false, flashZero = false) }
            timerJob?.cancel()
            timerJob = null
        } else {
            _uiState.update { it.copy(isRunning = true, hasEverStarted = true) }
            timerJob = viewModelScope.launch {
                while (isActive) {
                    delay(1000)
                    tick()
                }
            }
        }
    }

    fun pauseTimer() {
        if (_uiState.value.isRunning) {
            _uiState.update { it.copy(isRunning = false) }
            timerJob?.cancel()
            timerJob = null
        }
    }

    fun resetTimer() {
        timerJob?.cancel()
        timerJob = null
        val minutes = _uiState.value.timerStartValueMinutes
        _uiState.update {
            it.copy(
                isRunning = false,
                elapsedSeconds = 0,
                remainingSeconds = if (it.timerMode == TimerMode.COUNT_DOWN) minutes * 60L else 0,
                flashZero = false,
                hasEverStarted = false
            )
        }
    }

    fun openCalc(player: Player) {
        _uiState.update {
            it.copy(
                showCalcSheet = true,
                activePlayer = player,
                calcState = CalcInputState()
            )
        }
    }

    fun closeCalc() {
        _uiState.update { it.copy(showCalcSheet = false) }
    }

    fun setOperation(op: Operation) {
        _uiState.update {
            it.copy(calcState = it.calcState.copy(operation = op, digits = "", previewValue = 0, trailingZeroCount = 0))
        }
    }

    fun inputDigit(d: Int) {
        _uiState.update { state ->
            val newDigits = state.calcState.digits + d.toString()
            val (preview, trailingZeros) = computePreview(newDigits)
            state.copy(
                calcState = state.calcState.copy(
                    digits = newDigits,
                    previewValue = preview,
                    trailingZeroCount = trailingZeros
                )
            )
        }
    }

    fun clearInput() {
        _uiState.update {
            it.copy(calcState = it.calcState.copy(digits = "", previewValue = 0, trailingZeroCount = 0))
        }
    }

    fun addZeros(count: Int) {
        _uiState.update { state ->
            val newDigits = state.calcState.digits + "0".repeat(count)
            val (preview, trailingZeros) = computePreview(newDigits)
            state.copy(
                calcState = state.calcState.copy(
                    digits = newDigits,
                    previewValue = preview,
                    trailingZeroCount = trailingZeros
                )
            )
        }
    }

    fun applyCalcChange() {
        val state = _uiState.value
        val currentLp = activePlayerLp(state)
        val preview = state.calcState.previewValue
        val newLp = when (state.calcState.operation) {
            Operation.ADD -> currentLp + preview
            Operation.SUBTRACT -> maxOf(0, currentLp - preview)
            Operation.DIVIDE -> currentLp / 2
        }
        recordChangeAndClose(state.calcState.operation, preview, newLp)
    }

    fun divideBy2() {
        val state = _uiState.value
        val currentLp = activePlayerLp(state)
        val newLp = currentLp / 2
        recordChangeAndClose(Operation.DIVIDE, currentLp - newLp, newLp)
    }

    fun resetAll() {
        timerJob?.cancel()
        timerJob = null
        val lp = _uiState.value.startingLp
        val minutes = _uiState.value.timerStartValueMinutes
        _uiState.update {
            it.copy(
                isRunning = false,
                player1Lp = lp,
                player2Lp = lp,
                elapsedSeconds = 0,
                remainingSeconds = if (it.timerMode == TimerMode.COUNT_DOWN) minutes * 60L else 0,
                flashZero = false,
                hasEverStarted = false,
                history = emptyList()
            )
        }
    }

    fun undoLastChange() {
        val history = _uiState.value.history
        if (history.isEmpty()) return
        val last = history.first()
        _uiState.update {
            val updated = if (last.player == Player.PLAYER_1)
                it.copy(player1Lp = last.previousLp)
            else
                it.copy(player2Lp = last.previousLp)
            updated.copy(history = history.drop(1))
        }
    }

    fun setTimerMode(mode: TimerMode) {
        timerJob?.cancel()
        timerJob = null
        val lp = _uiState.value.startingLp
        val minutes = _uiState.value.timerStartValueMinutes
        _uiState.update {
            it.copy(
                timerMode = mode,
                isRunning = false,
                player1Lp = lp,
                player2Lp = lp,
                elapsedSeconds = 0,
                remainingSeconds = if (mode == TimerMode.COUNT_DOWN) minutes * 60L else 0,
                flashZero = false,
                hasEverStarted = false,
                history = emptyList()
            )
        }
    }

    fun setTimerStartValue(minutes: Int) {
        timerJob?.cancel()
        timerJob = null
        val lp = _uiState.value.startingLp
        _uiState.update {
            it.copy(
                timerStartValueMinutes = minutes,
                isRunning = false,
                player1Lp = lp,
                player2Lp = lp,
                elapsedSeconds = 0,
                remainingSeconds = if (it.timerMode == TimerMode.COUNT_DOWN) minutes * 60L else 0,
                flashZero = false,
                hasEverStarted = false,
                history = emptyList()
            )
        }
    }

    fun setStartingLp(lp: Int) {
        timerJob?.cancel()
        timerJob = null
        val minutes = _uiState.value.timerStartValueMinutes
        _uiState.update {
            it.copy(
                startingLp = lp,
                player1Lp = lp,
                player2Lp = lp,
                isRunning = false,
                elapsedSeconds = 0,
                remainingSeconds = if (it.timerMode == TimerMode.COUNT_DOWN) minutes * 60L else 0,
                flashZero = false,
                hasEverStarted = false,
                history = emptyList()
            )
        }
    }

    fun toggleHistory() {
        _uiState.update { it.copy(showHistory = !it.showHistory) }
    }

    fun clearHistory() {
        _uiState.update { it.copy(history = emptyList()) }
    }

    fun consumedFlashZero() {
        _uiState.update { it.copy(flashZero = false) }
    }

    private fun activePlayerLp(state: LifeCounterUiState): Int {
        return if (state.activePlayer == Player.PLAYER_1) state.player1Lp else state.player2Lp
    }

    private fun recordChangeAndClose(op: Operation, delta: Int, newLp: Int) {
        val state = _uiState.value
        val currentLp = activePlayerLp(state)
        val isPlayer1 = state.activePlayer == Player.PLAYER_1
        val change = LpChange(
            player = state.activePlayer,
            previousLp = currentLp,
            newLp = newLp,
            delta = delta,
            operation = op,
            timestamp = System.currentTimeMillis()
        )
        _uiState.update {
            val base = it.copy(
                history = listOf(change) + it.history,
                showCalcSheet = false,
                calcState = CalcInputState()
            )
            if (isPlayer1) base.copy(player1Lp = newLp) else base.copy(player2Lp = newLp)
        }
    }

    companion object {
        fun computePreview(digits: String): Pair<Int, Int> {
            if (digits.isEmpty()) return 0 to 0
            return when {
                digits.length <= 2 -> digits.toInt() * 100 to 2
                digits.length == 3 -> digits.toInt() * 10 to 1
                else -> digits.toInt() to 0
            }
        }
    }
}
