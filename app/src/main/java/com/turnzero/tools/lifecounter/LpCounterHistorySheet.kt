package com.turnzero.tools.lifecounter

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.turnzero.core.ui.components.TZHistorySheet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LpCounterHistorySheet(
    history: List<LpChange>,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TZHistorySheet(
        title = "LP History",
        emptyMessage = "No LP changes recorded yet",
        items = history,
        itemContent = { change, _ ->
            LpHistoryItem(change)
        },
        onClear = onClear,
        onDismiss = onDismiss,
        modifier = modifier,
    )
}

@Composable
private fun LpHistoryItem(change: LpChange) {
    val sign = if (change.delta > 0) "+" else ""
    val timeFormat = rememberTimeFormat()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "P${change.playerNumber}: ${change.previousLp} $sign${change.delta} = ${change.newTotal}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = timeFormat.format(Date(change.timestamp)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = "$sign${change.delta}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (change.delta >= 0) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun rememberTimeFormat(): SimpleDateFormat {
    return androidx.compose.runtime.remember {
        SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    }
}
