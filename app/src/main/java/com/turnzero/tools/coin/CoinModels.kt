package com.turnzero.tools.coin

import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable
import kotlinx.coroutines.flow.Flow

@Serializable
enum class CoinFace { HEAD, TAIL }

@Serializable
data class Coin(val id: String)

@Serializable
data class CoinFlip(
    val id: String,
    val timestamp: Long,
    val coinCount: Int,
    val results: List<CoinFace>,
    val headsCount: Int,
    val tailsCount: Int
)

data class CoinPreset(
    val id: String,
    val name: String,
    val gameId: String,
    val coinCount: Int
)

@Stable
data class CoinUiState(
    val gameId: String = "*",
    val coinCount: Int = 1,
    val presets: List<CoinPreset> = emptyList(),
    val history: List<CoinFlip> = emptyList(),
    val isFlipping: Boolean = false,
    val results: List<CoinFace> = emptyList(),
    val headsCount: Int = 0,
    val tailsCount: Int = 0,
    val showHistory: Boolean = false
)

interface CoinRepo {
    fun getCoinCount(gameId: String): Flow<Int>
    suspend fun saveCoinCount(gameId: String, count: Int)
    fun getHistory(): Flow<List<CoinFlip>>
    suspend fun saveHistory(history: List<CoinFlip>)
}
