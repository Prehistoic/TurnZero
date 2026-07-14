package com.turnzero.games.mtg

import com.turnzero.R
import com.turnzero.core.tools.api.Game
import com.turnzero.core.tools.api.GameCategory

object MtgGame : Game {
    override val id = "mtg"
    override val name = "Magic: The Gathering"
    override val description = "Trading Card Game"
    override val category = GameCategory.TCG
    override val toolIds = listOf("dice", "coin")
    override val logoRes = R.drawable.ic_game_mtg
}
