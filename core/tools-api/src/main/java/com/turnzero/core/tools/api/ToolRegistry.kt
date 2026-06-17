package com.turnzero.core.tools.api

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToolRegistry @Inject constructor(
    private val games: Set<@JvmSuppressWildcards Game>,
    private val tools: Set<@JvmSuppressWildcards Tool>
) {
    fun getGames(): List<Game> = games.toList()

    fun getToolsForGame(gameId: String): List<Tool> =
        tools.filter { tool ->
            tool.supportedGames.contains("*") || tool.supportedGames.contains(gameId)
        }
}
