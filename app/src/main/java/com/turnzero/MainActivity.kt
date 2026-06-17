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
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.turnzero.core.tools.api.ToolRegistry
import com.turnzero.core.ui.theme.TZTheme
import com.turnzero.navigation.TZNavHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var toolRegistry: ToolRegistry

    @Inject
    lateinit var dataStore: DataStore<Preferences>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var isDark by remember { mutableStateOf(isSystemInDarkTheme()) }

            TZTheme(darkTheme = isDark) {
                TZNavHost(
                    toolRegistry = toolRegistry,
                    dataStore = dataStore,
                    isDarkTheme = isDark,
                    onThemeChanged = { dark -> isDark = dark }
                )
            }
        }
    }
}
