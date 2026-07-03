package com.turnzero.tools.rules

import org.junit.Assert.*
import org.junit.Test

class RulesModelsTest {

    private val sampleKeywords = listOf(
        Keyword("Piercing Battle Damage", "Inflicts difference as damage", "Damage Calculation"),
        Keyword("Quick Effect", "Spell Speed 2 effect", "Effect Types"),
        Keyword("Tribute Summon", "Requires tributing monsters", "Summon"),
    )

    private val sampleConfig = RulesConfig(
        rulesetId = "tcg-advanced",
        rulesetName = "Advanced (TCG)",
        setup = listOf(
            RuleStep("Shuffle", "Shuffle your deck", "Full shuffle details..."),
            RuleStep("Draw", "Draw 5 cards", "Full draw details..."),
        ),
        winCons = listOf(
            WinCondition("Reduce LP", "Reduce opponent LP to 0"),
        ),
        playSequence = listOf(
            RuleStep("Draw Phase", "Draw 1 card", "Full draw phase details..."),
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
        assertEquals("wincons", tabs[1].key)
        assertEquals("sequence", tabs[2].key)
        assertEquals("keywords", tabs[3].key)
    }

    @Test
    fun `visibleTabs excludes keywords tab when keywords is null`() {
        val state = RulesUiState(configs = listOf(configWithoutKeywords))
        val tabs = state.visibleTabs
        assertEquals(3, tabs.size)
        assertEquals("setup", tabs[0].key)
        assertEquals("wincons", tabs[1].key)
        assertEquals("sequence", tabs[2].key)
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
        assertEquals("wincons", tabs[1].key)
        assertEquals("sequence", tabs[2].key)
    }

    @Test
    fun `filteredKeywords maintains sort order when filtered`() {
        val keywordsWithMultipleMatches = listOf(
            Keyword("Zeta Effect", ""),
            Keyword("Quick Effect", ""),
            Keyword("Alpha Effect", ""),
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
}
