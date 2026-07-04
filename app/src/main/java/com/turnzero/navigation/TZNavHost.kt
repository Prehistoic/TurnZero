package com.turnzero.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import com.turnzero.ui.settings.AboutSettingsScreen
import com.turnzero.ui.settings.AppearanceSettingsScreen
import com.turnzero.ui.settings.DisplaySettingsScreen
import com.turnzero.ui.settings.LanguageSelectionScreen
import com.turnzero.ui.settings.LicensesScreen
import com.turnzero.ui.settings.SearchSettingsScreen
import com.turnzero.ui.settings.SettingsScreen
import com.turnzero.ui.settings.ThemeMode
import com.turnzero.ui.tools.ToolSwitcherSheet
import com.turnzero.ui.tools.ToolsScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

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

    var gamesLayoutState by remember { mutableStateOf(LayoutMode.GRID) }
    LaunchedEffect(Unit) {
        dataStore.data.map { it[UserPreferences.LAYOUT_GAMES] }.first()?.let {
            try { gamesLayoutState = LayoutMode.valueOf(it) } catch (_: Exception) {}
        }
    }

    var toolsLayoutState by remember { mutableStateOf(LayoutMode.GRID) }
    LaunchedEffect(Unit) {
        dataStore.data.map { it[UserPreferences.LAYOUT_TOOLS] }.first()?.let {
            try { toolsLayoutState = LayoutMode.valueOf(it) } catch (_: Exception) {}
        }
    }

    var themeModeState by remember { mutableStateOf(ThemeMode.SYSTEM) }
    LaunchedEffect(Unit) {
        val mode = dataStore.data.map { it[UserPreferences.THEME_MODE] }.first() ?: "system"
        themeModeState = try { ThemeMode.valueOf(mode.uppercase()) } catch (_: Exception) { ThemeMode.SYSTEM }
    }

    var favoriteToolIds by remember { mutableStateOf(emptySet<String>()) }
    LaunchedEffect(Unit) {
        val favs = dataStore.data.map { it[UserPreferences.FAVORITE_TOOLS] }.first() ?: emptySet()
        favoriteToolIds = favs
    }

    var currentLanguage by remember { mutableStateOf("en") }
    LaunchedEffect(Unit) {
        val lang = dataStore.data.map { it[UserPreferences.LANGUAGE] }.first() ?: "en"
        currentLanguage = lang
    }

    var selectedToolId by remember { mutableStateOf<String?>(null) }
    var toolShowSwitcher by remember { mutableStateOf(false) }
    var toolShowOptions by remember { mutableStateOf(false) }

    LaunchedEffect(currentRoute) {
        selectedToolId = null
        toolShowSwitcher = false
        toolShowOptions = false
    }

    val currentToolId = navBackStackEntry?.arguments?.getString("toolId")
    val currentTool = remember(currentToolId) { if (currentToolId != null) toolRegistry.getToolById(currentToolId) else null }

    Scaffold(
        bottomBar = {
            when {
                currentRoute?.startsWith("tool/") == true && currentTool?.showBottomBar != false -> {
                    ToolBottomNavBar(
                        onToolsClick = { toolShowSwitcher = true },
                        onOptionsClick = { toolShowOptions = true }
                    )
                }
                currentRoute?.startsWith("tool/") == true -> {
                    // Landscape tool — no bottom bar
                }
                selectedToolId != null -> {
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
                            colors = navItemColors
                        )
                    }
                }
                else -> {
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
                val favoriteEntries = remember(favoriteToolIds) {
                    toolRegistry.getFavoriteEntries(favoriteToolIds)
                }
                FavoritesScreen(
                    favoriteEntries = favoriteEntries,
                    onToolClick = { gameId, toolId ->
                        navController.navigate(Screen.ToolDetail.createRoute(gameId, toolId))
                    },
                    highlightedToolId = selectedToolId,
                    onToolLongClick = { compositeKey ->
                        selectedToolId = compositeKey
                    },
                    onTapOutside = {
                        selectedToolId = null
                    },
                    layoutMode = toolsLayoutState
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onSearchClick = {
                        navController.navigate(Screen.SearchSettings.route)
                    },
                    onAppearanceClick = {
                        navController.navigate(Screen.AppearanceSettings.route)
                    },
                    onAboutClick = {
                        navController.navigate(Screen.AboutSettings.route)
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

            composable(Screen.AboutSettings.route) {
                AboutSettingsScreen(
                    onBack = { navController.popBackStack() },
                    onLicensesClick = {
                        navController.navigate(Screen.LicensesSettings.route)
                    }
                )
            }

            composable(Screen.LicensesSettings.route) {
                LicensesScreen(
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
                    onToolLongClick = { compositeKey ->
                        selectedToolId = compositeKey
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

                CompositionLocalProvider(
                    LocalToolSheetActions provides ToolSheetActions(
                        onOpenTools = { toolShowSwitcher = true },
                        onOpenOptions = { toolShowOptions = true }
                    )
                ) {
                    Column {
                        TZTopBar(
                            title = tool.name,
                            showBackButton = true,
                            onBackClick = { navController.popBackStack() },
                            actions = tool.TopBarActions(),
                            modifier = if (currentTool?.showBottomBar == false) 
                                Modifier.padding(horizontal = 0.dp) 
                            else 
                                Modifier
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .then(
                                    if (currentTool?.showBottomBar == false) 
                                        Modifier.padding(horizontal = 0.dp) 
                                    else 
                                        Modifier
                                )
                        ) {
                            tool.Screen(onBack = { navController.popBackStack() })
                        }
                    }
                }

                if (toolShowSwitcher) {
                    ToolSwitcherSheet(
                        tools = allTools,
                        currentToolId = toolId,
                        isLandscape = currentTool?.showBottomBar == false,
                        onToolSelected = { selected ->
                            toolShowSwitcher = false
                            navController.navigate(
                                Screen.ToolDetail.createRoute(gameId, selected.id)
                            ) {
                                popUpTo(Screen.Tools.createRoute(gameId)) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        onDismiss = { toolShowSwitcher = false }
                    )
                }

                if (toolShowOptions) {
                    tool.OptionsSheet(onDismiss = { toolShowOptions = false })
                }
            }
        }
    }
}
