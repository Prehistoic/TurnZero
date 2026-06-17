package com.turnzero.games.yugioh

import com.turnzero.core.tools.api.Game

object YuGiOhGame : Game {
    override val id = "yugioh"
    override val name = "Yu-Gi-Oh!"
    override val description = "Trading Card Game"
    override val imageRes = android.R.drawable.ic_menu_gallery
    override val toolIds = listOf("dice", "ygo-life-counter", "ygo-rulings-searcher")
}
