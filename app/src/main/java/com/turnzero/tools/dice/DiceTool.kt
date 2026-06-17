package com.turnzero.tools.dice

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.turnzero.core.tools.api.Tool
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiceTool @Inject constructor() : Tool {
    override val id = "dice"
    override val name = "Dice Roller"
    override val icon = Icons.Default.Casino
    override val supportedGames = setOf("*")

    @Composable
    override fun Screen(onBack: () -> Unit) {
        Text(
            text = "Dice Roller — Coming Soon",
            style = MaterialTheme.typography.titleMedium
        )
    }
}
