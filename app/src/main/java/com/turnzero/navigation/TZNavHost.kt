package com.turnzero.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.turnzero.core.data.UserPreferences
import com.turnzero.core.tools.api.ToolRegistry
import com.turnzero.core.ui.components.*
import com.turnzero.ui.favorites.FavoritesScreen
import com.turnzero.ui.games.GamesScreen
import com.turnzero.ui.settings.AppearanceSettingsScreen
import com.turnzero.ui.settings.DisplaySettingsScreen
import com.turnzero.ui.settings.LanguageSelectionScreen
import com.turnzero.ui.settings.SearchSettingsScreen
import com.turnzero.ui.settings.SettingsScreen
import com.turnzero.ui.settings.ThemeMode
import com.turnzero.ui.tools.ToolSwitcherFAB
import com.turnzero.ui.tools.ToolSwitcherSheet
import com.turnzero.ui.tools.ToolsScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Composable
fun TZNavHost(
    toolRegistry: ToolRegistry,
    dataStore: DataStore<Preferences>,
    isDarkTheme: Boolean,
    onThemeChanged: (Boolean) -> Unit,
    selectedColorThemeId: String,
    onColorThemeChanged: (String) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val scope = rememberCoroutineScope()
    val systemIsDark = isSystemInDarkTheme()

    val gamesLayout = remember {
        runBlocking {
            dataStore.data.first()[UserPreferences.LAYOUT_GAMES]?.let {
                try { LayoutMode.valueOf(it) } catch (_: Exception) { LayoutMode.GRID }
            } ?: LayoutMode.GRID
        }
    }

    var gamesLayoutState by remember { mutableStateOf(gamesLayout) }

    val toolsLayout = remember {
        runBlocking {
            dataStore.data.first()[UserPreferences.LAYOUT_TOOLS]?.let {
                try { LayoutMode.valueOf(it) } catch (_: Exception) { LayoutMode.GRID }
            } ?: LayoutMode.GRID
        }
    }

    var toolsLayoutState by remember { mutableStateOf(toolsLayout) }

    val themeMode = remember {
        runBlocking {
            dataStore.data.first()[UserPreferences.THEME_MODE] ?: "system"
        }
    }

    var themeModeState by remember {
        mutableStateOf(
            try { ThemeMode.valueOf(themeMode.uppercase()) } catch (_: Exception) { ThemeMode.SYSTEM }
        )
    }

    val initialFavorites = remember {
        runBlocking {
            dataStore.data.first()[UserPreferences.FAVORITE_TOOLS] ?: emptySet()
        }
    }

    val savedLanguage = remember {
        runBlocking {
            dataStore.data.first()[UserPreferences.LANGUAGE] ?: "en"
        }
    }

    var favoriteToolIds by remember { mutableStateOf(initialFavorites) }
    var selectedToolId by remember { mutableStateOf<String?>(null) }
    var currentLanguage by remember { mutableStateOf(savedLanguage) }

    Scaffold(
        bottomBar = {
            if (selectedToolId != null) {
                NavigationBar {
                    val isFavorited = selectedToolId in favoriteToolIds
                    NavigationBarItem(
                        selected = isFavorited,
                        onClick = {
                            val toolId = selectedToolId ?: return@NavigationBarItem
                            val updated = if (isFavorited) {
                                favoriteToolIds - toolId
                            } else {
                                favoriteToolIds + toolId
                            }
                            favoriteToolIds = updated
                            if (isFavorited) {
                                selectedToolId = null
                            }
                            scope.launch {
                                dataStore.edit { prefs ->
                                    prefs[UserPreferences.FAVORITE_TOOLS] = updated
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isFavorited) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = if (isFavorited) "Remove from favorites" else "Add to favorites"
                            )
                        },
                        label = { Text(if (isFavorited) "Favorited" else "Favorite") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            } else {
                TZBottomNavBar(
                    currentRoute = currentRoute,
                    onItemClick = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Games.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Games.route) {
                GamesScreen(
                    toolRegistry = toolRegistry,
                    currentLayout = gamesLayoutState,
                    onLayoutSelected = { mode ->
                        gamesLayoutState = mode
                        scope.launch {
                            dataStore.edit { prefs ->
                                prefs[UserPreferences.LAYOUT_GAMES] = mode.name
                            }
                        }
                    },
                    onGameClick = { gameId ->
                        navController.navigate(Screen.Tools.createRoute(gameId))
                    }
                )
            }

            composable(Screen.Favorites.route) {
                val favorites = remember(favoriteToolIds) {
                    toolRegistry.getToolsByIds(favoriteToolIds)
                }
                FavoritesScreen(
                    favoriteTools = favorites,
                    onToolClick = { gameId, toolId ->
                        navController.navigate(Screen.ToolDetail.createRoute(gameId, toolId))
                    },
                    highlightedToolId = selectedToolId,
                    onToolLongClick = { toolId ->
                        selectedToolId = toolId
                    },
                    onTapOutside = {
                        selectedToolId = null
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onSearchClick = {
                        navController.navigate(Screen.SearchSettings.route)
                    },
                    onAppearanceClick = {
                        navController.navigate(Screen.AppearanceSettings.route)
                    }
                )
            }

            composable(Screen.AppearanceSettings.route) {
                AppearanceSettingsScreen(
                    currentTheme = themeModeState,
                    selectedColorThemeId = selectedColorThemeId,
                    currentLanguage = currentLanguage,
                    gamesLayout = gamesLayoutState,
                    toolsLayout = toolsLayoutState,
                    onThemeModeSelected = { mode ->
                        themeModeState = mode
                        scope.launch {
                            dataStore.edit { prefs ->
                                prefs[UserPreferences.THEME_MODE] = mode.name.lowercase()
                            }
                        }
                        onThemeChanged(
                            when (mode) {
                                ThemeMode.LIGHT -> false
                                ThemeMode.DARK -> true
                                ThemeMode.SYSTEM -> systemIsDark
                            }
                        )
                    },
                    onColorThemeSelected = { id ->
                        onColorThemeChanged(id)
                    },
                    onLanguageClick = {
                        navController.navigate(Screen.LanguageSettings.route)
                    },
                    onGamesLayoutClick = {
                        navController.navigate(Screen.DisplaySettings.createRoute("games"))
                    },
                    onToolsLayoutClick = {
                        navController.navigate(Screen.DisplaySettings.createRoute("tools"))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.LanguageSettings.route) {
                LanguageSelectionScreen(
                    currentLanguage = currentLanguage,
                    onLanguageSelected = { code ->
                        currentLanguage = code
                        scope.launch {
                            dataStore.edit { prefs ->
                                prefs[UserPreferences.LANGUAGE] = code
                            }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.DisplaySettings.route,
                arguments = listOf(navArgument("type") { type = NavType.StringType })
            ) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type") ?: return@composable
                val layout = if (type == "games") gamesLayoutState else toolsLayoutState
                DisplaySettingsScreen(
                    type = type,
                    currentLayout = layout,
                    onLayoutSelected = { mode ->
                        if (type == "games") {
                            gamesLayoutState = mode
                            scope.launch {
                                dataStore.edit { prefs ->
                                    prefs[UserPreferences.LAYOUT_GAMES] = mode.name
                                }
                            }
                        } else {
                            toolsLayoutState = mode
                            scope.launch {
                                dataStore.edit { prefs ->
                                    prefs[UserPreferences.LAYOUT_TOOLS] = mode.name
                                }
                            }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.SearchSettings.route) {
                SearchSettingsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.Tools.route,
                arguments = listOf(navArgument("gameId") { type = NavType.StringType })
            ) { backStackEntry ->
                val gameId = backStackEntry.arguments?.getString("gameId") ?: return@composable
                ToolsScreen(
                    toolRegistry = toolRegistry,
                    gameId = gameId,
                    currentLayout = toolsLayoutState,
                    onLayoutSelected = { mode ->
                        toolsLayoutState = mode
                        scope.launch {
                            dataStore.edit { prefs ->
                                prefs[UserPreferences.LAYOUT_TOOLS] = mode.name
                            }
                        }
                    },
                    onToolClick = { toolId ->
                        navController.navigate(Screen.ToolDetail.createRoute(gameId, toolId))
                    },
                    onBack = { navController.popBackStack() },
                    highlightedToolId = selectedToolId,
                    onToolLongClick = { toolId ->
                        selectedToolId = toolId
                    },
                    onTapOutside = {
                        selectedToolId = null
                    }
                )
            }

            composable(
                route = Screen.ToolDetail.route,
                arguments = listOf(
                    navArgument("gameId") { type = NavType.StringType },
                    navArgument("toolId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val gameId = backStackEntry.arguments?.getString("gameId") ?: return@composable
                val toolId = backStackEntry.arguments?.getString("toolId") ?: return@composable
                val allTools = remember(gameId) { toolRegistry.getToolsForGame(gameId) }
                val tool = allTools.firstOrNull { it.id == toolId } ?: return@composable

                var showSwitcher by remember { mutableStateOf(false) }

                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        topBar = {
                            TZTopBar(
                                title = tool.name,
                                showBackButton = true,
                                onBackClick = { navController.popBackStack() }
                            )
                        },
                        floatingActionButton = {
                            ToolSwitcherFAB(
                                onTap = { showSwitcher = true }
                            )
                        }
                    ) { contentPadding ->
                        Box(modifier = Modifier.padding(contentPadding)) {
                            tool.Screen(onBack = { navController.popBackStack() })
                        }
                    }
                }

                if (showSwitcher) {
                    ToolSwitcherSheet(
                        tools = allTools,
                        currentToolId = toolId,
                        onToolSelected = { selected ->
                            showSwitcher = false
                            navController.navigate(
                                Screen.ToolDetail.createRoute(gameId, selected.id)
                            ) {
                                popUpTo(Screen.Tools.createRoute(gameId)) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        onDismiss = { showSwitcher = false }
                    )
                }
            }
        }
    }
}
