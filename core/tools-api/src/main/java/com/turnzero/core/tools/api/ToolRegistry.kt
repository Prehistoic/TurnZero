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
}
