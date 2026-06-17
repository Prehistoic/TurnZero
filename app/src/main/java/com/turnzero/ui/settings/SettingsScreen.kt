package com.turnzero.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.turnzero.core.ui.components.TZTopBar

enum class ThemeMode { SYSTEM, LIGHT, DARK }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit
) {
    Scaffold(
        topBar = {
            TZTopBar(title = "Settings")
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ThemeOption("Follow System", ThemeMode.SYSTEM, currentTheme, onThemeSelected)
            ThemeOption("Light", ThemeMode.LIGHT, currentTheme, onThemeSelected)
            ThemeOption("Dark", ThemeMode.DARK, currentTheme, onThemeSelected)

            Spacer(Modifier.height(32.dp))

            Text(
                text = "About",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TurnZero v0.1.0",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Your all-in-one boardgame companion",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeOption(
    label: String,
    mode: ThemeMode,
    current: ThemeMode,
    onSelect: (ThemeMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = current == mode,
            onClick = { onSelect(mode) }
        )
        Spacer(Modifier.width(12.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}
