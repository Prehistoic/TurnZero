package com.turnzero.tools.dice

import kotlinx.serialization.Serializable

@Serializable
enum class DiceColor(val hex: Long) {
    RED(0xFFE53935),
    BLUE(0xFF1E88E5),
    GREEN(0xFF43A047),
    YELLOW(0xFFFDD835),
    WHITE(0xFFFFFFFF),
    BLACK(0xFF212121)
}

@Serializable
data class Die(
    val id: String,
    val faceCount: Int,
    val color: DiceColor
)

@Serializable
data class DiceRoll(
    val id: String,
    val timestamp: Long,
    val dice: List<Die>,
    val results: List<Int>,
    val total: Int
)

data class DicePreset(
    val id: String,
    val name: String,
    val gameId: String,
    val dice: List<Die>
)

data class DiceUiState(
    val gameId: String = "*",
    val dice: List<Die> = emptyList(),
    val selectedColor: DiceColor = DiceColor.RED,
    val presets: List<DicePreset> = emptyList(),
    val history: List<DiceRoll> = emptyList(),
    val isRolling: Boolean = false,
    val results: List<Int>? = null,
    val total: Int? = null,
    val showHistory: Boolean = false
)

interface DiceRepo {
    suspend fun getPool(gameId: String): List<Die>
    suspend fun savePool(gameId: String, dice: List<Die>)
    suspend fun getSelectedColor(): DiceColor
    suspend fun saveSelectedColor(color: DiceColor)
    suspend fun getHistory(): List<DiceRoll>
    suspend fun saveHistory(history: List<DiceRoll>)
}
