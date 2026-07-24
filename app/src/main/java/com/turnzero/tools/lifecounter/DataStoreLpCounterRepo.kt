package com.turnzero.tools.lifecounter

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.turnzero.core.data.UserPreferences
import com.turnzero.core.data.readJson
import com.turnzero.core.data.writeJson
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreLpCounterRepo @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : LpCounterRepo {

    override suspend fun getConfig(): LpCounterConfig {
        return dataStore.readJson(UserPreferences.LP_COUNTER_CONFIG, LpCounterConfig())
    }

    override suspend fun saveConfig(config: LpCounterConfig) {
        dataStore.writeJson(UserPreferences.LP_COUNTER_CONFIG, config)
    }
}
