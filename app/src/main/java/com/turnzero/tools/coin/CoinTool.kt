package com.turnzero.tools.coin

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Toll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turnzero.core.tools.api.Tool

class CoinTool : Tool {
    override val id = "coin"
    override val name = "Coin"
    override val icon = Icons.Default.Toll
    override val supportedGames = setOf("*")

    @Composable
    override fun TopBarActions(): @Composable RowScope.() -> Unit = {
        val viewModel: CoinViewModel = hiltViewModel()
        IconButton(onClick = { viewModel.toggleHistory() }) {
            Icon(Icons.Default.History, contentDescription = "History")
        }
    }

    @Composable
    override fun Screen(onBack: () -> Unit) {
        val viewModel: CoinViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        CoinScreen(
            uiState = uiState,
            onSetCoinCount = viewModel::setCoinCount,
            onFlip = viewModel::flip,
            onFlipAnimationComplete = viewModel::flipAnimationComplete,
            onLoadPreset = viewModel::loadPreset,
            onClearHistory = viewModel::clearHistory,
            onToggleHistory = viewModel::toggleHistory
        )
    }

    @Composable
    override fun OptionsSheet(onDismiss: () -> Unit) {
        val viewModel: CoinViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        CoinOptionsSheet(
            uiState = uiState,
            onSetCoinCount = viewModel::setCoinCount,
            onLoadPreset = viewModel::loadPreset,
            onDismiss = onDismiss
        )
    }
}
