package com.turnzero.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ToolBottomNavBar(
    onToolsClick: () -> Unit,
    onOptionsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        NavigationBarItem(
            selected = false,
            onClick = onToolsClick,
            icon = {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Tools"
                )
            },
            label = { Text("Tools") },
            colors = navItemColors
        )
        NavigationBarItem(
            selected = false,
            onClick = onOptionsClick,
            icon = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Options"
                )
            },
            label = { Text("Options") },
            colors = navItemColors
        )
    }
}
