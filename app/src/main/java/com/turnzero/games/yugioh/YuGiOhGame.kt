package com.turnzero.games.yugioh

import com.turnzero.R
import com.turnzero.core.tools.api.Game
import com.turnzero.core.tools.api.GameCategory

object YuGiOhGame : Game {
    override val id = "yugioh"
    override val name = "Yu-Gi-Oh!"
    override val description = "Trading Card Game"
    override val category = GameCategory.TCG
    override val toolIds = listOf("dice", "coin", "ygo-life-counter", "ygo-rulings-searcher")
    override val logoRes = R.drawable.ic_game_yugioh
}
