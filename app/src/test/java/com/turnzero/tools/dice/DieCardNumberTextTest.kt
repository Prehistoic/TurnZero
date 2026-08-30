package com.turnzero.tools.dice

import org.junit.Assert.assertEquals
import org.junit.Test

class DieCardNumberTextTest {

    @Test
    fun `dieCardNumberText_whenDisplayNumberIsNull_returnsOne`() {
        assertEquals("1", dieCardNumberText(null))
    }

    @Test
    fun `dieCardNumberText_whenDisplayNumberIsSet_returnsItsString`() {
        assertEquals("6", dieCardNumberText(6))
        assertEquals("20", dieCardNumberText(20))
    }
}
