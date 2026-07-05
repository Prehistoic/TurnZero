package com.turnzero.games.yugioh.tools.rulingssearcher

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import com.turnzero.core.tools.api.Tool
import com.turnzero.games.yugioh.tools.rulings.RulingsScreen
import com.turnzero.games.yugioh.tools.rulings.RulingsTool

class YuGiOhRulingsSearcher : Tool {
    override val id = "ygo-rulings-searcher"
    override val name = "Rulings"
    override val icon = Icons.Default.Search
    override val supportedGames = setOf("yugioh")

    @Composable
    override fun Screen(onBack: () -> Unit) {
        RulingsScreen(onBack = onBack)
    }

    @Suppress("ComposableNaming")
    @Composable
    override fun TopBarActions(): @Composable RowScope.() -> Unit {
        val impl = RulingsTool()
        return impl.TopBarActions()
    }
}
