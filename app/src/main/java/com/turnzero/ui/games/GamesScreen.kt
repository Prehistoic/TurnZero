package com.turnzero.ui.games

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import com.turnzero.R
import com.turnzero.core.tools.api.GameCategory
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
    var showCategorySheet by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory: GameCategory? by remember { mutableStateOf(null) }
    val allGames = remember { toolRegistry.getGames() }
    val filteredGames = remember(searchQuery, selectedCategory) {
        allGames.filter { game ->
            val matchesSearch = searchQuery.isEmpty() || game.name.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == null || game.category == selectedCategory
            matchesSearch && matchesCategory
        }
    }

    Scaffold(
        topBar = {
            TZTopBar(
                title = "TurnZero",
                logoPainter = painterResource(id = R.drawable.ic_tz_logo),
                showLayoutButton = true,
                showSearchButton = true,
                showCategoryFilter = true,
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                onLayoutClick = { showLayoutSheet = true },
                onSearchClick = { isSearchActive = true },
                onCategoryFilterClick = { showCategorySheet = true },
                onSearchQueryChange = { searchQuery = it },
                onSearchClose = {
                    isSearchActive = false
                    searchQuery = ""
                }
            )
        }
    ) { padding ->
        if (isSearchActive && searchQuery.isNotEmpty() && filteredGames.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                TZEmptyState(message = "No result found")
            }
        } else {
            TZTileLayout(
                tileItems = filteredGames,
                layoutMode = currentLayout,
                key = { it.id },
                modifier = Modifier.padding(horizontal = 16.dp),
                contentPadding = padding,
            ) { game ->
                GameTile(
                    game = game,
                    layoutMode = currentLayout,
                    onClick = { onGameClick(game.id) }
                )
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

    if (showCategorySheet) {
        CategoryFilterSheet(
            selectedCategory = selectedCategory,
            onCategorySelected = {
                selectedCategory = it
                showCategorySheet = false
            },
            onDismiss = { showCategorySheet = false }
        )
    }
}
