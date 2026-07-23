package com.turnzero.tools.lifecounter

import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable

@Stable
data class LpCounterUiState(
    val players: List<LpPlayer> = listOf(
        LpPlayer(number = 1, startingLp = 20, currentLp = 20),
        LpPlayer(number = 2, startingLp = 20, currentLp = 20)
    ),
    val playerCount: Int = 2,
    val globalStartingLp: Int = 20,
    val changeHistory: List<LpChange> = emptyList(),
    val showOptions: Boolean = false,
    val showHistory: Boolean = false,
)

data class LpPlayer(
    val number: Int,
    val startingLp: Int,
    val currentLp: Int,
    val lastChange: Int = 0,
) {
    init {
        require(number in 1..4) { "Player number must be 1-4, got $number" }
    }
}

@Serializable
data class LpChange(
    val playerNumber: Int,
    val previousLp: Int,
    val delta: Int,
    val newTotal: Int,
    val timestamp: Long,
)

@Serializable
data class LpCounterConfig(
    val playerCount: Int = 2,
    val globalStartingLp: Int = 20,
    val perPlayerStartingLp: Map<Int, Int> = emptyMap(),
)

object Defaults {
    val defaultLpByGame = mapOf(
        "mtg" to 20,
        "swu" to 30,
        "fab" to 20,
    )
    val lpPresets = listOf(20, 30, 40)
    const val HISTORY_CAP = 200
}
