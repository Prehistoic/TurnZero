package com.turnzero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turnzero.core.ui.theme.TZTheme
import com.turnzero.core.ui.theme.colorThemes
import com.turnzero.navigation.TZNavHost
import com.turnzero.ui.MainViewModel
import com.turnzero.ui.settings.ThemeMode
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            val mainUiState by mainViewModel.uiState.collectAsStateWithLifecycle()

            val systemDark = isSystemInDarkTheme()
            val isDark = when (mainUiState.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> systemDark
            }

            var selectedColorThemeId by remember { mutableStateOf("purple") }
            val selectedColorTheme = remember(selectedColorThemeId) {
                colorThemes.find { it.id == selectedColorThemeId } ?: colorThemes.first()
            }

            TZTheme(darkTheme = isDark, colorTheme = selectedColorTheme) {
                TZNavHost(
                    selectedColorThemeId = selectedColorThemeId,
                    onColorThemeChanged = { id -> selectedColorThemeId = id }
                )
            }
        }
    }
}
