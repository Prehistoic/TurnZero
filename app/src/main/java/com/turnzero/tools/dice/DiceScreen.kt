package com.turnzero.tools.dice

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
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
    showHistory: Boolean,
    onToggleHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Dice pool
            DiceAnimation(
                dice = uiState.dice,
                results = uiState.results,
                isRolling = uiState.isRolling,
                onAnimationComplete = onAnimationComplete,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Total
            if (uiState.total != null && !uiState.isRolling) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Total: ${uiState.total}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Roll button
            Button(
                onClick = onRoll,
                enabled = uiState.dice.isNotEmpty() && !uiState.isRolling,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp)
                    .height(56.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text(
                    text = "ROLL!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // History toggle FAB
        FloatingActionButton(
            onClick = onToggleHistory,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 0.dp, end = 0.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Icon(Icons.Default.History, contentDescription = "History")
        }
    }

    // History Sheet
    if (showHistory) {
        DiceHistorySheet(
            history = uiState.history,
            onClear = onClearHistory,
            onDismiss = onToggleHistory
        )
    }
}
