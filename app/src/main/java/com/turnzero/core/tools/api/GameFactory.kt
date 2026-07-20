package com.turnzero.core.tools.api

import androidx.annotation.DrawableRes

fun tcgGame(
    id: String,
    name: String,
    description: String = "Trading Card Game",
    @DrawableRes logoRes: Int,
    vararg extraToolIds: String = emptyArray(),
): Game = object : Game {
    override val id = id
    override val name = name
    override val description = description
    override val category = GameCategory.TCG
    override val toolIds = listOf("dice", "coin") + extraToolIds.toList()
    override val logoRes = logoRes
}
