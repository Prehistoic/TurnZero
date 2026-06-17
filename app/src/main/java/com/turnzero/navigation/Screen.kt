package com.turnzero.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Games
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Games : Screen("games")
    data object Favorites : Screen("favorites")
    data object Settings : Screen("settings")

    data object Tools : Screen("tools/{gameId}") {
        fun createRoute(gameId: String) = "tools/$gameId"
    }

    data object ToolDetail : Screen("tool/{gameId}/{toolId}") {
        fun createRoute(gameId: String, toolId: String) = "tool/$gameId/$toolId"
    }
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        screen = Screen.Games,
        label = "Games",
        selectedIcon = Icons.Filled.Games,
        unselectedIcon = Icons.Outlined.Games
    ),
    BottomNavItem(
        screen = Screen.Favorites,
        label = "Favorites",
        selectedIcon = Icons.Filled.Favorite,
        unselectedIcon = Icons.Outlined.FavoriteBorder
    ),
    BottomNavItem(
        screen = Screen.Settings,
        label = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
)
