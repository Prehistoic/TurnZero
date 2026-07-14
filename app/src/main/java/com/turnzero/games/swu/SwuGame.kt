package com.turnzero.games.swu

import com.turnzero.R
import com.turnzero.core.tools.api.Game
import com.turnzero.core.tools.api.GameCategory

object SwuGame : Game {
    override val id = "swu"
    override val name = "Star Wars Unlimited"
    override val description = "Trading Card Game"
    override val category = GameCategory.TCG
    override val toolIds = listOf("dice", "coin")
    override val logoRes = R.drawable.ic_game_swu
}
