package com.turnzero.tools.dice

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
class DiceViewModel @Inject constructor(
    private val diceRepo: DiceRepo,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val gameId: String = savedStateHandle.get<String>("gameId") ?: "*"

    private val _uiState = MutableStateFlow(DiceUiState(gameId = gameId))
    val uiState: StateFlow<DiceUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val pool = diceRepo.getPool(gameId)
            val color = diceRepo.getSelectedColor()
            val presets = getPresetsForGame(gameId)
            _uiState.update {
                it.copy(
                    dice = pool,
                    selectedColor = color,
                    presets = presets
                )
            }
        }
    }

    fun changeDieCount(faceCount: Int, color: DiceColor, delta: Int) {
        val current = _uiState.value.dice
        val matching = current.filter { it.faceCount == faceCount && it.color == color }
        if (delta > 0) {
            val clampedDelta = minOf(delta, 20 - current.size)
            if (clampedDelta <= 0) return
            val newDice = (1..clampedDelta).map {
                Die(id = UUID.randomUUID().toString(), faceCount = faceCount, color = color)
            }
            val updated = current + newDice
            persistPool(updated)
        } else {
            val removeCount = minOf(-delta, matching.size)
            val toRemove = matching.take(removeCount).map { it.id }.toSet()
            val updated = current.filter { it.id !in toRemove }
            persistPool(updated)
        }
    }

    fun setSelectedColor(color: DiceColor) {
        _uiState.update { it.copy(selectedColor = color) }
        viewModelScope.launch { diceRepo.saveSelectedColor(color) }
    }

    fun roll() {
        val dice = _uiState.value.dice
        if (dice.isEmpty()) return
        val results = dice.map { Random.nextInt(1, it.faceCount + 1) }
        val total = results.sum()
        _uiState.update { it.copy(isRolling = true, results = results, total = total) }
    }

    fun animationComplete() {
        val state = _uiState.value
        val results = state.results ?: return
        val dice = state.dice
        val roll = DiceRoll(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            dice = dice,
            results = results,
            total = results.sum()
        )
        val updatedHistory = (listOf(roll) + state.history).take(10)
        _uiState.update { it.copy(isRolling = false, history = updatedHistory) }
    }

    fun loadPreset(preset: DicePreset) {
        persistPool(preset.dice)
    }

    fun clearHistory() {
        _uiState.update { it.copy(history = emptyList()) }
    }

    fun toggleHistory() {
        _uiState.update { it.copy(showHistory = !it.showHistory) }
    }

    private fun persistPool(dice: List<Die>) {
        _uiState.update { it.copy(dice = dice) }
        viewModelScope.launch { diceRepo.savePool(gameId, dice) }
    }
}
