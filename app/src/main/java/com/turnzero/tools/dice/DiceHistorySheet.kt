package com.turnzero.tools.dice

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.turnzero.core.ui.components.TZHistorySheet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DiceHistorySheet(
    history: List<DiceRoll>,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    TZHistorySheet(
        title = "History",
        emptyMessage = "No rolls yet",
        items = history,
        onClear = onClear,
        onDismiss = onDismiss,
        itemContent = { roll, index ->
            RollEntry(roll = roll, index = index)
        }
    )
}

@Composable
private fun RollEntry(roll: DiceRoll, index: Int) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeStr = timeFormat.format(Date(roll.timestamp))
    val diceSummary = roll.dice.groupBy { it.faceCount }
        .map { (fc, list) -> "${list.size}d$fc" }
        .joinToString(" + ")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#$index  $diceSummary",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${roll.total}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = timeStr,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
