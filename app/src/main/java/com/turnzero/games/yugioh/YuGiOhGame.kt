package com.turnzero.games.yugioh

import com.turnzero.core.tools.api.Game

object YuGiOhGame : Game {
    override val id = "yugioh"
    override val name = "Yu-Gi-Oh!"
    override val description = "Trading Card Game"
    override val toolIds = listOf("dice", "coin", "ygo-life-counter", "ygo-rulings-searcher")
}
