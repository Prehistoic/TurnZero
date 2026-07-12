package com.turnzero.games.yugioh.tools.rulings

import com.turnzero.games.yugioh.tools.rulings.data.CardDescriptionEntity
import com.turnzero.games.yugioh.tools.rulings.data.CardEntity
import com.turnzero.games.yugioh.tools.rulings.data.CardRulingsDetail
import com.turnzero.games.yugioh.tools.rulings.data.CardSearchResult
import com.turnzero.games.yugioh.tools.rulings.data.RulingEntity
import com.turnzero.games.yugioh.tools.rulings.data.RulingsDao
import com.turnzero.games.yugioh.tools.rulings.data.RulingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
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
        coEvery { dao.getCard(999) } returns null

        val result = repository.getCardRulings(999, "Goat")

        assertNull(result)
    }

    @Test
    fun `getCardRulings returns null when ruling not found`() = runTest {
        coEvery { dao.getCard(1) } returns CardEntity(1, "Pot of Greed", "Spell Card", "spell", null, null, null, "Normal", null, "Draw 2 cards.", null)
        coEvery { dao.getRuling(1, "Goat") } returns null

        val result = repository.getCardRulings(1, "Goat")

        assertNull(result)
    }

    @Test
    fun `getCardRulings uses format-specific description when available`() = runTest {
        val card = CardEntity(1, "Pot of Greed", "Spell Card", "spell", null, null, null, "Normal", null, "Draw 2 cards.", null)
        val ruling = RulingEntity(1, "Goat", "Pot of Greed rulings...")
        val desc = CardDescriptionEntity(1, "Goat", "Draw 2 cards (Goat format).")
        coEvery { dao.getCard(1) } returns card
        coEvery { dao.getRuling(1, "Goat") } returns ruling
        coEvery { dao.getCardDescription(1, "Goat") } returns desc

        val result = repository.getCardRulings(1, "Goat")

        assertNotNull(result)
        assertEquals("Draw 2 cards (Goat format).", result!!.desc)
    }

    @Test
    fun `getCardRulings falls back to card desc when no format-specific description`() = runTest {
        val card = CardEntity(1, "Pot of Greed", "Spell Card", "spell", null, null, null, "Normal", null, "Draw 2 cards.", null)
        val ruling = RulingEntity(1, "Goat", "Pot of Greed rulings...")
        coEvery { dao.getCard(1) } returns card
        coEvery { dao.getRuling(1, "Goat") } returns ruling
        coEvery { dao.getCardDescription(1, "Goat") } returns null

        val result = repository.getCardRulings(1, "Goat")

        assertNotNull(result)
        assertEquals("Draw 2 cards.", result!!.desc)
    }

    @Test
    fun `getCardRulings falls back to card desc when format desc is blank`() = runTest {
        val card = CardEntity(1, "Pot of Greed", "Spell Card", "spell", null, null, null, "Normal", null, "Draw 2 cards.", null)
        val ruling = RulingEntity(1, "Goat", "Pot of Greed rulings...")
        val desc = CardDescriptionEntity(1, "Goat", "")
        coEvery { dao.getCard(1) } returns card
        coEvery { dao.getRuling(1, "Goat") } returns ruling
        coEvery { dao.getCardDescription(1, "Goat") } returns desc

        val result = repository.getCardRulings(1, "Goat")

        assertNotNull(result)
        assertEquals("Draw 2 cards.", result!!.desc)
    }

    @Test
    fun `getCardRulings returns detail when both card and ruling exist`() = runTest {
        val card = CardEntity(1, "Pot of Greed", "Spell Card", "spell", null, null, null, "Normal", null, "Draw 2 cards.", null)
        val ruling = RulingEntity(1, "Goat", "Pot of Greed rulings...")
        coEvery { dao.getCard(1) } returns card
        coEvery { dao.getRuling(1, "Goat") } returns ruling
        coEvery { dao.getCardDescription(1, "Goat") } returns null

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
