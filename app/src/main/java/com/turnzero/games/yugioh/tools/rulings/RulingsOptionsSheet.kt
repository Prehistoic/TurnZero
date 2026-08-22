package com.turnzero.games.yugioh.tools.rulings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.turnzero.games.yugioh.tools.rulings.data.FormatRulesData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulingsOptionsSheet(
    allFormatsWithStatus: List<Pair<String, Boolean>>,
    defaultFormat: String,
    onSetDefaultFormat: (String) -> Unit,
    onToggleFormat: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val enabledFormats = allFormatsWithStatus
        .filter { it.second }
        .map { it.first }
    var defaultDropdownExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(bottom = 32.dp)) {
            Text(
                text = "Default Format",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
            Text(
                text = "Format shown first when opening the tool",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
            )

            Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                OutlinedButton(onClick = { defaultDropdownExpanded = true }) {
                    Text(FormatRulesData.getLabelWithDate(defaultFormat))
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(
                    expanded = defaultDropdownExpanded,
                    onDismissRequest = { defaultDropdownExpanded = false },
                ) {
                    enabledFormats.forEach { formatKey ->
                        DropdownMenuItem(
                            text = { Text(FormatRulesData.getLabelWithDate(formatKey)) },
                            onClick = {
                                onSetDefaultFormat(formatKey)
                                defaultDropdownExpanded = false
                            },
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            Text(
                text = "Enabled Formats",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
            Text(
                text = "Select which formats appear in the dropdown",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
            ) {
                items(allFormatsWithStatus, key = { it.first }) { (formatKey, isEnabled) ->
                    val isLastEnabled = isEnabled && enabledFormats.size == 1
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = isEnabled,
                            enabled = !isLastEnabled,
                            onCheckedChange = { onToggleFormat(formatKey) },
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = FormatRulesData.getLabelWithDate(formatKey),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        }
    }
}
