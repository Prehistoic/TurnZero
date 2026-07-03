package com.turnzero.tools.coin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinOptionsSheet(
    uiState: CoinUiState,
    onSetCoinCount: (Int) -> Unit,
    onLoadPreset: (CoinPreset) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.presets.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    uiState.presets.forEach { preset ->
                        FilterChip(
                            selected = false,
                            onClick = { onLoadPreset(preset) },
                            label = { Text(preset.name, fontSize = 14.sp) },
                            modifier = Modifier.padding(horizontal = 4.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                labelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = { onSetCoinCount(uiState.coinCount - 1) },
                    enabled = uiState.coinCount > 1
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease coin count")
                }

                Text(
                    text = "${uiState.coinCount} coin${if (uiState.coinCount > 1) "s" else ""}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(100.dp)
                )

                IconButton(
                    onClick = { onSetCoinCount(uiState.coinCount + 1) },
                    enabled = uiState.coinCount < 8
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase coin count")
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
