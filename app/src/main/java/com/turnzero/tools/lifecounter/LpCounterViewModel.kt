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
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class LpCounterViewModel @Inject constructor(
    private val repo: LpCounterRepo,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LpCounterUiState())
    val uiState: StateFlow<LpCounterUiState> = _uiState.asStateFlow()

    private var currentGameId: String? = null
    private val gameStates = mutableMapOf<String, LpCounterUiState>()

    fun initialize(gameId: String) {
        currentGameId = gameId
        val existing = gameStates[gameId] ?: restoreState(gameId)
        if (existing != null) {
            _uiState.value = existing
            return
        }
        val defaultLp = defaultLpByGame[gameId] ?: 20
        val initial = LpCounterUiState(
            players = (1..2).map { LpPlayer(number = it, startingLp = defaultLp, currentLp = defaultLp) },
            globalStartingLp = defaultLp,
        )
        gameStates[gameId] = initial
        _uiState.value = initial
        viewModelScope.launch {
            val config = repo.getConfig()
            applyConfig(config, gameId)
        }
    }

    private fun applyConfig(config: LpCounterConfig, gameId: String) {
        val base = gameStates[gameId] ?: return
        val players = (1..config.playerCount).map { num ->
            LpPlayer(number = num, startingLp = config.globalStartingLp, currentLp = config.globalStartingLp)
        }
        val updated = base.copy(
            players = players,
            playerCount = config.playerCount,
            globalStartingLp = config.globalStartingLp,
        )
        gameStates[gameId] = updated
        persistState(gameId, updated)
        if (currentGameId == gameId) {
            _uiState.value = updated
        }
    }

    private inline fun updateState(transform: (LpCounterUiState) -> LpCounterUiState) {
        val updated = transform(_uiState.value)
        _uiState.value = updated
        val gameId = currentGameId
        if (gameId != null) {
            gameStates[gameId] = updated
            persistState(gameId, updated)
        }
    }

    private fun persistState(gameId: String, state: LpCounterUiState) {
        savedStateHandle[stateKey(gameId)] = json.encodeToString(
            LpCounterSnapshot(
                players = state.players,
                playerCount = state.playerCount,
                globalStartingLp = state.globalStartingLp,
            )
        )
    }

    private fun restoreState(gameId: String): LpCounterUiState? {
        val raw = try {
            savedStateHandle.get<String>(stateKey(gameId))
        } catch (_: Exception) {
            null
        } ?: return null
        return try {
            val snapshot = json.decodeFromString<LpCounterSnapshot>(raw)
            LpCounterUiState(
                players = snapshot.players,
                playerCount = snapshot.playerCount,
                globalStartingLp = snapshot.globalStartingLp,
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun stateKey(gameId: String) = "lp_counter_state_$gameId"

    fun incrementPlayer(playerNumber: Int) {
        applyDelta(playerNumber, 1)
    }

    fun decrementPlayer(playerNumber: Int) {
        applyDelta(playerNumber, -1)
    }

    private fun applyDelta(playerNumber: Int, delta: Int) {
        updateState { state ->
            val players = state.players.toMutableList()
            val index = players.indexOfFirst { it.number == playerNumber }
            if (index < 0) return@updateState state
            val player = players[index]
            players[index] = player.copy(
                currentLp = player.currentLp + delta,
                lastChange = player.lastChange + delta,
            )
            state.copy(players = players)
        }
    }

    fun resetPlayer(playerNumber: Int) {
        updateState { state ->
            val players = state.players.toMutableList()
            val index = players.indexOfFirst { it.number == playerNumber }
            if (index < 0) return@updateState state
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
        updateState { state ->
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
        updateState { it.copy(players = players, playerCount = count) }
        persistConfig(playerCount = count)
    }

    fun setGlobalStartingLp(lp: Int) {
        require(lp > 0) { "LP must be positive" }
        updateState { state ->
            state.copy(
                globalStartingLp = lp,
                players = state.players.map { it.copy(startingLp = lp, currentLp = lp) },
            )
        }
        persistConfig(globalStartingLp = lp)
    }

    fun resetChangeIndicator(playerNumber: Int) {
        updateState { state ->
            val players = state.players.toMutableList()
            val index = players.indexOfFirst { it.number == playerNumber }
            if (index < 0) return@updateState state
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
        updateState { it.copy(showOptions = show) }
    }

    fun showHistory(show: Boolean) {
        updateState { it.copy(showHistory = show) }
    }

    fun clearHistory() {
        updateState { it.copy(changeHistory = emptyList()) }
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

    companion object {
        private val json = Json { ignoreUnknownKeys = true }
    }
}
