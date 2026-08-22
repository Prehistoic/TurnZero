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
    "mtg" -> listOf(
        DicePreset(
            id = "mtg-1d20",
            name = "1d20",
            gameId = gameId,
            dice = listOf(
                Die(id = "m1d1", faceCount = 20, color = DiceColor.WHITE)
            )
        ),
        DicePreset(
            id = "mtg-1d6",
            name = "1d6",
            gameId = gameId,
            dice = listOf(
                Die(id = "m2d1", faceCount = 6, color = DiceColor.WHITE)
            )
        ),
        DicePreset(
            id = "mtg-2d6",
            name = "2d6",
            gameId = gameId,
            dice = listOf(
                Die(id = "m3d1", faceCount = 6, color = DiceColor.WHITE),
                Die(id = "m3d2", faceCount = 6, color = DiceColor.WHITE)
            )
        )
    )
    "pokemon" -> listOf(
        DicePreset(
            id = "pokemon-1d20",
            name = "1d20",
            gameId = gameId,
            dice = listOf(
                Die(id = "pk1d1", faceCount = 20, color = DiceColor.WHITE)
            )
        ),
        DicePreset(
            id = "pokemon-1d6",
            name = "1d6",
            gameId = gameId,
            dice = listOf(
                Die(id = "pk2d1", faceCount = 6, color = DiceColor.WHITE)
            )
        )
    )
    else -> listOf(
        DicePreset(
            id = "default-1d6",
            name = "1d6",
            gameId = gameId,
            dice = listOf(
                Die(id = "g1d1", faceCount = 6, color = DiceColor.WHITE)
            )
        ),
        DicePreset(
            id = "default-2d6",
            name = "2d6",
            gameId = gameId,
            dice = listOf(
                Die(id = "g2d1", faceCount = 6, color = DiceColor.WHITE),
                Die(id = "g2d2", faceCount = 6, color = DiceColor.WHITE)
            )
        ),
        DicePreset(
            id = "default-1d20",
            name = "1d20",
            gameId = gameId,
            dice = listOf(
                Die(id = "g3d1", faceCount = 20, color = DiceColor.WHITE)
            )
        )
    )
}
