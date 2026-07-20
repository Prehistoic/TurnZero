package com.turnzero.core.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import com.turnzero.core.tools.api.GameCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFilterSheet(
    selectedCategory: GameCategory?,
    onCategorySelected: (GameCategory?) -> Unit,
    onDismiss: () -> Unit
) {
    val options: List<OptionItem<GameCategory?>> = buildList {
        add(OptionItem(value = null, label = "All", description = "Show all games"))
        for (category in GameCategory.entries) {
            add(
                OptionItem<GameCategory?>(
                    value = category,
                    label = when (category) {
                        GameCategory.TCG -> "Trading Card Games"
                        GameCategory.BOARD_GAME -> "Board Games"
                    },
                    description = when (category) {
                        GameCategory.TCG -> "TCGs only"
                        GameCategory.BOARD_GAME -> "Board games only"
                    }
                )
            )
        }
    }
    OptionSelectorSheet(
        title = "Filter by Category",
        options = options,
        selected = selectedCategory,
        onSelected = onCategorySelected,
        onDismiss = onDismiss
    )
}
