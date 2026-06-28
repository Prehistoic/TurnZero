package com.turnzero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.datastore.preferences.core.edit
import dagger.hilt.android.AndroidEntryPoint
import com.turnzero.core.data.UserPreferences
import com.turnzero.core.ui.theme.TZTheme
import com.turnzero.core.ui.theme.colorThemes
import com.turnzero.navigation.TZNavHost
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val systemDark = isSystemInDarkTheme()
            val scope = rememberCoroutineScope()

            var isDark by remember { mutableStateOf(systemDark) }
            LaunchedEffect(Unit) {
                val mode = ServiceLocator.dataStore.data.map { it[UserPreferences.THEME_MODE] }.first() ?: "system"
                isDark = when (mode) {
                    "light" -> false
                    "dark" -> true
                    else -> systemDark
                }
            }

            var selectedColorThemeId by remember { mutableStateOf("purple") }
            LaunchedEffect(Unit) {
                val saved = ServiceLocator.dataStore.data.map { it[UserPreferences.COLOR_THEME] }.first() ?: "purple"
                selectedColorThemeId = saved
            }
            val selectedColorTheme = remember(selectedColorThemeId) {
                colorThemes.find { it.id == selectedColorThemeId } ?: colorThemes.first()
            }

            TZTheme(darkTheme = isDark, colorTheme = selectedColorTheme) {
                TZNavHost(
                    toolRegistry = ServiceLocator.toolRegistry,
                    dataStore = ServiceLocator.dataStore,
                    isDarkTheme = isDark,
                    onThemeChanged = { isDark = it },
                    selectedColorThemeId = selectedColorThemeId,
                    onColorThemeChanged = { id ->
                        selectedColorThemeId = id
                        scope.launch {
                            ServiceLocator.dataStore.edit { prefs ->
                                prefs[UserPreferences.COLOR_THEME] = id
                            }
                        }
                    }
                )
            }
        }
    }
}
