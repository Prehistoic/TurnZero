package com.turnzero.tools.coin

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.turnzero.core.data.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreCoinRepo @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : CoinRepo {

    private val json = Json { ignoreUnknownKeys = true }

    override fun getCoinCount(gameId: String): Flow<Int> {
        val key = UserPreferences.coinCountKey(gameId)
        return dataStore.data.map { prefs ->
            prefs[key]?.toIntOrNull() ?: 1
        }
    }

    override suspend fun saveCoinCount(gameId: String, count: Int) {
        val key = UserPreferences.coinCountKey(gameId)
        dataStore.edit { prefs ->
            prefs[key] = count.toString()
        }
    }

    override fun getHistory(): Flow<List<CoinFlip>> {
        return dataStore.data.map { prefs ->
            val raw = prefs[UserPreferences.COIN_HISTORY] ?: return@map emptyList()
            try {
                json.decodeFromString<List<CoinFlip>>(raw)
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    override suspend fun saveHistory(history: List<CoinFlip>) {
        val capped = history.take(10)
        dataStore.edit { prefs ->
            prefs[UserPreferences.COIN_HISTORY] = json.encodeToString(capped)
        }
    }
}
