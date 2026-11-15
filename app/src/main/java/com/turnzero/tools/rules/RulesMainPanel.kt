package com.turnzero.tools.rules

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.turnzero.core.ui.components.LayoutMode
import com.turnzero.core.ui.components.TZTileLayout

/** Main panel of the rules tool: one tile per [RuleSubTool], in the order given. */
@Composable
fun RulesMainPanel(
    subTools: List<RuleSubTool>,
    layoutMode: LayoutMode,
    onSubToolClick: (RuleSubTool) -> Unit,
    modifier: Modifier = Modifier,
) {
    TZTileLayout(
        tileItems = subTools,
        layoutMode = layoutMode,
        key = { it.id },
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        itemContent = { subTool ->
            RuleSubToolTile(
                subTool = subTool,
                layoutMode = layoutMode,
                onClick = { onSubToolClick(subTool) },
            )
        },
    )
}

@Composable
private fun RuleSubToolTile(
    subTool: RuleSubTool,
    layoutMode: LayoutMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        when (layoutMode) {
            LayoutMode.LIST -> RuleSubToolTileList(subTool)
            LayoutMode.GRID -> RuleSubToolTileGrid(subTool)
        }
    }
}

@Composable
private fun RuleSubToolTileGrid(subTool: RuleSubTool) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = subTool.icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = subTool.title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun RuleSubToolTileList(subTool: RuleSubTool) {
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = subTool.icon,
            contentDescription = null,
            modifier = Modifier.size(36.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = subTool.title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
