package com.turnzero.games.yugioh.tools.rulings.data

import androidx.sqlite.db.SimpleSQLiteQuery

class RulingsRepository(private val dao: RulingsDao) {

    suspend fun searchCards(query: String, format: String, limit: Int = 20): List<CardSearchResult> {
        val sanitized = query.trim()
        if (sanitized.isEmpty()) return emptyList()
        val sqlQuery = SimpleSQLiteQuery(
            "SELECT c.id, c.name, c.type, c.image_url FROM cards_fts fts " +
                "JOIN cards c ON fts.rowid = c.rowid " +
                "WHERE cards_fts MATCH ? " +
                "AND EXISTS (SELECT 1 FROM rulings r WHERE r.card_id = c.id AND r.format = ?) " +
                "UNION " +
                "SELECT c.id, c.name, c.type, c.image_url FROM names_fts nf " +
                "JOIN localized_names ln ON nf.rowid = ln.rowid " +
                "JOIN cards c ON ln.card_id = c.id " +
                "WHERE names_fts MATCH ? " +
                "AND EXISTS (SELECT 1 FROM rulings r WHERE r.card_id = c.id AND r.format = ?) " +
                "LIMIT ?",
            arrayOf<Any>("$sanitized*", format, "$sanitized*", format, limit),
        )
        return dao.searchCards(sqlQuery)
    }

    suspend fun getCardRulings(cardId: Int, format: String): CardRulingsDetail? {
        val card = dao.getCard(cardId) ?: return null
        val ruling = dao.getRuling(cardId, format) ?: return null
        return CardRulingsDetail(
            id = card.id,
            name = card.name,
            type = card.type,
            desc = card.desc,
            imageUrl = card.imageUrl,
            rulingsText = ruling.rulingText,
        )
    }

    suspend fun getAllFormats(): List<String> = dao.getAllFormats()
}
