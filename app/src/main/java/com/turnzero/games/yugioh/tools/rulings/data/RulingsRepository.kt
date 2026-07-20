package com.turnzero.games.yugioh.tools.rulings.data

import androidx.sqlite.db.SimpleSQLiteQuery

class RulingsRepository(private val dao: RulingsDao) {

    private fun sanitizeFts(query: String): String {
        val cleaned = query.replace(Regex("[\"*+\\-(){}\\[\\]&|^~]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        if (cleaned.isEmpty()) return ""
        return "$cleaned*"
    }

    suspend fun searchCards(query: String, format: String, limit: Int = 20): List<CardSearchResult> {
        val sanitized = sanitizeFts(query)
        if (sanitized.isEmpty()) return emptyList()
        val sqlQuery = SimpleSQLiteQuery(
            "SELECT c.id, c.name, c.type, c.image_url FROM cards_fts fts " +
                "JOIN cards c ON fts.rowid = c.rowid " +
                "WHERE cards_fts MATCH ? " +
                "AND EXISTS (SELECT 1 FROM rulings r WHERE r.card_id = c.id AND r.format = ?) " +
                "ORDER BY fts.rank LIMIT ?",
            arrayOf<Any>(sanitized, format, limit),
        )
        return dao.searchCards(sqlQuery)
    }

    suspend fun getCardRulings(cardId: Int, format: String): CardRulingsDetail? {
        val row = dao.getCardWithRulings(cardId, format) ?: return null
        return CardRulingsDetail(
            id = row.id,
            name = row.name,
            type = row.type,
            desc = row.description?.takeIf { it.isNotBlank() }
                ?: dao.getCard(cardId)?.desc
                ?: "",
            imageUrl = row.imageUrl,
            rulingsText = row.rulingText,
        )
    }

    suspend fun getAllFormats(): List<String> = dao.getAllFormats()
}
