package com.turnzero.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class OptionItem<T>(
    val value: T,
    val label: String,
    val description: String? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> OptionSelectorSheet(
    title: String,
    options: List<OptionItem<T>>,
    selected: T,
    onSelected: (T) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingSelection by remember { mutableStateOf(selected) }

    LaunchedEffect(selected) { pendingSelection = selected }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(16.dp))
        LazyColumn(modifier = modifier.padding(horizontal = 8.dp)) {
            items(options.size) { index ->
                val option = options[index]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = pendingSelection == option.value,
                        onClick = { pendingSelection = option.value }
                    )
                    Spacer(Modifier.width(4.dp))
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (option.description != null) {
                            Text(
                                text = option.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDismiss) { Text("Cancel") }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    onSelected(pendingSelection)
                    onDismiss()
                }
            ) {
                Text("Apply")
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
