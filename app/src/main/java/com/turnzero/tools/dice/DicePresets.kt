package com.turnzero.tools.dice

fun getPresetsForGame(gameId: String): List<DicePreset> = when (gameId) {
    "yugioh" -> listOf(
        DicePreset(
            id = "ygo-standard-2d6",
            name = "Standard 2d6",
            gameId = gameId,
            dice = listOf(
                Die(id = "p1d1", faceCount = 6, color = DiceColor.RED),
                Die(id = "p1d2", faceCount = 6, color = DiceColor.RED)
            )
        ),
        DicePreset(
            id = "ygo-boss-d20",
            name = "Boss d20",
            gameId = gameId,
            dice = listOf(
                Die(id = "p2d1", faceCount = 20, color = DiceColor.GREEN)
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
