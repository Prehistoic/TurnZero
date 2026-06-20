package com.turnzero.ui.favorites

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.turnzero.core.tools.api.Tool
import com.turnzero.core.ui.components.TZTopBar
import com.turnzero.core.ui.components.ToolTile
import com.turnzero.core.ui.components.LayoutMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    favoriteTools: List<Tool> = emptyList(),
    onToolClick: ((gameId: String, toolId: String) -> Unit)? = null,
    highlightedToolId: String? = null,
    onToolLongClick: ((String) -> Unit)? = null,
    onTapOutside: (() -> Unit)? = null
) {
    Scaffold(
        topBar = {
            TZTopBar(title = "Favorites")
        }
    ) { padding ->
        if (favoriteTools.isEmpty()) {
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
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = padding,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(favoriteTools, key = { it.id }) { tool ->
                        val gameId = tool.supportedGames.firstOrNull()
                        ToolTile(
                            tool = tool,
                            layoutMode = LayoutMode.GRID,
                            onClick = {
                                if (gameId != null && onToolClick != null) {
                                    onToolClick(gameId, tool.id)
                                }
                            },
                            onLongClick = onToolLongClick?.let { { it(tool.id) } },
                            isHighlighted = tool.id == highlightedToolId
                        )
                    }
                }
            }
        }
    }
}
