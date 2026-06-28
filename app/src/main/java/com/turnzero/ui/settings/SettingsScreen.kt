package com.turnzero.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.turnzero.core.ui.components.TZTopBar

enum class ThemeMode { SYSTEM, LIGHT, DARK }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onSearchClick: () -> Unit,
    onAppearanceClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TZTopBar(
                title = "Settings",
                showSearchButton = true,
                onSearchClick = onSearchClick
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            HorizontalDivider()

            SettingsCategoryRow(
                icon = Icons.Outlined.Palette,
                title = "Appearance",
                description = "Theme, colors and language",
                onClick = onAppearanceClick
            )

            HorizontalDivider()

            SettingsCategoryRow(
                icon = Icons.Outlined.Info,
                title = "About",
                description = "Version, licenses and more",
                onClick = onAboutClick
            )

            HorizontalDivider()
        }
    }
}

@Composable
internal fun SettingsCategoryRow(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    widget: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (widget != null) {
            Spacer(Modifier.width(8.dp))
            widget()
        }
    }
}
