package com.turnzero.games.pokemon

import com.turnzero.R
import com.turnzero.core.tools.api.Game
import com.turnzero.core.tools.api.tcgGame

val PokemonGame: Game = tcgGame(
    id = "pokemon",
    name = "Pokemon TCG",
    logoRes = R.drawable.ic_game_pokemon,
)
