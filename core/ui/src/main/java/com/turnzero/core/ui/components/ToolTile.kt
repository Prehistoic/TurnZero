package com.turnzero.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.turnzero.core.tools.api.Tool

@Composable
fun ToolTile(
    tool: Tool,
    layoutMode: LayoutMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (layoutMode) {
        LayoutMode.LIST -> ToolTileList(tool, onClick, modifier)
        LayoutMode.GRID -> ToolTileGrid(tool, onClick, modifier)
    }
}

@Composable
private fun ToolTileGrid(tool: Tool, onClick: () -> Unit, modifier: Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = tool.icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = tool.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ToolTileList(tool: Tool, onClick: () -> Unit, modifier: Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = tool.icon,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = tool.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
