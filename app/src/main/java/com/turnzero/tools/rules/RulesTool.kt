package com.turnzero.tools.rules

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turnzero.core.tools.api.Tool

class RulesTool : Tool {
    override val id = "rules"
    override val name = "Rules"
    override val icon: ImageVector get() = Icons.AutoMirrored.Filled.MenuBook
    override val supportedGames = setOf("*")

    @Composable
    override fun Screen(onBack: () -> Unit) {
        val viewModel: RulesViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        RulesScreen(
            uiState = uiState,
            onSelectRuleset = viewModel::selectRuleset,
            onSelectTab = viewModel::selectTab,
            onPageChanged = viewModel::onPageChanged,
            onToggleStep = viewModel::toggleStep,
            onToggleKeyword = viewModel::toggleKeyword,
            onSearchQueryChange = viewModel::setSearchQuery,
        )
    }
}
