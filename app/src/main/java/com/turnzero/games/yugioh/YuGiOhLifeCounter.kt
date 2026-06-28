package com.turnzero.games.yugioh

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.turnzero.core.tools.api.Tool

class YuGiOhLifeCounter : Tool {
    override val id = "ygo-life-counter"
    override val name = "LP Counter"
    override val icon = Icons.Default.Favorite
    override val supportedGames = setOf("yugioh")

    @Composable
    override fun Screen(onBack: () -> Unit) {
        Text(
            text = "Life Point Counter — Coming Soon",
            style = MaterialTheme.typography.titleMedium
        )
    }
}
