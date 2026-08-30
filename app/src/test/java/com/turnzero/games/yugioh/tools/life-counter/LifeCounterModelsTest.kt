package com.turnzero.games.yugioh.tools.lifecounter

import org.junit.Assert.assertEquals
import org.junit.Test

class LifeCounterModelsTest {

    @Test
    fun `computeLpChangeDirection returns zero when snapped regardless of lp delta`() {
        assertEquals(0, computeLpChangeDirection(previousLp = 5000, newLp = 8000, snapped = true))
        assertEquals(0, computeLpChangeDirection(previousLp = 8000, newLp = 5000, snapped = true))
    }

    @Test
    fun `computeLpChangeDirection returns positive on increase and negative on decrease when not snapped`() {
        assertEquals(1, computeLpChangeDirection(previousLp = 8000, newLp = 8500, snapped = false))
        assertEquals(-1, computeLpChangeDirection(previousLp = 8000, newLp = 7500, snapped = false))
        assertEquals(0, computeLpChangeDirection(previousLp = 8000, newLp = 8000, snapped = false))
    }
}
