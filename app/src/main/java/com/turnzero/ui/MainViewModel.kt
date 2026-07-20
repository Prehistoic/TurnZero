package com.turnzero.ui

import android.util.Log
import androidx.compose.runtime.Stable
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turnzero.core.data.UserPreferences
import com.turnzero.core.tools.api.Tool
import com.turnzero.core.tools.api.ToolRegistry
import com.turnzero.core.ui.components.LayoutMode
import com.turnzero.ui.settings.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "TZ/MainVM"

@Stable
data class MainUiState(
    val gamesLayout: LayoutMode = LayoutMode.GRID,
    val toolsLayout: LayoutMode = LayoutMode.GRID,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val currentLanguage: String = "en",
    val favoriteToolIds: Set<String> = emptySet(),
    val selectedTool: Tool? = null,
    val showToolSwitcher: Boolean = false,
    val showToolOptions: Boolean = false,
    val isReady: Boolean = false,
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    val toolRegistry: ToolRegistry,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val prefs = dataStore.data.first()
            _uiState.update {
                val gamesLayout = try {
                    LayoutMode.valueOf(prefs[UserPreferences.LAYOUT_GAMES] ?: "GRID")
                } catch (e: Exception) {
                    Log.w(TAG, "Invalid LAYOUT_GAMES preference", e)
                    LayoutMode.GRID
                }
                val toolsLayout = try {
                    LayoutMode.valueOf(prefs[UserPreferences.LAYOUT_TOOLS] ?: "GRID")
                } catch (e: Exception) {
                    Log.w(TAG, "Invalid LAYOUT_TOOLS preference", e)
                    LayoutMode.GRID
                }
                val themeMode = try {
                    ThemeMode.valueOf((prefs[UserPreferences.THEME_MODE] ?: "system").uppercase())
                } catch (e: Exception) {
                    Log.w(TAG, "Invalid THEME_MODE preference", e)
                    ThemeMode.SYSTEM
                }
                val language = prefs[UserPreferences.LANGUAGE] ?: "en"
                val favorites = prefs[UserPreferences.FAVORITE_TOOLS] ?: emptySet()
                it.copy(
                    gamesLayout = gamesLayout,
                    toolsLayout = toolsLayout,
                    themeMode = themeMode,
                    currentLanguage = language,
                    favoriteToolIds = favorites,
                    isReady = true,
                )
            }
        }
    }

    fun setGamesLayout(mode: LayoutMode) {
        _uiState.update { it.copy(gamesLayout = mode) }
        viewModelScope.launch {
            dataStore.edit { prefs -> prefs[UserPreferences.LAYOUT_GAMES] = mode.name }
        }
    }

    fun setToolsLayout(mode: LayoutMode) {
        _uiState.update { it.copy(toolsLayout = mode) }
        viewModelScope.launch {
            dataStore.edit { prefs -> prefs[UserPreferences.LAYOUT_TOOLS] = mode.name }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _uiState.update { it.copy(themeMode = mode) }
        viewModelScope.launch {
            dataStore.edit { prefs -> prefs[UserPreferences.THEME_MODE] = mode.name.lowercase() }
        }
    }

    fun setLanguage(code: String) {
        _uiState.update { it.copy(currentLanguage = code) }
        viewModelScope.launch {
            dataStore.edit { prefs -> prefs[UserPreferences.LANGUAGE] = code }
        }
    }

    fun toggleFavorite(toolId: String) {
        val current = _uiState.value.favoriteToolIds
        val updated = if (toolId in current) current - toolId else current + toolId
        _uiState.update { it.copy(favoriteToolIds = updated) }
        viewModelScope.launch {
            dataStore.edit { prefs -> prefs[UserPreferences.FAVORITE_TOOLS] = updated }
        }
    }

    fun selectTool(toolId: String?, gameId: String?) {
        val tool = if (toolId != null && gameId != null) {
            toolRegistry.getToolsForGame(gameId).firstOrNull { it.id == toolId }
        } else null
        _uiState.update { it.copy(selectedTool = tool) }
    }

    fun showToolSwitcher(show: Boolean) {
        _uiState.update { it.copy(showToolSwitcher = show, showToolOptions = false) }
    }

    fun showToolOptions(show: Boolean) {
        _uiState.update { it.copy(showToolOptions = show, showToolSwitcher = false) }
    }

    fun dismissAllToolSheets() {
        _uiState.update { it.copy(showToolSwitcher = false, showToolOptions = false) }
    }
}
