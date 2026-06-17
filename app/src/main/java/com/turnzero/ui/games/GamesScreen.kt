package com.turnzero.ui.games

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.turnzero.core.tools.api.ToolRegistry
import com.turnzero.core.ui.components.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(
    toolRegistry: ToolRegistry,
    currentLayout: LayoutMode,
    onLayoutSelected: (LayoutMode) -> Unit,
    onGameClick: (String) -> Unit
) {
    var showLayoutSheet by remember { mutableStateOf(false) }
    val games = remember { toolRegistry.getGames() }

    Scaffold(
        topBar = {
            TZTopBar(
                title = "TurnZero",
                showLayoutButton = true,
                onLayoutClick = { showLayoutSheet = true }
            )
        }
    ) { padding ->
        when (currentLayout) {
            LayoutMode.LIST -> {
                LazyColumn(
                    contentPadding = PaddingValues(padding)
                        .plus(PaddingValues(horizontal = 16.dp, vertical = 8.dp)),
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
                val columns = if (currentLayout == LayoutMode.COMPACT) 3 else 2
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    contentPadding = PaddingValues(padding)
                        .plus(PaddingValues(horizontal = 16.dp, vertical = 8.dp)),
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
