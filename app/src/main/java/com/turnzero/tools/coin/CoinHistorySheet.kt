package com.turnzero.tools.coin

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
fun CoinHistorySheet(
    history: List<CoinFlip>,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    TZHistorySheet(
        title = "History",
        emptyMessage = "No flips yet",
        items = history,
        onClear = onClear,
        onDismiss = onDismiss,
        itemContent = { flip, index ->
            FlipEntry(flip = flip, index = index)
        }
    )
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
