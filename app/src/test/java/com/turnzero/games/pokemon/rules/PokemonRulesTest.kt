package com.turnzero.games.pokemon.rules

import com.turnzero.core.ui.components.ContentBlock
import org.junit.Assert.*
import org.junit.Test

class PokemonRulesTest {

    @Test
    fun `each config has non-empty setup`() {
        for (config in PokemonRules.configs) {
            assertTrue("Config ${config.rulesetId} has empty setup", config.setup.isNotEmpty())
        }
    }

    @Test
    fun `each config has non-empty victoryRules`() {
        for (config in PokemonRules.configs) {
            assertTrue("Config ${config.rulesetId} has empty victoryRules", config.victoryRules.isNotEmpty())
        }
    }

    @Test
    fun `each config has non-empty playSequence`() {
        for (config in PokemonRules.configs) {
            assertTrue("Config ${config.rulesetId} has empty playSequence", config.playSequence.isNotEmpty())
        }
    }

    @Test
    fun `each config has keywords`() {
        for (config in PokemonRules.configs) {
            assertNotNull("Config ${config.rulesetId} has null keywords", config.keywords)
            assertTrue("Config ${config.rulesetId} has empty keywords", config.keywords!!.isNotEmpty())
        }
    }

    @Test
    fun `each config has fieldContent`() {
        for (config in PokemonRules.configs) {
            assertNotNull("Config ${config.rulesetId} has null fieldContent", config.fieldContent)
            assertTrue("Config ${config.rulesetId} has empty fieldContent", config.fieldContent!!.isNotEmpty())
        }
    }

    @Test
    fun `all ruleset ids are unique`() {
        val ids = PokemonRules.configs.map { it.rulesetId }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `all pdfAssets point to rulebooks directory`() {
        for (config in PokemonRules.configs) {
            assertNotNull("Config ${config.rulesetId} has null pdfAssets", config.pdfAssets)
            assertTrue("Config ${config.rulesetId} has empty pdfAssets", config.pdfAssets!!.isNotEmpty())
            for (pdf in config.pdfAssets!!) {
                assertTrue(
                    "Config ${config.rulesetId} pdfAsset '${pdf.assetPath}' doesn't start with rulebooks/",
                    pdf.assetPath.startsWith("rulebooks/")
                )
            }
        }
    }

    @Test
    fun `setup includes mulligan and prize steps`() {
        for (config in PokemonRules.configs) {
            val titles = config.setup.map { it.title }
            assertTrue("Config ${config.rulesetId} missing Basic Pokemon step", titles.any { it.lowercase().contains("basic") })
            assertTrue("Config ${config.rulesetId} missing Prize step", titles.any { it.lowercase().contains("prize") })
        }
    }

    @Test
    fun `play sequence includes Draw Action Attack and Checkup phases`() {
        for (config in PokemonRules.configs) {
            val titles = config.playSequence.map { it.title }
            assertTrue("Config ${config.rulesetId} missing Draw step", titles.any { it.lowercase().contains("draw") })
            assertTrue("Config ${config.rulesetId} missing Action phase", titles.any { it.lowercase().contains("action") })
            assertTrue("Config ${config.rulesetId} missing Attack step", titles.any { it.lowercase().contains("attack") })
            assertTrue("Config ${config.rulesetId} missing Checkup step", titles.any { it.lowercase().contains("checkup") })
        }
    }

    @Test
    fun `win conditions include prize deck and forfeit`() {
        for (config in PokemonRules.configs) {
            val titles = config.victoryRules.map { it.title }
            assertTrue("Config ${config.rulesetId} missing Prize win condition", titles.any { it.lowercase().contains("prize") })
            assertTrue("Config ${config.rulesetId} missing deck out condition", titles.any { it.lowercase().contains("deck") || it.lowercase().contains("empty") })
            assertTrue("Config ${config.rulesetId} missing concession", titles.any { it.lowercase().contains("concession") })
        }
    }

    @Test
    fun `keywords include Asleep Burned Confused Paralyzed Poisoned`() {
        for (config in PokemonRules.configs) {
            val names = config.keywords!!.map { it.name }
            for (cond in listOf("Asleep", "Burned", "Confused", "Paralyzed", "Poisoned")) {
                assertTrue("Config ${config.rulesetId} missing $cond", names.any { it.contains(cond, ignoreCase = true) })
            }
        }
    }

    @Test
    fun `keywords include ex V VSTAR and VMAX`() {
        for (config in PokemonRules.configs) {
            val names = config.keywords!!.map { it.name }
            assertTrue("Config ${config.rulesetId} missing Pokemon ex", names.any { it.contains("ex", ignoreCase = true) })
            assertTrue("Config ${config.rulesetId} missing Pokemon V", names.any { it.contains("Pokemon V", ignoreCase = true) || it.contains("<Pokemon V>", ignoreCase = true) })
            assertTrue("Config ${config.rulesetId} missing VSTAR", names.any { it.contains("VSTAR", ignoreCase = true) })
            assertTrue("Config ${config.rulesetId} missing VMAX", names.any { it.contains("VMAX", ignoreCase = true) })
        }
    }

    @Test
    fun `fieldContent includes an image asset`() {
        for (config in PokemonRules.configs) {
            val hasImage = config.fieldContent!!.any { block ->
                block is ContentBlock.Image && block.uri.startsWith("asset:")
            }
            assertTrue("Config ${config.rulesetId} fieldContent has no asset image", hasImage)
        }
    }

    @Test
    fun `keywords use valid categories`() {
        val validCategories = setOf("Game Terms", "Special Conditions", "Evolution", "Pokemon Types", "Misc")
        for (config in PokemonRules.configs) {
            for (keyword in config.keywords!!) {
                assertTrue(
                    "Keyword '${keyword.name}' has category '${keyword.category}' not in $validCategories",
                    keyword.category in validCategories
                )
            }
        }
    }
}
