package com.turnzero.games.onepiece

import com.turnzero.R
import com.turnzero.core.tools.api.Game
import com.turnzero.core.tools.api.GameCategory

object OnePieceGame : Game {
    override val id = "onepiece"
    override val name = "One Piece Card Game"
    override val description = "Trading Card Game"
    override val category = GameCategory.TCG
    override val toolIds = listOf("dice", "coin")
    override val logoRes = R.drawable.ic_game_onepiece
}
