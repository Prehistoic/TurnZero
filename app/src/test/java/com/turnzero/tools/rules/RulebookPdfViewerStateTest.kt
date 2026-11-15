package com.turnzero.tools.rules

import org.junit.Assert.assertEquals
import org.junit.Test

class RulebookPdfViewerStateTest {

    @Test
    fun `missing cached file reports failure`() {
        assertEquals(
            RulebookLoadState.FAILED,
            rulebookLoadState(cachedPath = null, pageCount = 0, renderFailed = false),
        )
    }

    @Test
    fun `cached file without pages is still loading`() {
        assertEquals(
            RulebookLoadState.LOADING,
            rulebookLoadState(cachedPath = "/tmp/book.pdf", pageCount = 0, renderFailed = false),
        )
    }

    @Test
    fun `renderer failure reports failure`() {
        assertEquals(
            RulebookLoadState.FAILED,
            rulebookLoadState(cachedPath = "/tmp/book.pdf", pageCount = 0, renderFailed = true),
        )
    }

    @Test
    fun `opened renderer with pages is ready`() {
        assertEquals(
            RulebookLoadState.READY,
            rulebookLoadState(cachedPath = "/tmp/book.pdf", pageCount = 12, renderFailed = false),
        )
    }
}
