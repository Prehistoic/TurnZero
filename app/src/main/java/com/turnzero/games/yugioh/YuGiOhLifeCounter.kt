package com.turnzero.games.yugioh

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turnzero.core.tools.api.Tool
import com.turnzero.navigation.LocalToolSheetActions

class YuGiOhLifeCounter : Tool {
    override val id = "ygo-life-counter"
    override val name = "LP Counter"
    override val icon = Icons.Default.Favorite
    override val supportedGames = setOf("yugioh")
    override val showBottomBar = false

    @Composable
    override fun TopBarActions(): @Composable RowScope.() -> Unit = {
        val sheetActions = LocalToolSheetActions.current
        val viewModel: LifeCounterViewModel = hiltViewModel()
        IconButton(onClick = { sheetActions?.onOpenTools?.invoke() }) {
            Icon(Icons.Default.SwapHoriz, contentDescription = "Tools")
        }
        IconButton(onClick = { viewModel.toggleHistory() }) {
            Icon(Icons.Default.History, contentDescription = "History")
        }
        IconButton(onClick = { sheetActions?.onOpenOptions?.invoke() }) {
            Icon(Icons.Default.Settings, contentDescription = "Options")
        }
    }

    @Composable
    override fun Screen(onBack: () -> Unit) {
        val viewModel: LifeCounterViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        val calcActions = LifeCounterCalcActions(
            onDigit = viewModel::inputDigit,
            onSetOperation = viewModel::setOperation,
            onClear = viewModel::clearInput,
            onDivideBy2 = viewModel::divideBy2,
            onAddZeros = viewModel::addZeros,
            onApply = viewModel::applyCalcChange
        )

        LifeCounterScreen(
            uiState = uiState,
            onStartPauseTimer = viewModel::startPauseTimer,
            onPauseTimer = viewModel::pauseTimer,
            onResetAll = viewModel::resetAll,
            onUndoLastChange = viewModel::undoLastChange,
            onOpenCalc = viewModel::openCalc,
            onConsumedFlashZero = viewModel::consumedFlashZero,
            onCloseCalc = viewModel::closeCalc,
            onCalcAction = calcActions
        )

        if (uiState.showHistory) {
            LifeCounterHistorySheet(
                history = uiState.history,
                onClear = viewModel::clearHistory,
                onDismiss = { viewModel.toggleHistory() }
            )
        }
    }

    @Composable
    override fun OptionsSheet(onDismiss: () -> Unit) {
        val viewModel: LifeCounterViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        LifeCounterOptionsSheet(
            timerMode = uiState.timerMode,
            timerStartValueMinutes = uiState.timerStartValueMinutes,
            startingLp = uiState.startingLp,
            onSetTimerMode = viewModel::setTimerMode,
            onSetTimerStartValue = viewModel::setTimerStartValue,
            onSetStartingLp = viewModel::setStartingLp,
            onDismiss = onDismiss
        )
    }
}
