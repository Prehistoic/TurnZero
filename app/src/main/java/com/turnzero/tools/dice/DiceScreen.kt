package com.turnzero.tools.dice

import androidx.compose.foundation.clickable
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
    val dieSize = when {
        uiState.dice.size <= 4 -> 80.dp
        uiState.dice.size <= 8 -> 64.dp
        uiState.dice.size <= 12 -> 56.dp
        uiState.dice.size <= 16 -> 48.dp
        else -> 40.dp
    }

    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clickable(enabled = canRoll) { onRoll() },
            contentAlignment = Alignment.Center
        ) {
            DiceAnimation(
                dice = uiState.dice,
                results = uiState.results,
                isRolling = uiState.isRolling,
                onAnimationComplete = onAnimationComplete,
                dieSize = dieSize
            )
        }

        if (uiState.total != null && !uiState.isRolling) {
            Text(
                text = "Total: ${uiState.total}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 16.dp)
            )
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
