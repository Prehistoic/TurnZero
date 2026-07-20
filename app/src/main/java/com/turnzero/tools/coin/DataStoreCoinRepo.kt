package com.turnzero.tools.coin

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.turnzero.core.data.UserPreferences
import com.turnzero.core.data.observeInt
import com.turnzero.core.data.observeJson
import com.turnzero.core.data.writeInt
import com.turnzero.core.data.writeJson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.serialization.builtins.ListSerializer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreCoinRepo @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : CoinRepo {

    override fun getCoinCount(gameId: String): Flow<Int> {
        return dataStore.observeInt(UserPreferences.coinCountKey(gameId), 1)
    }

    override suspend fun saveCoinCount(gameId: String, count: Int) {
        dataStore.writeInt(UserPreferences.coinCountKey(gameId), count)
    }

    override fun getHistory(): Flow<List<CoinFlip>> {
        return dataStore.observeJson(
            UserPreferences.COIN_HISTORY,
            emptyList(),
            ListSerializer(CoinFlip.serializer())
        ).distinctUntilChanged()
    }

    override suspend fun saveHistory(history: List<CoinFlip>) {
        dataStore.writeJson(UserPreferences.COIN_HISTORY, history.take(10))
    }
}
