package com.turnzero.ui.favorites

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
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
import com.turnzero.core.ui.components.TZTileLayout

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

    val filteredEntries = remember(favoriteEntries, searchQuery) {
        if (searchQuery.isEmpty()) {
            favoriteEntries
        } else {
            favoriteEntries.filter {
                it.tool.name.contains(searchQuery, ignoreCase = true) ||
                    it.gameName.contains(searchQuery, ignoreCase = true)
            }
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
                TZTileLayout(
                    tileItems = filteredEntries,
                    layoutMode = layoutMode,
                    key = { it.key },
                    modifier = Modifier.padding(horizontal = 16.dp),
                    contentPadding = padding,
                    verticalSpacing = 4.dp,
                ) { entry ->
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
