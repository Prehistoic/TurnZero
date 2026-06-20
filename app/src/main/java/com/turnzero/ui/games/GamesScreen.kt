package com.turnzero.ui.games

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turnzero.core.tools.api.ToolRegistry
import com.turnzero.core.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(
    toolRegistry: ToolRegistry,
    currentLayout: LayoutMode,
    onLayoutSelected: (LayoutMode) -> Unit,
    onGameClick: (String) -> Unit
) {
    var showLayoutSheet by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val allGames = remember { toolRegistry.getGames() }
    val games = if (searchQuery.isEmpty()) {
        allGames
    } else {
        allGames.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TZTopBar(
                title = "TurnZero",
                showLayoutButton = true,
                showSearchButton = true,
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                onLayoutClick = { showLayoutSheet = true },
                onSearchClick = { isSearchActive = true },
                onSearchQueryChange = { searchQuery = it },
                onSearchClose = {
                    isSearchActive = false
                    searchQuery = ""
                }
            )
        }
    ) { padding ->
        if (isSearchActive && searchQuery.isNotEmpty() && games.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "\u00AF\\_(\u30C4)_/\u00AF",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 32.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "No result found",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            when (currentLayout) {
                LayoutMode.LIST -> {
                    LazyColumn(
                        contentPadding = padding,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(games, key = { it.id }) { game ->
                            GameTile(
                                game = game,
                                layoutMode = currentLayout,
                                onClick = { onGameClick(game.id) }
                            )
                        }
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = padding,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(games, key = { it.id }) { game ->
                            GameTile(
                                game = game,
                                layoutMode = currentLayout,
                                onClick = { onGameClick(game.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showLayoutSheet) {
        LayoutBottomSheet(
            currentLayout = currentLayout,
            onLayoutSelected = {
                onLayoutSelected(it)
                showLayoutSheet = false
            },
            onDismiss = { showLayoutSheet = false }
        )
    }
}
