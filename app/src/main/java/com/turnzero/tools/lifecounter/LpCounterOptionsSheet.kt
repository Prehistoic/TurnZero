package com.turnzero.tools.lifecounter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LpCounterOptionsSheet(
    uiState: LpCounterUiState,
    onPlayerCountChange: (Int) -> Unit,
    onGlobalStartingLpChange: (Int) -> Unit,
    onResetAll: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showCustomInput by remember { mutableStateOf(false) }
    var customLpText by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "LP Counter Options",
                style = MaterialTheme.typography.titleLarge,
            )

            Column {
                Text(
                    text = "Players",
                    style = MaterialTheme.typography.labelLarge,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (count in 2..4) {
                        FilterChip(
                            selected = uiState.playerCount == count,
                            onClick = { onPlayerCountChange(count) },
                            label = { Text("$count") },
                        )
                    }
                }
            }

            HorizontalDivider()

            Column {
                Text(
                    text = "Starting Life Points",
                    style = MaterialTheme.typography.labelLarge,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Defaults.lpPresets.forEach { preset ->
                        FilterChip(
                            selected = uiState.globalStartingLp == preset,
                            onClick = {
                                onGlobalStartingLpChange(preset)
                                showCustomInput = false
                            },
                            label = { Text("$preset") },
                        )
                    }
                    if (!showCustomInput) {
                        TextButton(onClick = { showCustomInput = true }) {
                            Text("Custom...")
                        }
                    }
                }
                if (showCustomInput) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customLpText,
                        onValueChange = { newValue ->
                            customLpText = newValue.filter { it.isDigit() }
                            val lp = customLpText.toIntOrNull()
                            if (lp != null && lp > 0) {
                                onGlobalStartingLpChange(lp)
                            }
                        },
                        label = { Text("Custom LP") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            HorizontalDivider()

            OutlinedButton(
                onClick = onResetAll,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text("Reset All Players")
            }
        }
    }
}
