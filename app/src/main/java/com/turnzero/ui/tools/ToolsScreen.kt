package com.turnzero.ui.tools

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.turnzero.core.tools.api.ToolRegistry
import com.turnzero.core.ui.components.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    toolRegistry: ToolRegistry,
    gameId: String,
    currentLayout: LayoutMode,
    onLayoutSelected: (LayoutMode) -> Unit,
    onToolClick: (String) -> Unit,
    onBack: () -> Unit,
    highlightedToolId: String? = null,
    onToolLongClick: ((String) -> Unit)? = null,
    onTapOutside: (() -> Unit)? = null
) {
    var showLayoutSheet by remember { mutableStateOf(false) }
    val tools = remember(gameId) { toolRegistry.getToolsForGame(gameId) }
    val gameName = remember(gameId) {
        toolRegistry.getGames().firstOrNull { it.id == gameId }?.name ?: gameId
    }

    Scaffold(
        topBar = {
            TZTopBar(
                title = gameName,
                showBackButton = true,
                showLayoutButton = true,
                onBackClick = onBack,
                onLayoutClick = { showLayoutSheet = true }
            )
        }
    ) { padding ->
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
            when (currentLayout) {
                LayoutMode.LIST -> {
                    LazyColumn(
                        contentPadding = padding,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(tools, key = { it.id }) { tool ->
                            ToolTile(
                                tool = tool,
                                layoutMode = currentLayout,
                                onClick = { onToolClick(tool.id) },
                                onLongClick = onToolLongClick?.let { { it(tool.id) } },
                                isHighlighted = tool.id == highlightedToolId
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
                        items(tools, key = { it.id }) { tool ->
                            ToolTile(
                                tool = tool,
                                layoutMode = currentLayout,
                                onClick = { onToolClick(tool.id) },
                                onLongClick = onToolLongClick?.let { { it(tool.id) } },
                                isHighlighted = tool.id == highlightedToolId
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
