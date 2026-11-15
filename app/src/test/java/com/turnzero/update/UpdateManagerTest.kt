package com.turnzero.update

import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateManagerTest {

    @Test
    fun `download percent is zero before any bytes arrive`() {
        assertEquals(0, downloadPercent(bytesRead = 0, totalBytes = 1000))
    }

    @Test
    fun `download percent rounds down while in progress`() {
        assertEquals(42, downloadPercent(bytesRead = 425, totalBytes = 1000))
    }

    @Test
    fun `download percent reaches one hundred when complete`() {
        assertEquals(100, downloadPercent(bytesRead = 1000, totalBytes = 1000))
    }

    @Test
    fun `download percent is unknown when the size is unknown`() {
        assertEquals(0, downloadPercent(bytesRead = 500, totalBytes = -1))
    }
}
