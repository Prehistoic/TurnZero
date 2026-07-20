package com.turnzero.core.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayoutBottomSheet(
    currentLayout: LayoutMode,
    onLayoutSelected: (LayoutMode) -> Unit,
    onDismiss: () -> Unit
) {
    val options = LayoutMode.entries.map { mode ->
        OptionItem(
            value = mode,
            label = when (mode) {
                LayoutMode.GRID -> "Grid"
                LayoutMode.LIST -> "List"
            },
            description = when (mode) {
                LayoutMode.GRID -> "Tiles with details"
                LayoutMode.LIST -> "Full-width rows"
            }
        )
    }
    OptionSelectorSheet(
        title = "Display Layout",
        options = options,
        selected = currentLayout,
        onSelected = onLayoutSelected,
        onDismiss = onDismiss
    )
}
