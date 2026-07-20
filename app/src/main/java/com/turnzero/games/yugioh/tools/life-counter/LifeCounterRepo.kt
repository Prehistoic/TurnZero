package com.turnzero.games.yugioh.tools.lifecounter

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.turnzero.core.data.UserPreferences
import com.turnzero.core.data.readEnum
import com.turnzero.core.data.readInt
import com.turnzero.core.data.readJson
import com.turnzero.core.data.writeEnum
import com.turnzero.core.data.writeInt
import com.turnzero.core.data.writeJson
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

    override suspend fun getHistory(): List<LpChange> {
        return dataStore.readJson(UserPreferences.LIFE_COUNTER_HISTORY, emptyList())
    }

    override suspend fun saveHistory(history: List<LpChange>) {
        dataStore.writeJson(UserPreferences.LIFE_COUNTER_HISTORY, history)
    }

    override suspend fun getTimerMode(): TimerMode {
        return dataStore.readEnum(UserPreferences.LIFE_COUNTER_TIMER_MODE, TimerMode.COUNT_DOWN)
    }

    override suspend fun saveTimerMode(mode: TimerMode) {
        dataStore.writeEnum(UserPreferences.LIFE_COUNTER_TIMER_MODE, mode)
    }

    override suspend fun getTimerStartMinutes(): Int {
        return dataStore.readInt(UserPreferences.LIFE_COUNTER_TIMER_START, 30)
    }

    override suspend fun saveTimerStartMinutes(minutes: Int) {
        dataStore.writeInt(UserPreferences.LIFE_COUNTER_TIMER_START, minutes)
    }

    override suspend fun getStartingLp(): Int {
        return dataStore.readInt(UserPreferences.LIFE_COUNTER_STARTING_LP, 8000)
    }

    override suspend fun saveStartingLp(lp: Int) {
        dataStore.writeInt(UserPreferences.LIFE_COUNTER_STARTING_LP, lp)
    }
}
