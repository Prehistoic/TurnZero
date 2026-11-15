package com.turnzero.core.tools.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ToolDefaultsTest {

    private val tool = object : Tool {
        override val id = "t"
        override val name = "T"
        override val icon: ImageVector
            get() = ImageVector.Builder(
                name = "test",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).build()
        override val supportedGames = setOf("*")

        @Composable
        override fun Screen(onBack: () -> Unit) {}
    }

    @Test
    fun `providesOwnTopBar defaults to false`() {
        assertFalse(tool.providesOwnTopBar)
    }

    @Test
    fun `showBottomBar defaults to true`() {
        assertTrue(tool.showBottomBar)
    }
}
