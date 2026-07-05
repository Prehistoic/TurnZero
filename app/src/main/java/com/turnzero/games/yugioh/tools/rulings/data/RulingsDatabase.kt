package com.turnzero.games.yugioh.tools.rulings.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CardEntity::class, RulingEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class RulingsDatabase : RoomDatabase() {
    abstract fun rulingsDao(): RulingsDao
}
