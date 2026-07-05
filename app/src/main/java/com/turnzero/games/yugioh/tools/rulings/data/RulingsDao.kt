package com.turnzero.games.yugioh.tools.rulings.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery

@Dao
interface RulingsDao {

    @RawQuery(observedEntities = [CardEntity::class])
    suspend fun searchCards(query: SupportSQLiteQuery): List<CardSearchResult>

    @Query("SELECT * FROM cards WHERE id = :cardId")
    suspend fun getCard(cardId: Int): CardEntity?

    @Query("SELECT * FROM rulings WHERE card_id = :cardId AND format = :format")
    suspend fun getRuling(cardId: Int, format: String): RulingEntity?

    @Query("SELECT DISTINCT format FROM rulings ORDER BY format")
    suspend fun getAllFormats(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRuling(ruling: RulingEntity)
}
