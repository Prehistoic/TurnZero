package com.turnzero.games.yugioh.tools.rulingssearcher

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turnzero.core.tools.api.Tool
import com.turnzero.games.yugioh.tools.rulings.RulingsOptionsSheet
import com.turnzero.games.yugioh.tools.rulings.RulingsScreen
import com.turnzero.games.yugioh.tools.rulings.RulingsTool
import com.turnzero.games.yugioh.tools.rulings.RulingsViewModel
import androidx.hilt.navigation.compose.hiltViewModel

class YuGiOhRulingsSearcher : Tool {
    override val id = "ygo-rulings-searcher"
    override val name = "Rulings"
    override val icon = Icons.Default.Search
    override val supportedGames = setOf("yugioh")

    @Composable
    override fun Screen(onBack: () -> Unit) {
        RulingsScreen(onBack = onBack)
    }

    @Composable
    override fun OptionsSheet(onDismiss: () -> Unit) {
        val viewModel: RulingsViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()

        RulingsOptionsSheet(
            allFormatsWithStatus = viewModel.allFormatsWithStatus,
            defaultFormat = state.defaultFormat,
            onSetDefaultFormat = { viewModel.setDefaultFormat(it) },
            onToggleFormat = { viewModel.toggleFormatEnabled(it) },
            onDismiss = onDismiss,
        )
    }

    @Suppress("ComposableNaming")
    @Composable
    override fun TopBarActions(): @Composable RowScope.() -> Unit {
        val impl = RulingsTool()
        return impl.TopBarActions()
    }
}
