package com.turnzero.games.vanguard

import com.turnzero.R
import com.turnzero.core.tools.api.Game
import com.turnzero.core.tools.api.GameCategory

object VanguardGame : Game {
    override val id = "vanguard"
    override val name = "Cardfight!! Vanguard"
    override val description = "Trading Card Game"
    override val category = GameCategory.TCG
    override val toolIds = listOf("dice", "coin")
    override val logoRes = R.drawable.ic_game_vanguard
}
