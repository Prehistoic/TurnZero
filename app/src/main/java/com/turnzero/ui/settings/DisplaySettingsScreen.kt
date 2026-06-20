package com.turnzero.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.turnzero.core.ui.components.LayoutMode
import com.turnzero.core.ui.components.TZTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplaySettingsScreen(
    type: String,
    currentLayout: LayoutMode,
    onLayoutSelected: (LayoutMode) -> Unit,
    onBack: () -> Unit
) {
    val label = if (type == "games") "Games Display" else "Tools Display"

    Scaffold(
        topBar = {
            TZTopBar(
                title = label,
                showBackButton = true,
                onBackClick = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = currentLayout == LayoutMode.GRID,
                        onClick = { onLayoutSelected(LayoutMode.GRID) },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = currentLayout == LayoutMode.GRID,
                    onClick = null
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Grid", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Tiles shown in a grid",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = currentLayout == LayoutMode.LIST,
                        onClick = { onLayoutSelected(LayoutMode.LIST) },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = currentLayout == LayoutMode.LIST,
                    onClick = null
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("List", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Full-width rows",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
