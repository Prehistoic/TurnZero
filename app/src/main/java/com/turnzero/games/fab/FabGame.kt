package com.turnzero.games.fab

import com.turnzero.R
import com.turnzero.core.tools.api.Game
import com.turnzero.core.tools.api.tcgGame

val FabGame: Game = tcgGame(
    id = "fab",
    name = "Flesh and Blood",
    logoRes = R.drawable.ic_game_fab,
    extraToolIds = arrayOf("lp-counter"),
)
