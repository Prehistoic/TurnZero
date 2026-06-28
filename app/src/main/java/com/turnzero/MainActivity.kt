package com.turnzero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.edit
import com.turnzero.core.data.UserPreferences
import com.turnzero.core.ui.theme.TZTheme
import com.turnzero.core.ui.theme.colorThemes
import com.turnzero.navigation.TZNavHost
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val systemDark = isSystemInDarkTheme()

            val savedThemeMode = remember {
                runBlocking {
                    ServiceLocator.dataStore.data.first()[UserPreferences.THEME_MODE] ?: "system"
                }
            }
            var isDark by remember {
                mutableStateOf(
                    when (savedThemeMode) {
                        "light" -> false
                        "dark" -> true
                        else -> systemDark
                    }
                )
            }

            val savedColorTheme = remember {
                runBlocking {
                    ServiceLocator.dataStore.data.first()[UserPreferences.COLOR_THEME] ?: "purple"
                }
            }
            var selectedColorThemeId by remember { mutableStateOf(savedColorTheme) }
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
                        runBlocking {
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
