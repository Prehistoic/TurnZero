package com.turnzero.games.yugioh

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifeCounterHistorySheet(
    history: List<LpChange>,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LP History",
                    style = MaterialTheme.typography.titleLarge
                )
                TextButton(onClick = onClear) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Clear")
                }
            }

            Spacer(Modifier.height(8.dp))

            if (history.isEmpty()) {
                Text(
                    text = "No changes yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn {
                    itemsIndexed(history) { index, change ->
                        LpChangeEntry(change = change, index = history.size - index)
                        if (index < history.lastIndex) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun LpChangeEntry(change: LpChange, index: Int) {
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val timeStr = timeFormat.format(Date(change.timestamp))
    val playerLabel = when (change.player) {
        Player.PLAYER_1 -> "D1"
        Player.PLAYER_2 -> "D2"
    }
    val opSymbol = when (change.operation) {
        Operation.ADD -> "+"
        Operation.SUBTRACT -> "\u2212"
        Operation.DIVIDE -> "\u00f72"
    }
    val isPositive = change.operation == Operation.ADD

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#$index  $playerLabel",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.5f)
        )
        Text(
            text = "$opSymbol ${change.delta}",
            style = MaterialTheme.typography.labelMedium,
            color = if (isPositive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(0.5f)
        )
        Text(
            text = "${change.newLp}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = timeStr,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.6f)
        )
    }
}
