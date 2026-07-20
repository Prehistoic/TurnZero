package com.turnzero.games.digimon

import com.turnzero.R
import com.turnzero.core.tools.api.Game
import com.turnzero.core.tools.api.tcgGame

val DigimonGame: Game = tcgGame(
    id = "digimon",
    name = "Digimon Card Game",
    logoRes = R.drawable.ic_game_digimon,
)
