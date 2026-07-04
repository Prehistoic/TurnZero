package com.turnzero.games.yugioh.tools.lifecounter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifeCounterHistorySheet(
    history: List<LpChange>,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    val nestedScrollConnection = remember {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {}
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.6f),
        sheetState = sheetState
    ) {
        val maxHeight = LocalConfiguration.current.screenHeightDp.dp * 0.7f
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .heightIn(max = maxHeight)
        ) {
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
                LazyColumn(
                    modifier = Modifier.nestedScroll(nestedScrollConnection)
                ) {
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
