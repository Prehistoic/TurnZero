package com.turnzero.core.tools.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ToolRegistryTest {

    private val testGameA = TestGame("gameA", "Game A")
    private val testGameB = TestGame("gameB", "Game B")
    private val universalTool = TestTool("tool1", "Universal", setOf("*"))
    private val gameATool = TestTool("tool2", "Game A Only", setOf("gameA"))
    private val gameBTool = TestTool("tool3", "Game B Only", setOf("gameB"))

    private val registry = ToolRegistry(
        games = setOf(testGameA, testGameB),
        tools = setOf(universalTool, gameATool, gameBTool)
    )

    @Test
    fun `getGames returns all registered games`() {
        val games = registry.getGames()
        assertEquals(2, games.size)
    }

    @Test
    fun `getToolsForGame includes universal tools and game-specific tools`() {
        val tools = registry.getToolsForGame("gameA")
        assertEquals(2, tools.size)
        assertTrue(tools.any { it.id == "tool1" })
        assertTrue(tools.any { it.id == "tool2" })
    }

    @Test
    fun `getToolsForGame returns only universal tools for unknown game`() {
        val tools = registry.getToolsForGame("unknown")
        assertEquals(1, tools.size)
        assertEquals("tool1", tools[0].id)
    }

    @Test
    fun `getToolsForGame returns empty list when no tools match`() {
        val registry = ToolRegistry(games = setOf(testGameA), tools = setOf(gameBTool))
        val tools = registry.getToolsForGame("gameA")
        assertTrue(tools.isEmpty())
    }

    @Test
    fun `getToolsByIds returns matching tools`() {
        val tools = registry.getToolsByIds(setOf("tool1", "tool3"))
        assertEquals(2, tools.size)
        assertTrue(tools.any { it.id == "tool1" })
        assertTrue(tools.any { it.id == "tool3" })
    }

    @Test
    fun `getToolsByIds returns empty list for no matches`() {
        val tools = registry.getToolsByIds(setOf("nonexistent"))
        assertTrue(tools.isEmpty())
    }

    @Test
    fun `getToolById returns tool when found`() {
        val tool = registry.getToolById("tool1")
        assertNotNull(tool)
        assertEquals("Universal", tool?.name)
    }

    @Test
    fun `getToolById returns null when not found`() {
        val tool = registry.getToolById("nonexistent")
        assertNull(tool)
    }

    @Test
    fun `getGameById returns game when found`() {
        val game = registry.getGameById("gameA")
        assertNotNull(game)
        assertEquals("Game A", game?.name)
    }

    @Test
    fun `getGameById returns null when not found`() {
        val game = registry.getGameById("nonexistent")
        assertNull(game)
    }

    @Test
    fun `getFavoriteEntries resolves valid keys`() {
        val entries = registry.getFavoriteEntries(setOf("gameA:tool2"))
        assertEquals(1, entries.size)
        assertEquals("gameA", entries[0].gameId)
        assertEquals("Game A", entries[0].gameName)
        assertEquals("tool2", entries[0].tool.id)
    }

    @Test
    fun `getFavoriteEntries ignores malformed keys`() {
        val entries = registry.getFavoriteEntries(setOf("bad-key", "also:bad:format"))
        assertTrue(entries.isEmpty())
    }

    @Test
    fun `getFavoriteEntries ignores keys with missing game`() {
        val entries = registry.getFavoriteEntries(setOf("nonexistent:tool1"))
        assertTrue(entries.isEmpty())
    }

    @Test
    fun `getFavoriteEntries ignores keys with missing tool`() {
        val entries = registry.getFavoriteEntries(setOf("gameA:nonexistent"))
        assertTrue(entries.isEmpty())
    }
}

private class TestGame(
    override val id: String,
    override val name: String
) : Game {
    override val description = "Test game"
    override val toolIds = emptyList<String>()
}

private class TestTool(
    override val id: String,
    override val name: String,
    override val supportedGames: Set<String>
) : Tool {
    override val icon: ImageVector
        get() = ImageVector.Builder(
            name = "test",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).build()

    @Composable
    override fun Screen(onBack: () -> Unit) {}
}
