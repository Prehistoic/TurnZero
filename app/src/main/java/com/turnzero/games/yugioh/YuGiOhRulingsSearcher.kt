package com.turnzero.games.yugioh

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.turnzero.core.tools.api.Tool
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YuGiOhRulingsSearcher @Inject constructor() : Tool {
    override val id = "ygo-rulings-searcher"
    override val name = "Rulings Searcher"
    override val icon = Icons.Default.Search
    override val supportedGames = setOf("yugioh")

    @Composable
    override fun Screen(onBack: () -> Unit) {
        Text(
            text = "Rulings Searcher — Coming Soon",
            style = MaterialTheme.typography.titleMedium
        )
    }
}
