package com.turnzero.tools.dice

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DiceScreen(
    uiState: DiceUiState,
    onChangeDieCount: (Int, DiceColor, Int) -> Unit,
    onSetSelectedColor: (DiceColor) -> Unit,
    onRoll: () -> Unit,
    onAnimationComplete: () -> Unit,
    onLoadPreset: (DicePreset) -> Unit,
    onClearHistory: () -> Unit,
    onToggleHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canRoll = uiState.dice.isNotEmpty() && !uiState.isRolling

    var tapCount by remember { mutableIntStateOf(0) }

    Column(modifier = modifier.fillMaxSize()) {
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    enabled = canRoll
                ) { tapCount++ },
            contentAlignment = Alignment.Center
        ) {
            val columns = uiState.dice.chunked(4).maxOfOrNull { it.size }?.coerceAtLeast(1) ?: 1
            val availableWidth = maxWidth * 0.8f
            val gaps = 8.dp * (columns - 1)
            val dieSize = (availableWidth - gaps) / columns

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DiceAnimation(
                    dice = uiState.dice,
                    results = uiState.results,
                    isRolling = uiState.isRolling,
                    onAnimationComplete = onAnimationComplete,
                    onRoll = onRoll,
                    dieSize = dieSize,
                    triggerScale = tapCount
                )
                if (uiState.total != null && !uiState.isRolling) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Total: ${uiState.total}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (uiState.showHistory) {
        DiceHistorySheet(
            history = uiState.history,
            onClear = onClearHistory,
            onDismiss = onToggleHistory
        )
    }
}
