package com.turnzero.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Scaffold
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
    onThemeChanged: (Boolean) -> Unit
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

    Scaffold(
        bottomBar = {
            TZBottomNavBar(
                currentRoute = currentRoute,
                onItemClick = { screen ->
                    navController.navigate(screen.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
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
                FavoritesScreen()
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    currentTheme = themeModeState,
                    onThemeSelected = { mode ->
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
                    }
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
                    onBack = { navController.popBackStack() }
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
