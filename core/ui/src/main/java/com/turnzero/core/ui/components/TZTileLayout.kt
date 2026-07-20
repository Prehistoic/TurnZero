package com.turnzero.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun <T> TZTileLayout(
    tileItems: List<T>,
    layoutMode: LayoutMode,
    key: (T) -> Any,
    modifier: Modifier = Modifier,
    gridColumns: Int = 2,
    horizontalSpacing: Dp = 8.dp,
    verticalSpacing: Dp = 8.dp,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    itemContent: @Composable (T) -> Unit,
) {
    when (layoutMode) {
        LayoutMode.LIST -> {
            LazyColumn(
                contentPadding = contentPadding,
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(verticalSpacing),
            ) {
                lazyItems(tileItems, key = key) { item ->
                    itemContent(item)
                }
            }
        }
        else -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(gridColumns),
                contentPadding = contentPadding,
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
                verticalArrangement = Arrangement.spacedBy(verticalSpacing),
            ) {
                gridItems(tileItems, key = key) { item: T ->
                    itemContent(item)
                }
            }
        }
    }
}
