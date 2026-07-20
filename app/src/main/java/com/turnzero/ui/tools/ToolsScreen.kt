package com.turnzero.ui.tools

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
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
            TZTileLayout(
                tileItems = tools,
                layoutMode = currentLayout,
                key = { it.id },
                modifier = Modifier.padding(horizontal = 16.dp),
                contentPadding = padding,
            ) { tool ->
                ToolTile(
                    tool = tool,
                    layoutMode = currentLayout,
                    onClick = { onToolClick(tool.id) },
                    onLongClick = onToolLongClick?.let { { it("$gameId:${tool.id}") } },
                    isHighlighted = "$gameId:${tool.id}" == highlightedToolId
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
}
