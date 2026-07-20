package com.turnzero.games.yugioh

import com.turnzero.R
import com.turnzero.core.tools.api.Game
import com.turnzero.core.tools.api.tcgGame

val YuGiOhGame: Game = tcgGame(
    id = "yugioh",
    name = "Yu-Gi-Oh!",
    description = "Trading Card Game",
    logoRes = R.drawable.ic_game_yugioh,
    extraToolIds = arrayOf("ygo-life-counter", "ygo-rulings-searcher"),
)
