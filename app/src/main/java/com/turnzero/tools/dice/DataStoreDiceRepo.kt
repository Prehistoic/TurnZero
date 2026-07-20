package com.turnzero.tools.dice

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.turnzero.core.data.UserPreferences
import com.turnzero.core.data.readEnum
import com.turnzero.core.data.readJson
import com.turnzero.core.data.writeEnum
import com.turnzero.core.data.writeJson
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreDiceRepo @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : DiceRepo {

    override suspend fun getPool(gameId: String): List<Die> {
        return dataStore.readJson(
            UserPreferences.dicePoolKey(gameId),
            listOf(Die("d1", 6, DiceColor.WHITE))
        )
    }

    override suspend fun savePool(gameId: String, dice: List<Die>) {
        dataStore.writeJson(UserPreferences.dicePoolKey(gameId), dice)
    }

    override suspend fun getSelectedColor(): DiceColor {
        return dataStore.readEnum(UserPreferences.DICE_SELECTED_COLOR, DiceColor.WHITE)
    }

    override suspend fun saveSelectedColor(color: DiceColor) {
        dataStore.writeEnum(UserPreferences.DICE_SELECTED_COLOR, color)
    }

    override suspend fun getHistory(): List<DiceRoll> {
        return dataStore.readJson(UserPreferences.DICE_HISTORY, emptyList())
    }

    override suspend fun saveHistory(history: List<DiceRoll>) {
        dataStore.writeJson(UserPreferences.DICE_HISTORY, history.take(10))
    }
}
