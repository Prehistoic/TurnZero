package com.turnzero.tools.lifecounter

import android.annotation.SuppressLint
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turnzero.core.tools.api.Tool

val LocalGameId = staticCompositionLocalOf<String> { "mtg" }

@SuppressLint("ContextCastToActivity")
class GenericLifeCounter : Tool {

    override val id = "lp-counter"
    override val name = "LP Counter"
    override val icon: ImageVector = Icons.Default.Favorite
    override val supportedGames = setOf("mtg", "swu", "fab")
    override val showBottomBar = true

    @Composable
    override fun Screen(onBack: () -> Unit) {
        val gameId = LocalGameId.current
        val activity = LocalContext.current as ComponentActivity
        val viewModel: LpCounterViewModel = hiltViewModel(activity)
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        LaunchedEffect(gameId) {
            viewModel.initialize(gameId)
        }

        DisposableEffect(Unit) {
            val window = activity.window
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            val activityObserver = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME ->
                        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    Lifecycle.Event.ON_PAUSE ->
                        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    else -> {}
                }
            }

            activity.lifecycle.addObserver(activityObserver)

            onDispose {
                activity.lifecycle.removeObserver(activityObserver)
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        LpCounterScreen(
            uiState = uiState,
            onIncrement = viewModel::incrementPlayer,
            onDecrement = viewModel::decrementPlayer,
            onResetChangeIndicator = viewModel::resetChangeIndicator,
            onResetPlayer = viewModel::resetPlayer,
            modifier = Modifier.padding(0.dp),
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
                onResetAll = viewModel::resetAll,
                onDismiss = viewModel::dismissOptions,
            )
        }
    }

    @Composable
    override fun OptionsSheet(onDismiss: () -> Unit) {
        val activity = LocalContext.current as ComponentActivity
        val viewModel: LpCounterViewModel = hiltViewModel(activity)
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        LpCounterOptionsSheet(
            uiState = uiState,
            onPlayerCountChange = viewModel::setPlayerCount,
            onGlobalStartingLpChange = viewModel::setGlobalStartingLp,
            onResetAll = viewModel::resetAll,
            onDismiss = onDismiss,
        )
    }

    @Composable
    override fun TopBarActions(): @Composable RowScope.() -> Unit = {
        val activity = LocalContext.current as ComponentActivity
        val viewModel: LpCounterViewModel = hiltViewModel(activity)

        IconButton(onClick = { viewModel.showHistory(true) }) {
            Icon(Icons.Default.History, contentDescription = "History")
        }
    }
}
