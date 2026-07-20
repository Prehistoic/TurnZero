package com.turnzero.games.mtg

import com.turnzero.R
import com.turnzero.core.tools.api.Game
import com.turnzero.core.tools.api.tcgGame

val MtgGame: Game = tcgGame(
    id = "mtg",
    name = "Magic: The Gathering",
    logoRes = R.drawable.ic_game_mtg,
)
