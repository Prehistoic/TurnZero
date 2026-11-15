package com.turnzero.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
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

    val COIN_HISTORY = stringPreferencesKey("coin_history")
    val LP_COUNTER_CONFIG = stringPreferencesKey("lp_counter_config")
    val LIFE_COUNTER_HISTORY = stringPreferencesKey("life_counter_history")
    val LIFE_COUNTER_TIMER_MODE = stringPreferencesKey("life_counter_timer_mode")
    val LIFE_COUNTER_TIMER_START = stringPreferencesKey("life_counter_timer_start")
    val LIFE_COUNTER_STARTING_LP = stringPreferencesKey("life_counter_starting_lp")
    val RULINGS_ENABLED_FORMATS = stringSetPreferencesKey("rulings_enabled_formats")
    val RULINGS_DEFAULT_FORMAT = stringPreferencesKey("rulings_default_format")
    val RULES_LAYOUT = stringPreferencesKey("rules_layout")

    fun rulesSpecStringKey(gameId: String, specId: String) =
        stringPreferencesKey("rules_spec_string_${gameId}_$specId")

    fun rulesSpecIntKey(gameId: String, specId: String) =
        intPreferencesKey("rules_spec_int_${gameId}_$specId")

    fun rulesSpecSetKey(gameId: String, specId: String) =
        stringSetPreferencesKey("rules_spec_set_${gameId}_$specId")

    fun rulesConfiguredKey(gameId: String) = booleanPreferencesKey("rules_configured_$gameId")

    fun rulesSubToolKey(gameId: String) = stringPreferencesKey("rules_subtool_$gameId")

    fun coinCountKey(gameId: String) = stringPreferencesKey("coin_count_$gameId")
}
