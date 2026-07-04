package com.turnzero.games.yugioh.tools.lifecounter

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

@Composable
fun LifeCounterScreen(
    uiState: LifeCounterUiState,
    onStartPauseTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onResetAll: () -> Unit,
    onUndoLastChange: () -> Unit,
    onOpenCalc: (Player) -> Unit,
    onConsumedFlashZero: () -> Unit,
    onCloseCalc: () -> Unit,
    onCalcAction: LifeCounterCalcActions,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val activity = context as? Activity

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        view.keepScreenOn = true
        onDispose {
            onPauseTimer()
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            view.keepScreenOn = false
        }
    }

    LaunchedEffect(uiState.flashZero) {
        if (uiState.flashZero) {
            try {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vibratorManager?.defaultVibrator
                vibrator?.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
            } catch (_: Exception) {}
            onConsumedFlashZero()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlayerLpPanel(
                    player = Player.PLAYER_1,
                    lp = uiState.player1Lp,
                    onClick = { onOpenCalc(Player.PLAYER_1) },
                    modifier = Modifier.weight(1f)
                )

                CenterPanel(
                    elapsedSeconds = uiState.elapsedSeconds,
                    remainingSeconds = uiState.remainingSeconds,
                    timerMode = uiState.timerMode,
                    isRunning = uiState.isRunning,
                    hasEverStarted = uiState.hasEverStarted,
                    onStartPause = onStartPauseTimer,
                    onReset = onResetAll,
                    onUndo = onUndoLastChange,
                    modifier = Modifier.weight(1f)
                )

                PlayerLpPanel(
                    player = Player.PLAYER_2,
                    lp = uiState.player2Lp,
                    onClick = { onOpenCalc(Player.PLAYER_2) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (uiState.showCalcSheet) {
            LifeCounterCalcScreen(
                player = uiState.activePlayer,
                calcState = uiState.calcState,
                currentLp = if (uiState.activePlayer == Player.PLAYER_1) uiState.player1Lp else uiState.player2Lp,
                onBack = onCloseCalc,
                onAction = onCalcAction
            )
        }
    }
}

@Composable
private fun PlayerLpPanel(
    player: Player,
    lp: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val label = when (player) {
        Player.PLAYER_1 -> "Duellist 1"
        Player.PLAYER_2 -> "Duellist 2"
    }
    val color = MaterialTheme.colorScheme.primary

    val prevLp = remember { mutableIntStateOf(lp) }
    val duration = remember(lp) {
        val delta = abs(lp - prevLp.intValue)
        prevLp.intValue = lp
        (delta / 10).coerceIn(200, 1500)
    }
    val displayLp by animateIntAsState(
        targetValue = lp,
        animationSpec = tween(durationMillis = duration),
        label = "lp"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = displayLp.toString(),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                ),
                fontSize = 72.sp
            )
            Text(
                text = "LP",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CenterPanel(
    elapsedSeconds: Long,
    remainingSeconds: Long,
    timerMode: TimerMode,
    isRunning: Boolean,
    hasEverStarted: Boolean,
    onStartPause: () -> Unit,
    onReset: () -> Unit,
    onUndo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .clickable(onClick = onStartPause)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            val displaySeconds = if (timerMode == TimerMode.COUNT_UP) elapsedSeconds else remainingSeconds
            val hours = displaySeconds / 3600
            val minutes = (displaySeconds % 3600) / 60
            val seconds = displaySeconds % 60
            val timeText = String.format("%02d:%02d:%02d", hours, minutes, seconds)

            val statusLabel = when {
                isRunning -> "Running"
                hasEverStarted -> "Paused"
                else -> "Chrono"
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onUndo,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Cancel")
            }
            Button(
                onClick = onReset,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Reset")
            }
        }
    }
}

data class LifeCounterCalcActions(
    val onDigit: (Int) -> Unit,
    val onSetOperation: (Operation) -> Unit,
    val onClear: () -> Unit,
    val onDivideBy2: () -> Unit,
    val onAddZeros: (Int) -> Unit,
    val onApply: () -> Unit
)
