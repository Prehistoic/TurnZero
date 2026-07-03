package com.turnzero.tools.coin

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
fun CoinHistorySheet(
    history: List<CoinFlip>,
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
                    text = "History",
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
                    text = "No flips yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn {
                    itemsIndexed(history) { index, flip ->
                        FlipEntry(flip = flip, index = history.size - index)
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
private fun FlipEntry(flip: CoinFlip, index: Int) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeStr = timeFormat.format(Date(flip.timestamp))
    val summary = "${flip.coinCount} coin${if (flip.coinCount > 1) "s" else ""}"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#$index  $summary",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        val resultStr = buildString {
            if (flip.headsCount > 0) {
                if (flip.coinCount == 1) append("H")
                else append("${flip.headsCount}H")
            }
            if (flip.headsCount > 0 && flip.tailsCount > 0) {
                append(" \u00B7 ")
            }
            if (flip.tailsCount > 0) {
                if (flip.coinCount == 1) append("T")
                else append("${flip.tailsCount}T")
            }
        }
        Text(
            text = resultStr,
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
