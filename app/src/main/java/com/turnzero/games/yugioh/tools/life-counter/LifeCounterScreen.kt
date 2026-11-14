package com.turnzero.games.yugioh.tools.lifecounter

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.delay
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

/** Test-observable rendering of the LP color direction (-1 red, 0 default, +1 green). */
internal val LpColorDirectionKey = SemanticsPropertyKey<Int>("LpColorDirection")

private fun Modifier.lpColorDirection(value: Int): Modifier =
    semantics { this[LpColorDirectionKey] = value }

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
    val activity = context as? Activity

    var showResetDialog by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val window = activity?.window
        val originalOrientation = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val activityObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
                Lifecycle.Event.ON_PAUSE -> {
                    window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
                else -> {}
            }
        }

        (activity as? LifecycleOwner)?.lifecycle?.addObserver(activityObserver)

        onDispose {
            (activity as? LifecycleOwner)?.lifecycle?.removeObserver(activityObserver)
            onPauseTimer()
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            if (originalOrientation != null) {
                activity?.requestedOrientation = originalOrientation
            } else {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    LaunchedEffect(uiState.flashZero) {
        if (uiState.flashZero) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    vibrateApi31(context)
                } else {
                    @Suppress("DEPRECATION")
                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                    vibrator?.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
                }
            } catch (_: Exception) {}
            onConsumedFlashZero()
        }
    }

    Box(modifier = modifier.fillMaxSize().displayCutoutPadding().navigationBarsPadding()) {
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
                    lpColorChange = uiState.lpColorChange,
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
                    onReset = { showResetDialog = true },
                    onUndo = onUndoLastChange,
                    modifier = Modifier.weight(1f)
                )

                PlayerLpPanel(
                    player = Player.PLAYER_2,
                    lp = uiState.player2Lp,
                    lpColorChange = uiState.lpColorChange,
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

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset the Duel") },
            text = { Text("Reset ?") },
            confirmButton = {
                TextButton(onClick = {
                    showResetDialog = false
                    onResetAll()
                }) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PlayerLpPanel(
    player: Player,
    lp: Int,
    lpColorChange: LpColorChange?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val label = when (player) {
        Player.PLAYER_1 -> "Duellist 1"
        Player.PLAYER_2 -> "Duellist 2"
    }
    val defaultColor = MaterialTheme.colorScheme.primary

    val change = lpColorChange?.takeIf { it.player == player }
    var handledChangeId by remember { mutableStateOf(change?.id) }
    var lpChangeDirection by remember { mutableIntStateOf(0) }

    // A change is colored only when it arrived while this panel was alive and
    // has not been animated yet. Reset, undo and option changes never carry a
    // color change, so they snap to the new LP without coloring it.
    val isNewChange = change != null && change.id != handledChangeId
    val duration = if (isNewChange) {
        (abs(change!!.newLp - change.previousLp) / 5).coerceIn(1000, 2000)
    } else {
        0
    }
    val displayLp by animateIntAsState(
        targetValue = lp,
        animationSpec = tween(durationMillis = duration),
        label = "lp"
    )

    LaunchedEffect(change?.id) {
        val currentChange = change
        if (currentChange != null && currentChange.id != handledChangeId) {
            lpChangeDirection = currentChange.newLp.compareTo(currentChange.previousLp)
            if (duration > 0) {
                delay(duration.toLong())
                lpChangeDirection = 0
            }
            handledChangeId = currentChange.id
        } else {
            lpChangeDirection = 0
        }
    }

    val color = when {
        lpChangeDirection > 0 -> Color(0xFF4CAF50)
        lpChangeDirection < 0 -> Color(0xFFF44336)
        else -> defaultColor
    }

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
                fontSize = 72.sp,
                modifier = Modifier
                    .testTag(if (player == Player.PLAYER_1) "player-1-lp" else "player-2-lp")
                    .lpColorDirection(lpChangeDirection)
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
            val timeText = remember(displaySeconds) {
                val hours = displaySeconds / 3600
                val minutes = (displaySeconds % 3600) / 60
                val seconds = displaySeconds % 60
                String.format(java.util.Locale.ROOT, "%02d:%02d:%02d", hours, minutes, seconds)
            }

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
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text("Reset")
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
private fun vibrateApi31(context: Context) {
    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
    vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
}

data class LifeCounterCalcActions(
    val onDigit: (Int) -> Unit,
    val onSetOperation: (Operation) -> Unit,
    val onClear: () -> Unit,
    val onDivideBy2: () -> Unit,
    val onAddZeros: (Int) -> Unit,
    val onApply: () -> Unit
)
