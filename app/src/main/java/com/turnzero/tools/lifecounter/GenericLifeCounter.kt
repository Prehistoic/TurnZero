package com.turnzero.tools.lifecounter

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turnzero.core.tools.api.Tool
import com.turnzero.navigation.LocalToolSheetActions

val LocalGameId = staticCompositionLocalOf<String> { "mtg" }

class GenericLifeCounter : Tool {

    override val id = "lp-counter"
    override val name = "LP Counter"
    override val icon: ImageVector = Icons.Default.Favorite
    override val supportedGames = setOf("mtg", "swu", "fab")
    override val showBottomBar = true

    @Composable
    override fun Screen(onBack: () -> Unit) {
        val gameId = LocalGameId.current
        val viewModel: LpCounterViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        LaunchedEffect(gameId) {
            viewModel.initialize(gameId)
        }

        LpCounterScreen(
            uiState = uiState,
            onIncrement = viewModel::incrementPlayer,
            onDecrement = viewModel::decrementPlayer,
            onRelease = viewModel::releasePlayer,
            onResetPlayer = viewModel::resetPlayer,
            modifier = Modifier.padding(8.dp),
        )

        if (uiState.showHistory) {
            LpCounterHistorySheet(
                history = uiState.changeHistory,
                onClear = viewModel::clearHistory,
                onDismiss = viewModel::dismissHistory,
            )
        }

        if (uiState.showOptions) {
            LpCounterOptionsSheet(
                uiState = uiState,
                onPlayerCountChange = viewModel::setPlayerCount,
                onGlobalStartingLpChange = viewModel::setGlobalStartingLp,
                onPlayerStartingLpChange = viewModel::setPlayerStartingLp,
                onResetAll = viewModel::resetAll,
                onDismiss = viewModel::dismissOptions,
            )
        }
    }

    @Composable
    override fun OptionsSheet(onDismiss: () -> Unit) {
        val viewModel: LpCounterViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        LpCounterOptionsSheet(
            uiState = uiState,
            onPlayerCountChange = viewModel::setPlayerCount,
            onGlobalStartingLpChange = viewModel::setGlobalStartingLp,
            onPlayerStartingLpChange = viewModel::setPlayerStartingLp,
            onResetAll = viewModel::resetAll,
            onDismiss = onDismiss,
        )
    }

    @Composable
    override fun TopBarActions(): @Composable RowScope.() -> Unit = {
        val viewModel: LpCounterViewModel = hiltViewModel()
        val sheetActions = LocalToolSheetActions.current

        IconButton(onClick = { viewModel.showHistory(true) }) {
            Icon(Icons.Default.History, contentDescription = "History")
        }

        IconButton(onClick = { sheetActions?.onOpenOptions?.invoke() }) {
            Icon(Icons.Default.Settings, contentDescription = "Options")
        }
    }
}
