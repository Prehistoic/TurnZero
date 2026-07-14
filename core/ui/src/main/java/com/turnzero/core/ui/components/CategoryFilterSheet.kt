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
import com.turnzero.core.tools.api.GameCategory

data class CategoryFilterOption(
    val category: GameCategory?,
    val label: String,
    val description: String
)

val categoryFilterOptions = listOf(
    CategoryFilterOption(null, "All", "Show all games"),
    CategoryFilterOption(GameCategory.TCG, "Trading Card Games", "TCGs only"),
    CategoryFilterOption(GameCategory.BOARD_GAME, "Board Games", "Board games only")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFilterSheet(
    selectedCategory: GameCategory?,
    onCategorySelected: (GameCategory?) -> Unit,
    onDismiss: () -> Unit
) {
    var selected by remember(selectedCategory) { mutableStateOf(selectedCategory) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Filter by Category",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            categoryFilterOptions.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selected == option.category,
                            onClick = { selected = option.category },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selected == option.category,
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
                onClick = { onCategorySelected(selected) },
                modifier = Modifier.fillMaxWidth(),
                enabled = selected != selectedCategory
            ) {
                Text("Apply")
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
