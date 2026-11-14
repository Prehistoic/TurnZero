package com.turnzero.games.yugioh.tools.lifecounter

import org.junit.Assert.assertEquals
import org.junit.Test

class LifeCounterModelsTest {

    @Test
    fun `LpColorChange carries the edited player and lp values`() {
        val change = LpColorChange(
            player = Player.PLAYER_2,
            previousLp = 8000,
            newLp = 8300,
            id = 42L
        )
        assertEquals(Player.PLAYER_2, change.player)
        assertEquals(8000, change.previousLp)
        assertEquals(8300, change.newLp)
        assertEquals(42L, change.id)
    }

    @Test
    fun `LifeCounterUiState has no lpColorChange by default`() {
        assertEquals(null, LifeCounterUiState().lpColorChange)
    }
}
