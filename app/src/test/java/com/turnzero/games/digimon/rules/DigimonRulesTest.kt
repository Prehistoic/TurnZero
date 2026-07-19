package com.turnzero.games.digimon.rules

import com.turnzero.tools.rules.ContentBlock
import org.junit.Assert.*
import org.junit.Test

class DigimonRulesTest {

    @Test
    fun `each config has non-empty setup`() {
        for (config in DigimonRules.configs) {
            assertTrue(
                "Config ${config.rulesetId} has empty setup",
                config.setup.isNotEmpty()
            )
        }
    }

    @Test
    fun `each config has non-empty victoryRules`() {
        for (config in DigimonRules.configs) {
            assertTrue(
                "Config ${config.rulesetId} has empty victoryRules",
                config.victoryRules.isNotEmpty()
            )
        }
    }

    @Test
    fun `each config has non-empty playSequence`() {
        for (config in DigimonRules.configs) {
            assertTrue(
                "Config ${config.rulesetId} has empty playSequence",
                config.playSequence.isNotEmpty()
            )
        }
    }

    @Test
    fun `each config has keywords`() {
        for (config in DigimonRules.configs) {
            assertNotNull(
                "Config ${config.rulesetId} has null keywords",
                config.keywords
            )
            assertTrue(
                "Config ${config.rulesetId} has empty keywords",
                config.keywords!!.isNotEmpty()
            )
        }
    }

    @Test
    fun `each config has fieldContent`() {
        for (config in DigimonRules.configs) {
            assertNotNull(
                "Config ${config.rulesetId} has null fieldContent",
                config.fieldContent
            )
            assertTrue(
                "Config ${config.rulesetId} has empty fieldContent",
                config.fieldContent!!.isNotEmpty()
            )
        }
    }

    @Test
    fun `all ruleset ids are unique`() {
        val ids = DigimonRules.configs.map { it.rulesetId }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `all pdfAssets point to rulebooks directory`() {
        for (config in DigimonRules.configs) {
            assertNotNull(
                "Config ${config.rulesetId} has null pdfAssets",
                config.pdfAssets
            )
            assertTrue(
                "Config ${config.rulesetId} has empty pdfAssets",
                config.pdfAssets!!.isNotEmpty()
            )
            for (pdf in config.pdfAssets!!) {
                assertTrue(
                    "Config ${config.rulesetId} pdfAsset '${pdf.assetPath}' doesn't start with rulebooks/",
                    pdf.assetPath.startsWith("rulebooks/")
                )
            }
        }
    }

    @Test
    fun `setup includes security and mulligan steps`() {
        for (config in DigimonRules.configs) {
            val titles = config.setup.map { it.title }
            assertTrue(
                "Config ${config.rulesetId} missing Security step",
                titles.any { it.lowercase().contains("security") }
            )
            assertTrue(
                "Config ${config.rulesetId} missing Mulligan step",
                titles.any { it.lowercase().contains("mulligan") }
            )
        }
    }

    @Test
    fun `play sequence includes Unsuspend Draw Breeding Main and End phases`() {
        for (config in DigimonRules.configs) {
            val titles = config.playSequence.map { it.title }
            assertTrue(
                "Config ${config.rulesetId} missing Unsuspend Phase",
                titles.any { it.lowercase().contains("unsuspend") }
            )
            assertTrue(
                "Config ${config.rulesetId} missing Draw Phase",
                titles.any { it.lowercase().contains("draw") }
            )
            assertTrue(
                "Config ${config.rulesetId} missing Breeding Phase",
                titles.any { it.lowercase().contains("breeding") }
            )
            assertTrue(
                "Config ${config.rulesetId} missing Main Phase",
                titles.any { it.lowercase().contains("main") }
            )
            assertTrue(
                "Config ${config.rulesetId} missing End Phase",
                titles.any { it.lowercase().contains("end of turn") }
            )
        }
    }

    @Test
    fun `win conditions include security deck out and forfeit`() {
        for (config in DigimonRules.configs) {
            val titles = config.victoryRules.map { it.title }
            assertTrue(
                "Config ${config.rulesetId} missing security win condition",
                titles.any { it.lowercase().contains("security") }
            )
            assertTrue(
                "Config ${config.rulesetId} missing deck out win condition",
                titles.any { it.lowercase().contains("deck") || it.lowercase().contains("empty") }
            )
            assertTrue(
                "Config ${config.rulesetId} missing concession win condition",
                titles.any { it.lowercase().contains("concession") }
            )
        }
    }

    @Test
    fun `keywords include Blocker Security Rush and Jamming`() {
        for (config in DigimonRules.configs) {
            val names = config.keywords!!.map { it.name }
            assertTrue(
                "Config ${config.rulesetId} missing Blocker",
                names.any { it.contains("Blocker", ignoreCase = true) }
            )
            assertTrue(
                "Config ${config.rulesetId} missing Security A.",
                names.any { it.contains("Security A.", ignoreCase = true) }
            )
            assertTrue(
                "Config ${config.rulesetId} missing Rush",
                names.any { it.contains("Rush", ignoreCase = true) }
            )
            assertTrue(
                "Config ${config.rulesetId} missing Jamming",
                names.any { it.contains("Jamming", ignoreCase = true) }
            )
        }
    }

    @Test
    fun `keywords include OnPlay WhenDigivolving OnDeletion and Main`() {
        for (config in DigimonRules.configs) {
            val names = config.keywords!!.map { it.name }
            assertTrue(
                "Config ${config.rulesetId} missing On Play",
                names.any { it.contains("On Play", ignoreCase = true) }
            )
            assertTrue(
                "Config ${config.rulesetId} missing When Digivolving",
                names.any { it.contains("When Digivolving", ignoreCase = true) }
            )
            assertTrue(
                "Config ${config.rulesetId} missing On Deletion",
                names.any { it.contains("On Deletion", ignoreCase = true) }
            )
            assertTrue(
                "Config ${config.rulesetId} missing Main",
                names.any { it.equals("<Main>", ignoreCase = true) }
            )
        }
    }

    @Test
    fun `keywords include DNA Digivolution DigiXros and Assembly`() {
        for (config in DigimonRules.configs) {
            val names = config.keywords!!.map { it.name }
            assertTrue(
                "Config ${config.rulesetId} missing DNA Digivolution",
                names.any { it.contains("DNA Digivolution", ignoreCase = true) }
            )
            assertTrue(
                "Config ${config.rulesetId} missing DigiXros",
                names.any { it.contains("DigiXros", ignoreCase = true) }
            )
            assertTrue(
                "Config ${config.rulesetId} missing Assembly",
                names.any { it.contains("Assembly", ignoreCase = true) }
            )
        }
    }

    @Test
    fun `keywords include Overflow ACE and Security Digimon`() {
        for (config in DigimonRules.configs) {
            val names = config.keywords!!.map { it.name }
            assertTrue(
                "Config ${config.rulesetId} missing Overflow",
                names.any { it.contains("Overflow", ignoreCase = true) }
            )
            assertTrue(
                "Config ${config.rulesetId} missing ACE",
                names.any { it.contains("<ACE>", ignoreCase = true) }
            )
            assertTrue(
                "Config ${config.rulesetId} missing Security Digimon",
                names.any { it.contains("Security Digimon", ignoreCase = true) }
            )
        }
    }

    @Test
    fun `fieldContent includes an image asset`() {
        for (config in DigimonRules.configs) {
            val hasImage = config.fieldContent!!.any { block ->
                block is ContentBlock.Image && block.uri.startsWith("asset:")
            }
            assertTrue(
                "Config ${config.rulesetId} fieldContent has no asset image",
                hasImage
            )
        }
    }

    @Test
    fun `keywords use valid categories`() {
        val validCategories = setOf("Combat", "Movement", "Costs", "Protection", "Recovery", "Utility", "Timing", "Evolution", "Misc")
        for (config in DigimonRules.configs) {
            for (keyword in config.keywords!!) {
                assertTrue(
                    "Keyword '${keyword.name}' has category '${keyword.category}' not in $validCategories",
                    keyword.category in validCategories
                )
            }
        }
    }
}
