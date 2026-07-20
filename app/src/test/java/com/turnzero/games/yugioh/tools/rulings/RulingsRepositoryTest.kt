package com.turnzero.games.yugioh.tools.rulings

import com.turnzero.games.yugioh.tools.rulings.data.CardEntity
import com.turnzero.games.yugioh.tools.rulings.data.CardRulingsDetail
import com.turnzero.games.yugioh.tools.rulings.data.CardRulingsRow
import com.turnzero.games.yugioh.tools.rulings.data.CardSearchResult
import com.turnzero.games.yugioh.tools.rulings.data.RulingsDao
import com.turnzero.games.yugioh.tools.rulings.data.RulingsRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RulingsRepositoryTest {

    private val dao: RulingsDao = mockk()
    private val repository = RulingsRepository(dao)

    @Test
    fun `searchCards delegates to dao and returns results`() = runTest {
        val expected = listOf(CardSearchResult(1, "Pot of Greed", "Spell Card", null))
        coEvery { dao.searchCards(any()) } returns expected

        val results = repository.searchCards("pot", "Goat")

        assertEquals(1, results.size)
        assertEquals("Pot of Greed", results[0].name)
    }

    @Test
    fun `searchCards returns empty when query is blank`() = runTest {
        val results = repository.searchCards("  ", "Goat")
        assertEquals(0, results.size)
    }

    @Test
    fun `getCardRulings returns null when card not found`() = runTest {
        coEvery { dao.getCardWithRulings(999, "Goat") } returns null

        val result = repository.getCardRulings(999, "Goat")

        assertNull(result)
    }

    @Test
    fun `getCardRulings returns null when ruling not found`() = runTest {
        coEvery { dao.getCardWithRulings(1, "Goat") } returns null

        val result = repository.getCardRulings(1, "Goat")

        assertNull(result)
    }

    @Test
    fun `getCardRulings uses format-specific description when available`() = runTest {
        val row = CardRulingsRow(
            id = 1, name = "Pot of Greed", type = "Spell Card", imageUrl = null,
            rulingText = "Pot of Greed rulings...",
            description = "Draw 2 cards (Goat format).",
        )
        coEvery { dao.getCardWithRulings(1, "Goat") } returns row

        val result = repository.getCardRulings(1, "Goat")

        assertNotNull(result)
        assertEquals("Draw 2 cards (Goat format).", result!!.desc)
    }

    @Test
    fun `getCardRulings falls back to card desc when no format-specific description`() = runTest {
        val row = CardRulingsRow(
            id = 1, name = "Pot of Greed", type = "Spell Card", imageUrl = null,
            rulingText = "Pot of Greed rulings...",
            description = null,
        )
        val card = CardEntity(1, "Pot of Greed", "Spell Card", "spell", null, null, null, "Normal", null, "Draw 2 cards.", null)
        coEvery { dao.getCardWithRulings(1, "Goat") } returns row
        coEvery { dao.getCard(1) } returns card

        val result = repository.getCardRulings(1, "Goat")

        assertNotNull(result)
        assertEquals("Draw 2 cards.", result!!.desc)
    }

    @Test
    fun `getCardRulings falls back to card desc when format desc is blank`() = runTest {
        val row = CardRulingsRow(
            id = 1, name = "Pot of Greed", type = "Spell Card", imageUrl = null,
            rulingText = "Pot of Greed rulings...",
            description = "",
        )
        val card = CardEntity(1, "Pot of Greed", "Spell Card", "spell", null, null, null, "Normal", null, "Draw 2 cards.", null)
        coEvery { dao.getCardWithRulings(1, "Goat") } returns row
        coEvery { dao.getCard(1) } returns card

        val result = repository.getCardRulings(1, "Goat")

        assertNotNull(result)
        assertEquals("Draw 2 cards.", result!!.desc)
    }

    @Test
    fun `getCardRulings returns detail when both card and ruling exist`() = runTest {
        val row = CardRulingsRow(
            id = 1, name = "Pot of Greed", type = "Spell Card", imageUrl = null,
            rulingText = "Pot of Greed rulings...",
            description = null,
        )
        val card = CardEntity(1, "Pot of Greed", "Spell Card", "spell", null, null, null, "Normal", null, "Draw 2 cards.", null)
        coEvery { dao.getCardWithRulings(1, "Goat") } returns row
        coEvery { dao.getCard(1) } returns card

        val result = repository.getCardRulings(1, "Goat")

        assertNotNull(result)
        assertEquals("Pot of Greed", result!!.name)
        assertEquals("Pot of Greed rulings...", result.rulingsText)
    }

    @Test
    fun `getAllFormats delegates to dao`() = runTest {
        coEvery { dao.getAllFormats() } returns listOf("Edison", "Goat")

        val formats = repository.getAllFormats()

        assertEquals(listOf("Edison", "Goat"), formats)
    }
}
