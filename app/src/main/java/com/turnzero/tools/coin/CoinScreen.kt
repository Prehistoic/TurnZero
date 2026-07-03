package com.turnzero.tools.coin

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
fun CoinScreen(
    uiState: CoinUiState,
    onSetCoinCount: (Int) -> Unit,
    onFlip: () -> Unit,
    onFlipAnimationComplete: () -> Unit,
    onLoadPreset: (CoinPreset) -> Unit,
    onClearHistory: () -> Unit,
    onToggleHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canFlip = !uiState.isFlipping

    var tapCount by remember { mutableIntStateOf(0) }

    Column(modifier = modifier.fillMaxSize()) {
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    enabled = canFlip
                ) { tapCount++ },
            contentAlignment = Alignment.Center
        ) {
            val columns = uiState.coinCount.coerceAtMost(4)
            val widthFraction = when (columns) {
                1 -> 0.35f
                2 -> 0.60f
                else -> 0.8f
            }
            val availableWidth = maxWidth * widthFraction
            val gaps = 8.dp * (columns - 1)
            val coinSize = (availableWidth - gaps) / columns

            CoinAnimation(
                coinCount = uiState.coinCount,
                results = uiState.results,
                isFlipping = uiState.isFlipping,
                onAnimationComplete = onFlipAnimationComplete,
                onFlip = onFlip,
                coinSize = coinSize,
                triggerScale = tapCount
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.25f),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.results.isNotEmpty() && !uiState.isFlipping) {
                val resultText = buildString {
                    if (uiState.headsCount > 0) {
                        if (uiState.coinCount == 1) append("Heads")
                        else append("${uiState.headsCount} Heads")
                    }
                    if (uiState.headsCount > 0 && uiState.tailsCount > 0) {
                        append(" \u00B7 ")
                    }
                    if (uiState.tailsCount > 0) {
                        if (uiState.coinCount == 1) append("Tails")
                        else append("${uiState.tailsCount} Tails")
                    }
                }
                Text(
                    text = resultText,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (uiState.showHistory) {
        CoinHistorySheet(
            history = uiState.history,
            onClear = onClearHistory,
            onDismiss = onToggleHistory
        )
    }
}
