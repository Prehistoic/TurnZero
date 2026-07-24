package com.turnzero.tools.lifecounter

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turnzero.tools.lifecounter.Defaults.defaultLpByGame
import com.turnzero.tools.lifecounter.Defaults.HISTORY_CAP
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LpCounterViewModel @Inject constructor(
    private val repo: LpCounterRepo,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LpCounterUiState())
    val uiState: StateFlow<LpCounterUiState> = _uiState.asStateFlow()

    fun initialize(gameId: String) {
        val defaultLp = defaultLpByGame[gameId] ?: 20
        val initialPlayers = (1..2).map { LpPlayer(number = it, startingLp = defaultLp, currentLp = defaultLp) }
        _uiState.update { it.copy(players = initialPlayers, globalStartingLp = defaultLp) }
        viewModelScope.launch {
            val config = repo.getConfig()
            applyConfig(config)
        }
    }

    private fun applyConfig(config: LpCounterConfig) {
        val globalLp = config.globalStartingLp
        val players = (1..config.playerCount).map { num ->
            LpPlayer(number = num, startingLp = globalLp, currentLp = globalLp)
        }
        _uiState.update {
            it.copy(
                players = players,
                playerCount = config.playerCount,
                globalStartingLp = globalLp,
            )
        }
    }

    fun incrementPlayer(playerNumber: Int) {
        applyDelta(playerNumber, 1)
    }

    fun decrementPlayer(playerNumber: Int) {
        applyDelta(playerNumber, -1)
    }

    private fun applyDelta(playerNumber: Int, delta: Int) {
        _uiState.update { state ->
            val players = state.players.toMutableList()
            val index = players.indexOfFirst { it.number == playerNumber }
            if (index < 0) return@update state
            val player = players[index]
            players[index] = player.copy(
                currentLp = player.currentLp + delta,
                lastChange = player.lastChange + delta,
            )
            state.copy(players = players)
        }
    }

    fun resetPlayer(playerNumber: Int) {
        _uiState.update { state ->
            val players = state.players.toMutableList()
            val index = players.indexOfFirst { it.number == playerNumber }
            if (index < 0) return@update state
            val player = players[index]
            val delta = player.startingLp - player.currentLp
            players[index] = player.copy(currentLp = player.startingLp, lastChange = 0)
            val change = LpChange(
                playerNumber = playerNumber,
                previousLp = player.currentLp,
                delta = delta,
                newTotal = player.startingLp,
                timestamp = System.currentTimeMillis(),
            )
            val history = (state.changeHistory + change).takeLast(HISTORY_CAP)
            state.copy(players = players, changeHistory = history)
        }
    }

    fun resetAll() {
        _uiState.update { state ->
            val changes = state.players.map { player ->
                LpChange(
                    playerNumber = player.number,
                    previousLp = player.currentLp,
                    delta = player.startingLp - player.currentLp,
                    newTotal = player.startingLp,
                    timestamp = System.currentTimeMillis(),
                )
            }
            val history = (state.changeHistory + changes).takeLast(HISTORY_CAP)
            state.copy(
                players = state.players.map { it.copy(currentLp = it.startingLp, lastChange = 0) },
                changeHistory = history,
            )
        }
    }

    fun setPlayerCount(count: Int) {
        require(count in 2..4) { "Player count must be 2-4" }
        val globalLp = _uiState.value.globalStartingLp
        val players = (1..count).map { num ->
            LpPlayer(number = num, startingLp = globalLp, currentLp = globalLp)
        }
        _uiState.update { it.copy(players = players, playerCount = count) }
        persistConfig(playerCount = count)
    }

    fun setGlobalStartingLp(lp: Int) {
        require(lp > 0) { "LP must be positive" }
        _uiState.update { state ->
            state.copy(
                globalStartingLp = lp,
                players = state.players.map { it.copy(startingLp = lp, currentLp = lp) },
            )
        }
        persistConfig(globalStartingLp = lp)
    }

    fun resetChangeIndicator(playerNumber: Int) {
        _uiState.update { state ->
            val players = state.players.toMutableList()
            val index = players.indexOfFirst { it.number == playerNumber }
            if (index < 0) return@update state
            val player = players[index]
            val netDelta = player.lastChange
            if (netDelta != 0) {
                val previousLp = player.currentLp - netDelta
                val change = LpChange(
                    playerNumber = playerNumber,
                    previousLp = previousLp,
                    delta = netDelta,
                    newTotal = player.currentLp,
                    timestamp = System.currentTimeMillis(),
                )
                val history = (state.changeHistory + change).takeLast(HISTORY_CAP)
                players[index] = player.copy(lastChange = 0)
                state.copy(players = players, changeHistory = history)
            } else {
                state
            }
        }
    }

    fun showOptions(show: Boolean) {
        _uiState.update { it.copy(showOptions = show) }
    }

    fun showHistory(show: Boolean) {
        _uiState.update { it.copy(showHistory = show) }
    }

    fun clearHistory() {
        _uiState.update { it.copy(changeHistory = emptyList()) }
    }

    fun dismissOptions() {
        showOptions(false)
    }

    fun dismissHistory() {
        showHistory(false)
    }

    private fun persistConfig(
        playerCount: Int? = null,
        globalStartingLp: Int? = null,
    ) {
        viewModelScope.launch {
            val current = repo.getConfig()
            val updated = current.copy(
                playerCount = playerCount ?: current.playerCount,
                globalStartingLp = globalStartingLp ?: current.globalStartingLp,
            )
            repo.saveConfig(updated)
        }
    }
}
