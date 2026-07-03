package com.turnzero.tools.rules

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
    override fun TopBarActions(): @Composable RowScope.() -> Unit {
        val viewModel: RulesViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val hasPdf = uiState.currentConfig?.pdfUrl != null

        return {
            if (hasPdf) {
                IconButton(onClick = { viewModel.togglePdf() }) {
                    Icon(
                        Icons.Default.PictureAsPdf,
                        contentDescription = "Official Rules PDF",
                    )
                }
            }
        }
    }

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
            onFilterCategory = viewModel::filterCategory,
            onTogglePdf = viewModel::togglePdf,
        )
    }
}
