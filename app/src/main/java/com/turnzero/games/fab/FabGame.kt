package com.turnzero.games.fab

import com.turnzero.R
import com.turnzero.core.tools.api.Game
import com.turnzero.core.tools.api.GameCategory

object FabGame : Game {
    override val id = "fab"
    override val name = "Flesh and Blood"
    override val description = "Trading Card Game"
    override val category = GameCategory.TCG
    override val toolIds = listOf("dice", "coin")
    override val logoRes = R.drawable.ic_game_fab
}
