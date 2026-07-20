package com.turnzero.tools.coin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class CoinViewModel @Inject constructor(
    private val coinRepo: CoinRepo,
    private val savedStateHandle: SavedStateHandle,
    private val random: Random = Random,
) : ViewModel() {

    private val gameId: String = savedStateHandle.get<String>("gameId") ?: "*"

    private val _uiState = MutableStateFlow(CoinUiState(gameId = gameId))
    val uiState: StateFlow<CoinUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            coinRepo.getCoinCount(gameId).collect { count ->
                _uiState.update { it.copy(coinCount = count.coerceIn(1, 8)) }
            }
        }
        viewModelScope.launch {
            val presets = getCoinPresets(gameId)
            _uiState.update { it.copy(presets = presets) }
        }
        viewModelScope.launch {
            coinRepo.getHistory().collect { history ->
                _uiState.update { it.copy(history = history) }
            }
        }
    }

    fun setCoinCount(count: Int) {
        val clamped = count.coerceIn(1, 8)
        _uiState.update { it.copy(coinCount = clamped) }
        viewModelScope.launch { coinRepo.saveCoinCount(gameId, clamped) }
    }

    fun flip() {
        val coinCount = _uiState.value.coinCount
        val results = (1..coinCount).map {
            if (random.nextBoolean()) CoinFace.HEAD else CoinFace.TAIL
        }
        _uiState.update {
            it.copy(
                isFlipping = true,
                results = results,
                headsCount = results.count { face -> face == CoinFace.HEAD },
                tailsCount = results.count { face -> face == CoinFace.TAIL }
            )
        }
    }

    fun flipAnimationComplete() {
        val state = _uiState.value
        val flip = CoinFlip(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            coinCount = state.coinCount,
            results = state.results,
            headsCount = state.headsCount,
            tailsCount = state.tailsCount
        )
        val updatedHistory = (listOf(flip) + state.history).take(10)
        _uiState.update { it.copy(isFlipping = false, history = updatedHistory) }
        viewModelScope.launch { coinRepo.saveHistory(updatedHistory) }
    }

    fun loadPreset(preset: CoinPreset) {
        setCoinCount(preset.coinCount)
    }

    fun clearHistory() {
        _uiState.update { it.copy(history = emptyList()) }
        viewModelScope.launch { coinRepo.saveHistory(emptyList()) }
    }

    fun toggleHistory() {
        _uiState.update { it.copy(showHistory = !it.showHistory) }
    }
}
