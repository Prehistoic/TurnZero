package com.turnzero.tools.coin

fun getCoinPresets(gameId: String): List<CoinPreset> = when (gameId) {
    "yugioh" -> listOf(
        CoinPreset(
            id = "ygo-1-coin",
            name = "1 Coin",
            gameId = gameId,
            coinCount = 1
        )
    )
    else -> listOf(
        CoinPreset(
            id = "default-1-coin",
            name = "1 Coin",
            gameId = gameId,
            coinCount = 1
        )
    )
}
