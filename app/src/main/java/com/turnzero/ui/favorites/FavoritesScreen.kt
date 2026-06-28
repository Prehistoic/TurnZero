package com.turnzero.ui.favorites

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turnzero.core.tools.api.FavoriteEntry
import com.turnzero.core.ui.components.TZTopBar
import com.turnzero.core.ui.components.TZEmptyState
import com.turnzero.core.ui.components.ToolTile
import com.turnzero.core.ui.components.LayoutMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    favoriteEntries: List<FavoriteEntry> = emptyList(),
    onToolClick: ((gameId: String, toolId: String) -> Unit)? = null,
    highlightedToolId: String? = null,
    onToolLongClick: ((String) -> Unit)? = null,
    onTapOutside: (() -> Unit)? = null,
    layoutMode: LayoutMode = LayoutMode.GRID
) {
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredEntries = if (searchQuery.isEmpty()) {
        favoriteEntries
    } else {
        favoriteEntries.filter {
            it.tool.name.contains(searchQuery, ignoreCase = true) ||
                it.gameName.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TZTopBar(
                title = "Favorites",
                showSearchButton = true,
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                onSearchClick = { isSearchActive = true },
                onSearchQueryChange = { searchQuery = it },
                onSearchClose = {
                    isSearchActive = false
                    searchQuery = ""
                }
            )
        }
    ) { padding ->
        if (favoriteEntries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No favorites yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Long-press any tool to add it here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (isSearchActive && searchQuery.isNotEmpty() && filteredEntries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                TZEmptyState(message = "No result found")
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (highlightedToolId != null && onTapOutside != null) {
                            Modifier.pointerInput(highlightedToolId) {
                                detectTapGestures { onTapOutside() }
                            }
                        } else {
                            Modifier
                        }
                    )
            ) {
                if (layoutMode == LayoutMode.GRID) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = padding,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredEntries, key = { it.key }) { entry ->
                            ToolTile(
                                tool = entry.tool,
                                layoutMode = layoutMode,
                                onClick = {
                                    onToolClick?.invoke(entry.gameId, entry.tool.id)
                                },
                                onLongClick = onToolLongClick?.let { { it(entry.key) } },
                                isHighlighted = entry.key == highlightedToolId,
                                subtitle = entry.gameName
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = padding,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredEntries, key = { it.key }) { entry ->
                            ToolTile(
                                tool = entry.tool,
                                layoutMode = layoutMode,
                                onClick = {
                                    onToolClick?.invoke(entry.gameId, entry.tool.id)
                                },
                                onLongClick = onToolLongClick?.let { { it(entry.key) } },
                                isHighlighted = entry.key == highlightedToolId,
                                subtitle = entry.gameName
                            )
                        }
                    }
                }
            }
        }
    }
}
