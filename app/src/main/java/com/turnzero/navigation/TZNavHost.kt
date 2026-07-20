package com.turnzero.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.turnzero.core.ui.components.*
import com.turnzero.ui.MainViewModel
import com.turnzero.ui.favorites.FavoritesScreen
import com.turnzero.ui.games.GamesScreen
import com.turnzero.ui.settings.AboutSettingsScreen
import com.turnzero.ui.settings.AppearanceSettingsScreen
import com.turnzero.ui.settings.DisplaySettingsScreen
import com.turnzero.ui.settings.LanguageSelectionScreen
import com.turnzero.ui.settings.LicensesScreen
import com.turnzero.ui.settings.SearchSettingsScreen
import com.turnzero.ui.settings.SettingsScreen
import com.turnzero.ui.tools.ToolSwitcherSheet
import com.turnzero.ui.tools.ToolsScreen

@Composable
fun TZNavHost(
    selectedColorThemeId: String,
    onColorThemeChanged: (String) -> Unit
) {
    val mainViewModel: MainViewModel = hiltViewModel()
    val mainUiState by mainViewModel.uiState.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val currentToolId = navBackStackEntry?.arguments?.getString("toolId")
    val currentGameId = navBackStackEntry?.arguments?.getString("gameId")
    val currentTool = mainUiState.selectedTool

    LaunchedEffect(currentToolId, currentGameId) {
        mainViewModel.selectTool(currentToolId, currentGameId)
    }

    LaunchedEffect(currentRoute) {
        mainViewModel.dismissAllToolSheets()
    }

    val toolRegistry = mainViewModel.toolRegistry

    Scaffold(
        contentWindowInsets = if (currentTool?.showBottomBar == false)
            WindowInsets(0.dp)
        else
            ScaffoldDefaults.contentWindowInsets,
        bottomBar = {
            when {
                currentRoute?.startsWith("tool/") == true && currentTool?.showBottomBar != false -> {
                    ToolBottomNavBar(
                        onToolsClick = { mainViewModel.showToolSwitcher(true) },
                        onOptionsClick = { mainViewModel.showToolOptions(true) }
                    )
                }
                currentRoute?.startsWith("tool/") == true -> {
                    // Landscape tool — no bottom bar
                }
                mainUiState.selectedTool != null -> {
                    val toolId = mainUiState.selectedTool!!.id
                    val isFavorited = toolId in mainUiState.favoriteToolIds
                    NavigationBar {
                        NavigationBarItem(
                            selected = isFavorited,
                            onClick = { mainViewModel.toggleFavorite(toolId) },
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
            composable(
                route = Screen.Games.route,
                enterTransition = { fadeIn(initialAlpha = 0f, animationSpec = tween(150)) },
                exitTransition = { fadeOut(targetAlpha = 0f, animationSpec = tween(150)) },
            ) {
                GamesScreen(
                    toolRegistry = toolRegistry,
                    currentLayout = mainUiState.gamesLayout,
                    onLayoutSelected = mainViewModel::setGamesLayout,
                    onGameClick = { gameId ->
                        navController.navigate(Screen.Tools.createRoute(gameId))
                    }
                )
            }

            composable(
                route = Screen.Favorites.route,
                enterTransition = { fadeIn(initialAlpha = 0f, animationSpec = tween(150)) },
                exitTransition = { fadeOut(targetAlpha = 0f, animationSpec = tween(150)) },
            ) {
                val favoriteEntries = remember(mainUiState.favoriteToolIds) {
                    toolRegistry.getFavoriteEntries(mainUiState.favoriteToolIds)
                }
                FavoritesScreen(
                    favoriteEntries = favoriteEntries,
                    onToolClick = { gameId, toolId ->
                        navController.navigate(Screen.ToolDetail.createRoute(gameId, toolId))
                    },
                    highlightedToolId = mainUiState.selectedTool?.id,
                    onToolLongClick = { _ -> },
                    onTapOutside = { },
                    layoutMode = mainUiState.toolsLayout
                )
            }

            composable(
                route = Screen.Settings.route,
                enterTransition = { fadeIn(initialAlpha = 0f, animationSpec = tween(150)) },
                exitTransition = { fadeOut(targetAlpha = 0f, animationSpec = tween(150)) },
            ) {
                SettingsScreen(
                    onSearchClick = { navController.navigate(Screen.SearchSettings.route) },
                    onAppearanceClick = { navController.navigate(Screen.AppearanceSettings.route) },
                    onAboutClick = { navController.navigate(Screen.AboutSettings.route) }
                )
            }

            composable(
                route = Screen.AppearanceSettings.route,
                enterTransition = { fadeIn(initialAlpha = 0f, animationSpec = tween(150)) },
                exitTransition = { fadeOut(targetAlpha = 0f, animationSpec = tween(150)) },
            ) {
                AppearanceSettingsScreen(
                    currentTheme = mainUiState.themeMode,
                    selectedColorThemeId = selectedColorThemeId,
                    currentLanguage = mainUiState.currentLanguage,
                    gamesLayout = mainUiState.gamesLayout,
                    toolsLayout = mainUiState.toolsLayout,
                    onThemeModeSelected = { mode ->
                        mainViewModel.setThemeMode(mode)
                        onColorThemeChanged(selectedColorThemeId)
                    },
                    onColorThemeSelected = onColorThemeChanged,
                    onLanguageClick = { navController.navigate(Screen.LanguageSettings.route) },
                    onGamesLayoutClick = { navController.navigate(Screen.DisplaySettings.createRoute("games")) },
                    onToolsLayoutClick = { navController.navigate(Screen.DisplaySettings.createRoute("tools")) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.LanguageSettings.route,
                enterTransition = { fadeIn(initialAlpha = 0f, animationSpec = tween(150)) },
                exitTransition = { fadeOut(targetAlpha = 0f, animationSpec = tween(150)) },
            ) {
                LanguageSelectionScreen(
                    currentLanguage = mainUiState.currentLanguage,
                    onLanguageSelected = mainViewModel::setLanguage,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.DisplaySettings.route,
                arguments = listOf(navArgument("type") { type = NavType.StringType }),
                enterTransition = { fadeIn(initialAlpha = 0f, animationSpec = tween(150)) },
                exitTransition = { fadeOut(targetAlpha = 0f, animationSpec = tween(150)) },
            ) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type") ?: return@composable
                val layout = if (type == "games") mainUiState.gamesLayout else mainUiState.toolsLayout
                DisplaySettingsScreen(
                    type = type,
                    currentLayout = layout,
                    onLayoutSelected = { mode ->
                        if (type == "games") mainViewModel.setGamesLayout(mode)
                        else mainViewModel.setToolsLayout(mode)
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.SearchSettings.route,
                enterTransition = { fadeIn(initialAlpha = 0f, animationSpec = tween(150)) },
                exitTransition = { fadeOut(targetAlpha = 0f, animationSpec = tween(150)) },
            ) {
                SearchSettingsScreen(onBack = { navController.popBackStack() })
            }

            composable(
                route = Screen.AboutSettings.route,
                enterTransition = { fadeIn(initialAlpha = 0f, animationSpec = tween(150)) },
                exitTransition = { fadeOut(targetAlpha = 0f, animationSpec = tween(150)) },
            ) {
                AboutSettingsScreen(
                    onBack = { navController.popBackStack() },
                    onLicensesClick = { navController.navigate(Screen.LicensesSettings.route) }
                )
            }

            composable(
                route = Screen.LicensesSettings.route,
                enterTransition = { fadeIn(initialAlpha = 0f, animationSpec = tween(150)) },
                exitTransition = { fadeOut(targetAlpha = 0f, animationSpec = tween(150)) },
            ) {
                LicensesScreen(onBack = { navController.popBackStack() })
            }

            composable(
                route = Screen.Tools.route,
                arguments = listOf(navArgument("gameId") { type = NavType.StringType }),
                enterTransition = { fadeIn(initialAlpha = 0f, animationSpec = tween(150)) },
                exitTransition = { fadeOut(targetAlpha = 0f, animationSpec = tween(150)) },
            ) { backStackEntry ->
                val gameId = backStackEntry.arguments?.getString("gameId") ?: return@composable
                ToolsScreen(
                    toolRegistry = toolRegistry,
                    gameId = gameId,
                    currentLayout = mainUiState.toolsLayout,
                    onLayoutSelected = mainViewModel::setToolsLayout,
                    onToolClick = { toolId ->
                        navController.navigate(Screen.ToolDetail.createRoute(gameId, toolId))
                    },
                    onBack = { navController.popBackStack() },
                    highlightedToolId = mainUiState.selectedTool?.id,
                    onToolLongClick = { _ -> },
                    onTapOutside = { }
                )
            }

            composable(
                route = Screen.ToolDetail.route,
                arguments = listOf(
                    navArgument("gameId") { type = NavType.StringType },
                    navArgument("toolId") { type = NavType.StringType }
                ),
                enterTransition = { fadeIn(initialAlpha = 0f, animationSpec = tween(150)) },
                exitTransition = { fadeOut(targetAlpha = 0f, animationSpec = tween(150)) },
            ) { backStackEntry ->
                val gameId = backStackEntry.arguments?.getString("gameId") ?: return@composable
                val toolId = backStackEntry.arguments?.getString("toolId") ?: return@composable
                val allTools = remember(gameId) { toolRegistry.getToolsForGame(gameId) }
                val tool = allTools.firstOrNull { it.id == toolId } ?: return@composable

                CompositionLocalProvider(
                    LocalToolSheetActions provides ToolSheetActions(
                        onOpenTools = { mainViewModel.showToolSwitcher(true) },
                        onOpenOptions = { mainViewModel.showToolOptions(true) }
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

                if (mainUiState.showToolSwitcher) {
                    ToolSwitcherSheet(
                        tools = allTools,
                        currentToolId = toolId,
                        isLandscape = currentTool?.showBottomBar == false,
                        onToolSelected = { selected ->
                            mainViewModel.showToolSwitcher(false)
                            navController.navigate(
                                Screen.ToolDetail.createRoute(gameId, selected.id)
                            ) {
                                popUpTo(Screen.Tools.createRoute(gameId)) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        onDismiss = { mainViewModel.showToolSwitcher(false) }
                    )
                }

                if (mainUiState.showToolOptions) {
                    tool.OptionsSheet(onDismiss = { mainViewModel.showToolOptions(false) })
                }
            }
        }
    }
}
