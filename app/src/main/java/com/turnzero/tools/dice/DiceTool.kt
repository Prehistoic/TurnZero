package com.turnzero.tools.dice

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.runtime.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turnzero.core.tools.api.Tool

class DiceTool : Tool {
    override val id = "dice"
    override val name = "Dice"
    override val icon = Icons.Default.Casino
    override val supportedGames = setOf("*")

    @Composable
    override fun Screen(onBack: () -> Unit) {
        val viewModel: DiceViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        var showHistory by remember { mutableStateOf(false) }

        DiceScreen(
            uiState = uiState,
            onChangeDieCount = viewModel::changeDieCount,
            onSetSelectedColor = viewModel::setSelectedColor,
            onRoll = viewModel::roll,
            onAnimationComplete = viewModel::animationComplete,
            onLoadPreset = viewModel::loadPreset,
            onClearHistory = viewModel::clearHistory,
            showHistory = showHistory,
            onToggleHistory = { showHistory = !showHistory }
        )
    }

    @Composable
    override fun OptionsSheet(onDismiss: () -> Unit) {
        val viewModel: DiceViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        DiceOptionsSheet(
            uiState = uiState,
            onChangeDieCount = viewModel::changeDieCount,
            onSetSelectedColor = viewModel::setSelectedColor,
            onLoadPreset = viewModel::loadPreset,
            onDismiss = onDismiss
        )
    }
}
