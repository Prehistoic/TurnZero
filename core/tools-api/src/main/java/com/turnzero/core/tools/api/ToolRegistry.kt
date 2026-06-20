package com.turnzero.core.tools.api

class ToolRegistry(
    private val games: Set<@JvmSuppressWildcards Game>,
    private val tools: Set<@JvmSuppressWildcards Tool>
) {
    fun getGames(): List<Game> = games.toList()

    fun getToolsForGame(gameId: String): List<Tool> =
        tools.filter { tool ->
            tool.supportedGames.contains("*") || tool.supportedGames.contains(gameId)
        }

    fun getToolsByIds(ids: Set<String>): List<Tool> =
        tools.filter { it.id in ids }

    fun getToolById(id: String): Tool? =
        tools.firstOrNull { it.id == id }

    fun getGameById(id: String): Game? =
        games.firstOrNull { it.id == id }

    fun getFavoriteEntries(favoriteKeys: Set<String>): List<FavoriteEntry> {
        return favoriteKeys.mapNotNull { key ->
            val parts = key.split(":", limit = 2)
            if (parts.size != 2) return@mapNotNull null
            val gameId = parts[0]
            val toolId = parts[1]
            val game = getGameById(gameId) ?: return@mapNotNull null
            val tool = getToolById(toolId) ?: return@mapNotNull null
            FavoriteEntry(
                key = key,
                gameId = gameId,
                gameName = game.name,
                tool = tool
            )
        }
    }
}

data class FavoriteEntry(
    val key: String,
    val gameId: String,
    val gameName: String,
    val tool: Tool
)
