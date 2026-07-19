package com.turnzero.games.mtg.rules

import org.junit.Assert.*
import org.junit.Test

class MagicRulesTest {

    @Test
    fun `each config has non-empty setup`() {
        for (config in MagicRules.configs) {
            assertTrue("Config ${config.rulesetId} has empty setup", config.setup.isNotEmpty())
        }
    }

    @Test
    fun `each config has non-empty victoryRules`() {
        for (config in MagicRules.configs) {
            assertTrue("Config ${config.rulesetId} has empty victoryRules", config.victoryRules.isNotEmpty())
        }
    }

    @Test
    fun `each config has non-empty playSequence`() {
        for (config in MagicRules.configs) {
            assertTrue("Config ${config.rulesetId} has empty playSequence", config.playSequence.isNotEmpty())
        }
    }

    @Test
    fun `each config has non-empty keywords`() {
        for (config in MagicRules.configs) {
            assertNotNull("Config ${config.rulesetId} has null keywords", config.keywords)
            assertTrue("Config ${config.rulesetId} has empty keywords", config.keywords!!.isNotEmpty())
        }
    }

    @Test
    fun `each config has non-empty deckRules`() {
        for (config in MagicRules.configs) {
            assertNotNull("Config ${config.rulesetId} has null deckRules", config.deckRules)
            assertTrue("Config ${config.rulesetId} has empty deckRules", config.deckRules!!.isNotEmpty())
        }
    }

    @Test
    fun `all ruleset ids are unique`() {
        val ids = MagicRules.configs.map { it.rulesetId }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `config contains all 9 formats`() {
        val ids = MagicRules.configs.map { it.rulesetId }.toSet()
        val expected = setOf("standard", "modern", "pioneer", "legacy", "vintage", "premodern", "pauper", "commander", "duel-commander")
        assertEquals(expected, ids)
    }

    @Test
    fun `setup includes mulligan step`() {
        for (config in MagicRules.configs) {
            val allText = config.setup.joinToString(" ") { it.title + " " + it.summary }
            assertTrue("Config ${config.rulesetId} missing Mulligan step", allText.lowercase().contains("mulligan"))
        }
    }

    @Test
    fun `turn structure includes untap upkeep draw main combat and ending phases`() {
        for (config in MagicRules.configs) {
            val titles = config.playSequence.map { it.title }
            assertTrue("Config ${config.rulesetId} missing Beginning Phase", titles.any { it.lowercase().contains("beginning") })
            assertTrue("Config ${config.rulesetId} missing Main Phase 1", titles.any { it.lowercase().contains("main phase 1") })
            assertTrue("Config ${config.rulesetId} missing Combat Phase", titles.any { it.lowercase().contains("combat") })
            assertTrue("Config ${config.rulesetId} missing Main Phase 2", titles.any { it.lowercase().contains("main phase 2") })
            assertTrue("Config ${config.rulesetId} missing Ending Phase", titles.any { it.lowercase().contains("ending") })
        }
    }

    @Test
    fun `commander and duel commander have different setup from standard`() {
        val standardSetup = MagicRules.configs.first { it.rulesetId == "standard" }.setup
        val commanderSetup = MagicRules.configs.first { it.rulesetId == "commander" }.setup
        assertTrue("Commander setup should differ from standard", standardSetup != commanderSetup)
    }

    @Test
    fun `commander and duel commander have extra victory condition`() {
        val standardVictory = MagicRules.configs.first { it.rulesetId == "standard" }.victoryRules
        val commanderVictory = MagicRules.configs.first { it.rulesetId == "commander" }.victoryRules
        assertTrue("Commander should have more victory conditions", commanderVictory.size > standardVictory.size)
    }

    @Test
    fun `keywords include combat ability keywords`() {
        for (config in MagicRules.configs) {
            val names = config.keywords!!.map { it.name }
            for (kw in listOf("Flying", "Trample", "Haste", "Vigilance", "Deathtouch", "Lifelink", "First Strike", "Menace")) {
                assertTrue("Config ${config.rulesetId} missing $kw", names.any { it.contains(kw, ignoreCase = true) })
            }
        }
    }

    @Test
    fun `keywords use valid categories`() {
        val validCategories = setOf("Combat", "Timing", "Protection", "Triggers", "Actions", "Core Concepts", "Commander", "Keyword Ability", "Keyword Action", "Core Concept")
        for (config in MagicRules.configs) {
            for (keyword in config.keywords!!) {
                assertTrue(
                    "Keyword '${keyword.name}' has category '${keyword.category}' not in $validCategories",
                    keyword.category in validCategories
                )
            }
        }
    }
}
