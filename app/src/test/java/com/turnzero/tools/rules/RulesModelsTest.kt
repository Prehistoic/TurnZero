package com.turnzero.tools.rules

import org.junit.Assert.*
import org.junit.Test

class RulesModelsTest {

    private val sampleKeywords = listOf(
        Keyword("Piercing Battle Damage", listOf(ContentBlock.Paragraph("Inflicts difference as damage")), "Damage Calculation"),
        Keyword("Quick Effect", listOf(ContentBlock.Paragraph("Spell Speed 2 effect")), "Effect Types"),
        Keyword("Tribute Summon", listOf(ContentBlock.Paragraph("Requires tributing monsters")), "Summon"),
    )

    private val sampleConfig = RulesConfig(
        rulesetId = "tcg-advanced",
        rulesetName = "Advanced (TCG)",
        setup = listOf(
            RuleStep("Shuffle", "Shuffle your deck", listOf(ContentBlock.Paragraph("Full shuffle details..."))),
            RuleStep("Draw", "Draw 5 cards", listOf(ContentBlock.Paragraph("Full draw details..."))),
        ),
        victoryRules = listOf(
            InfoCard("Reduce LP", listOf(ContentBlock.Paragraph("Reduce opponent LP to 0"))),
        ),
        playSequence = listOf(
            RuleStep("Draw Phase", "Draw 1 card", listOf(ContentBlock.Paragraph("Full draw phase details..."))),
        ),
        keywords = sampleKeywords,
    )

    private val configWithoutKeywords = sampleConfig.copy(keywords = null)

    @Test
    fun `filteredKeywords returns alphabetically sorted keywords when query is blank`() {
        val state = RulesUiState(
            configs = listOf(sampleConfig),
        )
        val result = state.filteredKeywords
        assertEquals("Piercing Battle Damage", result[0].name)
        assertEquals("Quick Effect", result[1].name)
        assertEquals("Tribute Summon", result[2].name)
    }

    @Test
    fun `filteredKeywords filters by name case-insensitively`() {
        val state = RulesUiState(
            configs = listOf(sampleConfig),
            keywordSearchQuery = "effect",
        )
        val result = state.filteredKeywords
        assertEquals(1, result.size)
        assertEquals("Quick Effect", result[0].name)
    }

    @Test
    fun `filteredKeywords returns empty list when no match`() {
        val state = RulesUiState(
            configs = listOf(sampleConfig),
            keywordSearchQuery = "zzznotfound",
        )
        assertEquals(0, state.filteredKeywords.size)
    }

    @Test
    fun `filteredKeywords returns empty list when keywords is null`() {
        val state = RulesUiState(
            configs = listOf(configWithoutKeywords),
        )
        assertTrue(state.filteredKeywords.isEmpty())
    }

    @Test
    fun `visibleTabs includes keywords tab when keywords is not null`() {
        val state = RulesUiState(configs = listOf(sampleConfig))
        val tabs = state.visibleTabs
        assertEquals(4, tabs.size)
        assertEquals("setup", tabs[0].key)
        assertEquals("turn", tabs[1].key)
        assertEquals("victory", tabs[2].key)
        assertEquals("keywords", tabs[3].key)
    }

    @Test
    fun `visibleTabs excludes keywords tab when keywords is null`() {
        val state = RulesUiState(configs = listOf(configWithoutKeywords))
        val tabs = state.visibleTabs
        assertEquals(3, tabs.size)
        assertEquals("setup", tabs[0].key)
        assertEquals("turn", tabs[1].key)
        assertEquals("victory", tabs[2].key)
    }

    @Test
    fun `visibleTabs includes deck tab when deckRules is not null`() {
        val configWithDeck = sampleConfig.copy(
            deckRules = listOf(InfoCard("Deck rules", listOf(ContentBlock.Paragraph("Rules"))))
        )
        val state = RulesUiState(configs = listOf(configWithDeck))
        val tabs = state.visibleTabs
        assertEquals(5, tabs.size)
        assertEquals("setup", tabs[0].key)
        assertEquals("turn", tabs[1].key)
        assertEquals("victory", tabs[2].key)
        assertEquals("deck", tabs[3].key)
        assertEquals("keywords", tabs[4].key)
    }

    @Test
    fun `visibleTabs includes deck and field in correct order`() {
        val configWithAll = sampleConfig.copy(
            deckRules = listOf(InfoCard("Deck", listOf(ContentBlock.Paragraph("Rules")))),
            fieldContent = listOf(ContentBlock.Paragraph("Field rules")),
        )
        val state = RulesUiState(configs = listOf(configWithAll))
        val tabs = state.visibleTabs
        assertEquals(6, tabs.size)
        assertEquals("setup", tabs[0].key)
        assertEquals("turn", tabs[1].key)
        assertEquals("victory", tabs[2].key)
        assertEquals("deck", tabs[3].key)
        assertEquals("field", tabs[4].key)
        assertEquals("keywords", tabs[5].key)
    }

    @Test
    fun `visibleTabs returns empty list when no configs`() {
        val state = RulesUiState()
        assertTrue(state.visibleTabs.isEmpty())
    }

    @Test
    fun `currentConfig returns config at selected index`() {
        val configs = listOf(
            sampleConfig.copy(rulesetId = "a"),
            sampleConfig.copy(rulesetId = "b"),
        )
        val state = RulesUiState(configs = configs, selectedRulesetIndex = 1)
        assertEquals("b", state.currentConfig?.rulesetId)
    }

    @Test
    fun `currentConfig returns null when index out of bounds`() {
        val state = RulesUiState(configs = listOf(sampleConfig), selectedRulesetIndex = 5)
        assertNull(state.currentConfig)
    }

    @Test
    fun `visibleTabs excludes keywords tab when keywords is empty list`() {
        val configWithEmptyKeywords = sampleConfig.copy(keywords = emptyList())
        val state = RulesUiState(configs = listOf(configWithEmptyKeywords))
        val tabs = state.visibleTabs
        assertEquals(3, tabs.size)
        assertEquals("setup", tabs[0].key)
        assertEquals("turn", tabs[1].key)
        assertEquals("victory", tabs[2].key)
    }

    @Test
    fun `filteredKeywords maintains sort order when filtered`() {
        val keywordsWithMultipleMatches = listOf(
            Keyword("Zeta Effect", listOf(ContentBlock.Paragraph("")), null),
            Keyword("Quick Effect", listOf(ContentBlock.Paragraph("")), null),
            Keyword("Alpha Effect", listOf(ContentBlock.Paragraph("")), null),
        )
        val configWithMany = sampleConfig.copy(keywords = keywordsWithMultipleMatches)
        val state = RulesUiState(
            configs = listOf(configWithMany),
            keywordSearchQuery = "effect",
        )
        val result = state.filteredKeywords
        assertEquals(3, result.size)
        assertEquals("Alpha Effect", result[0].name)
        assertEquals("Quick Effect", result[1].name)
        assertEquals("Zeta Effect", result[2].name)
    }

    @Test
    fun `filteredKeywords filters by selected category`() {
        val state = RulesUiState(
            configs = listOf(sampleConfig),
            selectedKeywordCategory = "Summon",
        )
        val result = state.filteredKeywords
        assertEquals(1, result.size)
        assertEquals("Tribute Summon", result[0].name)
    }

    @Test
    fun `allKeywordCategories returns distinct sorted categories`() {
        val state = RulesUiState(configs = listOf(sampleConfig))
        val categories = state.allKeywordCategories
        assertEquals(listOf("Damage Calculation", "Effect Types", "Summon"), categories)
    }
}
