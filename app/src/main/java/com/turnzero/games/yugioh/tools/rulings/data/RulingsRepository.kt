package com.turnzero.games.yugioh.tools.rulings.data

import androidx.sqlite.db.SimpleSQLiteQuery
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.math.min

data class FuzzyCardResult(
    val id: Int,
    val name: String,
    val score: Int,
)

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
                "UNION " +
                "SELECT c.id, c.name, c.type, c.image_url FROM names_fts nf " +
                "JOIN localized_names ln ON nf.rowid = ln.rowid " +
                "JOIN cards c ON ln.card_id = c.id " +
                "WHERE names_fts MATCH ? " +
                "AND EXISTS (SELECT 1 FROM rulings r WHERE r.card_id = c.id AND r.format = ?) " +
                "LIMIT ?",
            arrayOf<Any>(sanitized, format, sanitized, format, limit),
        )
        return dao.searchCards(sqlQuery)
    }

    suspend fun fuzzySearch(query: String, format: String): List<FuzzyCardResult> {
        val words = query.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        if (words.isEmpty()) return emptyList()

        val results = coroutineScope {
            words.filter { it.length > 2 }.map { word ->
                async { searchCards(word, format, 10) }
            }.awaitAll()
        }
        val allResults = results.flatten().distinctBy { it.id }

        val normalizedQuery = query.uppercase().replace(Regex("[^A-Z0-9\\s]"), "").trim()
        return allResults
            .map { result ->
                val normalizedName = result.name.uppercase().replace(Regex("[^A-Z0-9\\s]"), "").trim()
                val score = levenshteinDistance(normalizedQuery, normalizedName)
                FuzzyCardResult(result.id, result.name, score)
            }
            .sortedBy { it.score }
            .filter { it.score <= min(normalizedQuery.length / 2, 5) }
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

    companion object {
        fun levenshteinDistance(a: String, b: String): Int {
            if (a.isEmpty()) return b.length
            if (b.isEmpty()) return a.length

            var prev = IntArray(b.length + 1) { it }
            var curr = IntArray(b.length + 1)

            for (i in 1..a.length) {
                curr[0] = i
                for (j in 1..b.length) {
                    curr[j] = if (a[i - 1] == b[j - 1]) {
                        prev[j - 1]
                    } else {
                        1 + minOf(prev[j], curr[j - 1], prev[j - 1])
                    }
                }
                val temp = prev
                prev = curr
                curr = temp
            }
            return prev[b.length]
        }
    }
}
