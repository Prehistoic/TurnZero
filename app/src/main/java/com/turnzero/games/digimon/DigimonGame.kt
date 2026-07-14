package com.turnzero.games.digimon

import com.turnzero.R
import com.turnzero.core.tools.api.Game
import com.turnzero.core.tools.api.GameCategory

object DigimonGame : Game {
    override val id = "digimon"
    override val name = "Digimon Card Game"
    override val description = "Trading Card Game"
    override val category = GameCategory.TCG
    override val toolIds = listOf("dice", "coin")
    override val logoRes = R.drawable.ic_game_digimon
}
