package com.turnzero.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "turnzero_prefs")

object UserPreferences {
    val LAYOUT_GAMES = stringPreferencesKey("layout_games")
    val LAYOUT_TOOLS = stringPreferencesKey("layout_tools")
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val COLOR_THEME = stringPreferencesKey("color_theme")
    val LANGUAGE = stringPreferencesKey("language")
    val FAVORITE_TOOLS = stringSetPreferencesKey("favorite_tools")
    val DICE_SELECTED_COLOR = stringPreferencesKey("dice_selected_color")
    val DICE_HISTORY = stringPreferencesKey("dice_history")

    fun dicePoolKey(gameId: String) = stringPreferencesKey("dice_pool_$gameId")
}
