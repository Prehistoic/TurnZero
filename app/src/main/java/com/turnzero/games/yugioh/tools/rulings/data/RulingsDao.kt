package com.turnzero.games.yugioh.tools.rulings.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery

data class CardRulingsRow(
    val id: Int,
    val name: String,
    val type: String,
    val imageUrl: String?,
    val rulingText: String,
    val description: String?,
)

@Dao
interface RulingsDao {

    @RawQuery(observedEntities = [CardEntity::class])
    suspend fun searchCards(query: SupportSQLiteQuery): List<CardSearchResult>

    @Query("SELECT * FROM cards WHERE id = :cardId")
    suspend fun getCard(cardId: Int): CardEntity?

    @Query("SELECT * FROM card_descriptions WHERE card_id = :cardId AND format = :format")
    suspend fun getCardDescription(cardId: Int, format: String): CardDescriptionEntity?

    @Query("SELECT * FROM rulings WHERE card_id = :cardId AND format = :format")
    suspend fun getRuling(cardId: Int, format: String): RulingEntity?

    @Query("""
        SELECT c.id, c.name, c.type, c.image_url AS imageUrl,
               r.ruling_text AS rulingText,
               cd.description
        FROM cards c
        INNER JOIN rulings r ON c.id = r.card_id AND r.format = :format
        LEFT JOIN card_descriptions cd ON c.id = cd.card_id AND cd.format = :format
        WHERE c.id = :cardId
    """)
    suspend fun getCardWithRulings(cardId: Int, format: String): CardRulingsRow?

    @Query("SELECT DISTINCT format FROM rulings ORDER BY format")
    suspend fun getAllFormats(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRuling(ruling: RulingEntity)
}
