package com.turnzero.tools.dice

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.turnzero.core.data.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreDiceRepo @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : DiceRepo {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getPool(gameId: String): List<Die> {
        val key = UserPreferences.dicePoolKey(gameId)
        val raw = dataStore.data.map { it[key] }.first() ?: return listOf(Die("d1", 6, DiceColor.WHITE))
        return try {
            json.decodeFromString<List<Die>>(raw)
        } catch (_: Exception) {
            listOf(Die("d1", 6, DiceColor.WHITE))
        }
    }

    override suspend fun savePool(gameId: String, dice: List<Die>) {
        val key = UserPreferences.dicePoolKey(gameId)
        dataStore.edit { prefs ->
            prefs[key] = json.encodeToString(dice)
        }
    }

    override suspend fun getSelectedColor(): DiceColor {
        val raw = dataStore.data.map { it[UserPreferences.DICE_SELECTED_COLOR] }.first()
        return raw?.let { name ->
            try { DiceColor.valueOf(name) } catch (_: Exception) { DiceColor.WHITE }
        } ?: DiceColor.WHITE
    }

    override suspend fun saveSelectedColor(color: DiceColor) {
        dataStore.edit { prefs ->
            prefs[UserPreferences.DICE_SELECTED_COLOR] = color.name
        }
    }

    override suspend fun getHistory(): List<DiceRoll> {
        val raw = dataStore.data.map { it[UserPreferences.DICE_HISTORY] }.first() ?: return emptyList()
        return try {
            json.decodeFromString<List<DiceRoll>>(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun saveHistory(history: List<DiceRoll>) {
        val capped = history.take(10)
        dataStore.edit { prefs ->
            prefs[UserPreferences.DICE_HISTORY] = json.encodeToString(capped)
        }
    }
}
