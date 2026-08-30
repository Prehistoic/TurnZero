package com.turnzero.games.yugioh.tools.lifecounter

import androidx.compose.runtime.Stable

internal fun computeLpChangeDirection(previousLp: Int, newLp: Int, snapped: Boolean): Int =
    if (snapped) 0 else newLp.compareTo(previousLp)

enum class TimerMode { COUNT_UP, COUNT_DOWN }

enum class Player { PLAYER_1, PLAYER_2 }

enum class Operation { ADD, SUBTRACT, DIVIDE }

data class LpChange(
    val player: Player,
    val previousLp: Int,
    val newLp: Int,
    val delta: Int,
    val operation: Operation,
    val timestamp: Long
)

data class CalcInputState(
    val operation: Operation = Operation.SUBTRACT,
    val digits: String = "",
    val previewValue: Int = 0,
    val trailingZeroCount: Int = 0,
    val lockedIn: Boolean = false
)

@Stable
data class LifeCounterUiState(
    val isRunning: Boolean = false,
    val player1Lp: Int = 8000,
    val player2Lp: Int = 8000,
    val elapsedSeconds: Long = 0,
    val remainingSeconds: Long = 0,
    val timerMode: TimerMode = TimerMode.COUNT_DOWN,
    val timerStartValueMinutes: Int = 45,
    val startingLp: Int = 8000,
    val history: List<LpChange> = emptyList(),
    val showHistory: Boolean = false,
    val showCalcSheet: Boolean = false,
    val activePlayer: Player = Player.PLAYER_1,
    val calcState: CalcInputState = CalcInputState(),
    val flashZero: Boolean = false,
    val hasEverStarted: Boolean = false,
    val snapLpAnimation: Boolean = false
)
