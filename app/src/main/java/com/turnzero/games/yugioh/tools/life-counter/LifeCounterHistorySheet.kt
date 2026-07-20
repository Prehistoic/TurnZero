package com.turnzero.games.yugioh.tools.lifecounter

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.turnzero.core.ui.components.TZHistorySheet

@Composable
fun LifeCounterHistorySheet(
    history: List<LpChange>,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    TZHistorySheet(
        title = "LP History",
        emptyMessage = "No changes yet",
        items = history,
        onClear = onClear,
        onDismiss = onDismiss,
        itemContent = { change, index ->
            LpChangeEntry(change = change, index = index)
        }
    )
}

@Composable
private fun LpChangeEntry(change: LpChange, index: Int) {
    val playerLabel = when (change.player) {
        Player.PLAYER_1 -> "D1"
        Player.PLAYER_2 -> "D2"
    }
    val opSymbol = when (change.operation) {
        Operation.ADD -> "+"
        Operation.SUBTRACT -> "\u2212"
        Operation.DIVIDE -> "\u00f72"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)) {
                    append("#$index  $playerLabel  ${change.previousLp}  ")
                }
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)) {
                    append("$opSymbol${change.delta}")
                }
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)) {
                    append("  =  ")
                }
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                    append("${change.newLp}")
                }
            },
            style = MaterialTheme.typography.labelMedium
        )
    }
}
