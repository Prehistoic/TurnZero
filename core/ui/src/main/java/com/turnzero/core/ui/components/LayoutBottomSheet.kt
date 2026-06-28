package com.turnzero.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

data class LayoutOption(
    val mode: LayoutMode,
    val label: String,
    val description: String
)

val layoutOptions = listOf(
    LayoutOption(LayoutMode.GRID, "Grid", "Tiles with details"),
    LayoutOption(LayoutMode.LIST, "List", "Full-width rows")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayoutBottomSheet(
    currentLayout: LayoutMode,
    onLayoutSelected: (LayoutMode) -> Unit,
    onDismiss: () -> Unit
) {
    var selected by remember(currentLayout) { mutableStateOf(currentLayout) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Display Layout",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            layoutOptions.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selected == option.mode,
                            onClick = { selected = option.mode },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selected == option.mode,
                        onClick = null
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = option.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onLayoutSelected(selected) },
                modifier = Modifier.fillMaxWidth(),
                enabled = selected != currentLayout
            ) {
                Text("Apply")
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
