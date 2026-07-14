package com.turnzero.games.pokemon

import com.turnzero.R
import com.turnzero.core.tools.api.Game
import com.turnzero.core.tools.api.GameCategory

object PokemonGame : Game {
    override val id = "pokemon"
    override val name = "Pokemon TCG"
    override val description = "Trading Card Game"
    override val category = GameCategory.TCG
    override val toolIds = listOf("dice", "coin")
    override val logoRes = R.drawable.ic_game_pokemon
}
