package com.turnzero.games.yugioh.tools.lifecounter

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
        LifeCounterUiState(remainingSeconds = 45 * 60L).copy(
            player1Lp = savedStateHandle.get<Int>(KEY_PLAYER1_LP) ?: 8000,
            player2Lp = savedStateHandle.get<Int>(KEY_PLAYER2_LP) ?: 8000,
            startingLp = savedStateHandle.get<Int>(KEY_STARTING_LP) ?: 8000,
        )
    )
    val uiState: StateFlow<LifeCounterUiState> = _uiState.asStateFlow()

    private inline fun updateState(transform: (LifeCounterUiState) -> LifeCounterUiState) {
        val updated = transform(_uiState.value)
        _uiState.value = updated
        savedStateHandle[KEY_PLAYER1_LP] = updated.player1Lp
        savedStateHandle[KEY_PLAYER2_LP] = updated.player2Lp
        savedStateHandle[KEY_STARTING_LP] = updated.startingLp
    }

    internal fun tick() {
        updateState { current ->
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
            updateState { it.copy(isRunning = false, flashZero = false) }
            timerJob?.cancel()
            timerJob = null
        } else {
            updateState { it.copy(isRunning = true, hasEverStarted = true) }
            timerJob = viewModelScope.launch {
                var nextTick = System.nanoTime() + 1_000_000_000L
                while (isActive) {
                    val now = System.nanoTime()
                    val waitMs = ((nextTick - now) / 1_000_000).coerceAtLeast(0)
                    delay(waitMs)
                    tick()
                    nextTick += 1_000_000_000L
                }
            }
        }
    }

    fun pauseTimer() {
        if (_uiState.value.isRunning) {
            updateState { it.copy(isRunning = false) }
            timerJob?.cancel()
            timerJob = null
        }
    }

    fun resetTimer() {
        timerJob?.cancel()
        timerJob = null
        val minutes = _uiState.value.timerStartValueMinutes
        updateState {
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
        updateState {
            it.copy(
                showCalcSheet = true,
                activePlayer = player,
                calcState = CalcInputState()
            )
        }
    }

    fun closeCalc() {
        updateState { it.copy(showCalcSheet = false) }
    }

    fun setOperation(op: Operation) {
        updateState {
            it.copy(calcState = it.calcState.copy(operation = op, digits = "", previewValue = 0, trailingZeroCount = 0, lockedIn = false))
        }
    }

    fun inputDigit(d: Int) {
        updateState { state ->
            if (d == 0 && state.calcState.digits.isEmpty()) return@updateState state
            val newDigits = state.calcState.digits + d.toString()
            val isZero = d == 0
            val result = if (state.calcState.lockedIn || isZero) {
                (newDigits.toIntOrNull() ?: 0) to 0
            } else {
                computePreview(newDigits)
            }
            state.copy(
                calcState = state.calcState.copy(
                    digits = newDigits,
                    previewValue = result.first,
                    trailingZeroCount = result.second,
                    lockedIn = state.calcState.lockedIn || isZero
                )
            )
        }
    }

    fun addZeros(count: Int) {
        updateState { state ->
            if (state.calcState.digits.isEmpty()) return@updateState state
            val newDigits = state.calcState.digits + "0".repeat(count)
            val result = (newDigits.toIntOrNull() ?: 0) to 0
            state.copy(
                calcState = state.calcState.copy(
                    digits = newDigits,
                    previewValue = result.first,
                    trailingZeroCount = result.second,
                    lockedIn = true
                )
            )
        }
    }

    fun clearInput() {
        updateState {
            it.copy(calcState = it.calcState.copy(digits = "", previewValue = 0, trailingZeroCount = 0, lockedIn = false))
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
        updateState {
            it.copy(calcState = CalcInputState(operation = Operation.DIVIDE, lockedIn = true))
        }
    }

    fun resetAll() {
        timerJob?.cancel()
        timerJob = null
        val lp = _uiState.value.startingLp
        val minutes = _uiState.value.timerStartValueMinutes
        updateState {
            it.copy(
                isRunning = false,
                player1Lp = lp,
                player2Lp = lp,
                elapsedSeconds = 0,
                remainingSeconds = if (it.timerMode == TimerMode.COUNT_DOWN) minutes * 60L else 0,
                flashZero = false,
                hasEverStarted = false,
                history = emptyList(),
                snapLpAnimation = true
            )
        }
    }

    fun undoLastChange() {
    val history = _uiState.value.history
    if (history.isEmpty()) return
    val last = history.first()
    updateState {
        val updated = if (last.player == Player.PLAYER_1)
            it.copy(player1Lp = last.previousLp)
        else
            it.copy(player2Lp = last.previousLp)
        updated.copy(history = history.drop(1), snapLpAnimation = true)
    }
}

    fun setTimerMode(mode: TimerMode) {
        timerJob?.cancel()
        timerJob = null
        val lp = _uiState.value.startingLp
        val minutes = _uiState.value.timerStartValueMinutes
        updateState {
            it.copy(
                timerMode = mode,
                isRunning = false,
                player1Lp = lp,
                player2Lp = lp,
                elapsedSeconds = 0,
                remainingSeconds = if (mode == TimerMode.COUNT_DOWN) minutes * 60L else 0,
                flashZero = false,
                hasEverStarted = false,
                history = emptyList(),
                snapLpAnimation = true
            )
        }
    }

    fun setTimerStartValue(minutes: Int) {
        timerJob?.cancel()
        timerJob = null
        val lp = _uiState.value.startingLp
        updateState {
            it.copy(
                timerStartValueMinutes = minutes,
                isRunning = false,
                player1Lp = lp,
                player2Lp = lp,
                elapsedSeconds = 0,
                remainingSeconds = if (it.timerMode == TimerMode.COUNT_DOWN) minutes * 60L else 0,
                flashZero = false,
                hasEverStarted = false,
                history = emptyList(),
                snapLpAnimation = true
            )
        }
    }

    fun setStartingLp(lp: Int) {
        timerJob?.cancel()
        timerJob = null
        val minutes = _uiState.value.timerStartValueMinutes
        updateState {
            it.copy(
                startingLp = lp,
                player1Lp = lp,
                player2Lp = lp,
                isRunning = false,
                elapsedSeconds = 0,
                remainingSeconds = if (it.timerMode == TimerMode.COUNT_DOWN) minutes * 60L else 0,
                flashZero = false,
                hasEverStarted = false,
                history = emptyList(),
                snapLpAnimation = true
            )
        }
    }

    fun toggleHistory() {
        updateState { it.copy(showHistory = !it.showHistory) }
    }

    fun clearHistory() {
        updateState { it.copy(history = emptyList()) }
    }

    fun consumedFlashZero() {
        updateState { it.copy(flashZero = false) }
    }

    fun consumedSnapLpAnimation() {
        updateState { it.copy(snapLpAnimation = false) }
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
        updateState {
            val base = it.copy(
                history = listOf(change) + it.history,
                showCalcSheet = false,
                calcState = CalcInputState()
            )
            if (isPlayer1) base.copy(player1Lp = newLp) else base.copy(player2Lp = newLp)
        }
    }

    companion object {
        private const val KEY_PLAYER1_LP = "player1_lp"
        private const val KEY_PLAYER2_LP = "player2_lp"
        private const val KEY_STARTING_LP = "starting_lp"

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
