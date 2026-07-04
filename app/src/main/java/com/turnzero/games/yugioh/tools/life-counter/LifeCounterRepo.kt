package com.turnzero.games.yugioh.tools.lifecounter

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

interface LifeCounterRepo {
    suspend fun getHistory(): List<LpChange>
    suspend fun saveHistory(history: List<LpChange>)
    suspend fun getTimerMode(): TimerMode
    suspend fun saveTimerMode(mode: TimerMode)
    suspend fun getTimerStartMinutes(): Int
    suspend fun saveTimerStartMinutes(minutes: Int)
    suspend fun getStartingLp(): Int
    suspend fun saveStartingLp(lp: Int)
}

@Singleton
class DataStoreLifeCounterRepo @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : LifeCounterRepo {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getHistory(): List<LpChange> {
        val raw = dataStore.data.map { it[UserPreferences.LIFE_COUNTER_HISTORY] }.first()
            ?: return emptyList()
        return try {
            json.decodeFromString<List<LpChange>>(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun saveHistory(history: List<LpChange>) {
        dataStore.edit { prefs ->
            prefs[UserPreferences.LIFE_COUNTER_HISTORY] = json.encodeToString(history)
        }
    }

    override suspend fun getTimerMode(): TimerMode {
        val raw = dataStore.data.map { it[UserPreferences.LIFE_COUNTER_TIMER_MODE] }.first()
        return raw?.let {
            try { TimerMode.valueOf(it) } catch (_: Exception) { TimerMode.COUNT_DOWN }
        } ?: TimerMode.COUNT_DOWN
    }

    override suspend fun saveTimerMode(mode: TimerMode) {
        dataStore.edit { prefs ->
            prefs[UserPreferences.LIFE_COUNTER_TIMER_MODE] = mode.name
        }
    }

    override suspend fun getTimerStartMinutes(): Int {
        val raw = dataStore.data.map { it[UserPreferences.LIFE_COUNTER_TIMER_START] }.first()
        return raw?.toIntOrNull() ?: 30
    }

    override suspend fun saveTimerStartMinutes(minutes: Int) {
        dataStore.edit { prefs ->
            prefs[UserPreferences.LIFE_COUNTER_TIMER_START] = minutes.toString()
        }
    }

    override suspend fun getStartingLp(): Int {
        val raw = dataStore.data.map { it[UserPreferences.LIFE_COUNTER_STARTING_LP] }.first()
        return raw?.toIntOrNull() ?: 8000
    }

    override suspend fun saveStartingLp(lp: Int) {
        dataStore.edit { prefs ->
            prefs[UserPreferences.LIFE_COUNTER_STARTING_LP] = lp.toString()
        }
    }
}
