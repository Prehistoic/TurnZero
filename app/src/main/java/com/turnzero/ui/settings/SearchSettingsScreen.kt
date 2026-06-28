package com.turnzero.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turnzero.core.ui.components.TZTopBar
import com.turnzero.core.ui.components.TZDivider

data class SettingEntry(
    val id: String,
    val category: String,
    val title: String,
    val description: String
)

val searchableSettings = listOf(
    SettingEntry("theme", "Appearance", "Theme", "Light, Dark or System"),
    SettingEntry("games_layout", "Display", "Games Layout", "Grid or List"),
    SettingEntry("tools_layout", "Display", "Tools Layout", "Grid or List"),
    SettingEntry("about", "About", "Version", "App version, licenses and updates"),
    SettingEntry("about", "About", "Changelog", "View release notes and changes"),
    SettingEntry("about", "About", "Licenses", "Open source component licenses"),
    SettingEntry("about", "About", "Updates", "Check for app updates")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchSettingsScreen(onBack: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }

    val results = if (searchQuery.isEmpty()) {
        emptyList()
    } else {
        searchableSettings.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TZTopBar(
                title = "",
                showBackButton = true,
                isSearchActive = true,
                searchQuery = searchQuery,
                onBackClick = onBack,
                onSearchClose = onBack,
                onSearchQueryChange = { searchQuery = it }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            HorizontalDivider()

            if (searchQuery.isNotEmpty() && results.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
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
            } else if (results.isNotEmpty()) {
                Column {
                    results.forEach { setting ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = setting.title,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "${setting.category} \u2022 ${setting.description}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        TZDivider()
                    }
                }
            }
        }
    }
}
