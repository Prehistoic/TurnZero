package com.turnzero.tools.dice

fun getPresetsForGame(gameId: String): List<DicePreset> = when (gameId) {
    "yugioh" -> listOf(
        DicePreset(
            id = "ygo-1d6",
            name = "1d6",
            gameId = gameId,
            dice = listOf(
                Die(id = "p1d1", faceCount = 6, color = DiceColor.WHITE)
            )
        ),
        DicePreset(
            id = "ygo-2d6",
            name = "2d6",
            gameId = gameId,
            dice = listOf(
                Die(id = "p2d1", faceCount = 6, color = DiceColor.WHITE),
                Die(id = "p2d2", faceCount = 6, color = DiceColor.WHITE)
            )
        )
    )
    else -> listOf(
        DicePreset(
            id = "default-2d6",
            name = "Standard 2d6",
            gameId = gameId,
            dice = listOf(
                Die(id = "d1d1", faceCount = 6, color = DiceColor.RED),
                Die(id = "d1d2", faceCount = 6, color = DiceColor.RED)
            )
        )
    )
}
