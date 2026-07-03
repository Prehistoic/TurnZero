package com.turnzero

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.turnzero.core.data.dataStore
import com.turnzero.core.tools.api.Game
import com.turnzero.core.tools.api.Tool
import com.turnzero.core.tools.api.ToolRegistry
import com.turnzero.games.yugioh.YuGiOhGame
import com.turnzero.games.yugioh.YuGiOhLifeCounter
import com.turnzero.games.yugioh.YuGiOhRulingsSearcher
import com.turnzero.tools.coin.CoinTool
import com.turnzero.tools.dice.DiceTool
import com.turnzero.tools.rules.RulesTool

object ServiceLocator {
    val tools: Set<Tool> = setOf(
        DiceTool(),
        CoinTool(),
        RulesTool(),
        YuGiOhLifeCounter(),
        YuGiOhRulingsSearcher()
    )

    val games: Set<Game> = setOf(
        YuGiOhGame
    )

    val toolRegistry = ToolRegistry(games, tools)

    lateinit var dataStore: DataStore<Preferences>
        private set

    fun init(context: Context) {
        dataStore = context.dataStore
    }
}
