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
import com.turnzero.core.ui.theme.TZTheme
import com.turnzero.navigation.TZNavHost

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val systemDark = isSystemInDarkTheme()
            var isDark by remember { mutableStateOf(systemDark) }

            TZTheme(darkTheme = isDark) {
                TZNavHost(
                    toolRegistry = ServiceLocator.toolRegistry,
                    dataStore = ServiceLocator.dataStore,
                    isDarkTheme = isDark,
                    onThemeChanged = { isDark = it }
                )
            }
        }
    }
}
