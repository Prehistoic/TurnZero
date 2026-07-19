package com.turnzero.games.onepiece.rules

import com.turnzero.tools.rules.ContentBlock
import org.junit.Assert.*
import org.junit.Test

class OnePieceRulesTest {

    @Test
    fun `each config has non-empty setup`() {
        for (config in OnePieceRules.configs) {
            assertTrue(
                "Config ${config.rulesetId} has empty setup",
                config.setup.isNotEmpty()
            )
        }
    }

    @Test
    fun `each config has non-empty winCons`() {
        for (config in OnePieceRules.configs) {
            assertTrue(
                "Config ${config.rulesetId} has empty winCons",
                config.winCons.isNotEmpty()
            )
        }
    }

    @Test
    fun `each config has non-empty playSequence`() {
        for (config in OnePieceRules.configs) {
            assertTrue(
                "Config ${config.rulesetId} has empty playSequence",
                config.playSequence.isNotEmpty()
            )
        }
    }

    @Test
    fun `each config has keywords`() {
        for (config in OnePieceRules.configs) {
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
        for (config in OnePieceRules.configs) {
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
        val ids = OnePieceRules.configs.map { it.rulesetId }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `all pdfAssets point to rulebooks directory`() {
        for (config in OnePieceRules.configs) {
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
    fun `setup includes leader and mulligan steps`() {
        for (config in OnePieceRules.configs) {
            val titles = config.setup.map { it.title }
            assertTrue(
                "Config ${config.rulesetId} missing Leader step",
                titles.any { it.lowercase().contains("leader") }
            )
            assertTrue(
                "Config ${config.rulesetId} missing Mulligan step",
                titles.any { it.lowercase().contains("mulligan") }
            )
        }
    }

    @Test
    fun `play sequence includes Refresh Draw DON!! Main and End phases`() {
        for (config in OnePieceRules.configs) {
            val titles = config.playSequence.map { it.title }
            assertTrue(
                "Config ${config.rulesetId} missing Refresh Phase",
                titles.any { it.lowercase().contains("refresh") }
            )
            assertTrue(
                "Config ${config.rulesetId} missing Draw Phase",
                titles.any { it.lowercase().contains("draw") }
            )
            assertTrue(
                "Config ${config.rulesetId} missing DON!! Phase",
                titles.any { it.lowercase().contains("don") }
            )
            assertTrue(
                "Config ${config.rulesetId} missing Main Phase",
                titles.any { it.lowercase().contains("main") }
            )
            assertTrue(
                "Config ${config.rulesetId} missing End Phase",
                titles.any { it.lowercase().contains("end") }
            )
        }
    }

    @Test
    fun `win conditions include finishing blow and deck out`() {
        for (config in OnePieceRules.configs) {
            val titles = config.winCons.map { it.title }
            assertTrue(
                "Config ${config.rulesetId} missing finishing blow win condition",
                titles.any { it.lowercase().contains("finish") }
            )
            assertTrue(
                "Config ${config.rulesetId} missing deck out win condition",
                titles.any { it.lowercase().contains("empty") }
            )
        }
    }

    @Test
    fun `keywords include Rush Blocker DoubleAttack Banish and Trigger`() {
        for (config in OnePieceRules.configs) {
            val names = config.keywords!!.map { it.name }
            assertTrue(
                "Config ${config.rulesetId} missing Rush",
                names.any { it.contains("Rush", ignoreCase = true) }
            )
            assertTrue(
                "Config ${config.rulesetId} missing Blocker",
                names.any { it.contains("Blocker", ignoreCase = true) }
            )
            assertTrue(
                "Config ${config.rulesetId} missing Double Attack",
                names.any { it.contains("Double Attack", ignoreCase = true) }
            )
            assertTrue(
                "Config ${config.rulesetId} missing Banish",
                names.any { it.contains("Banish", ignoreCase = true) }
            )
            assertTrue(
                "Config ${config.rulesetId} missing Trigger",
                names.any { it.contains("Trigger", ignoreCase = true) }
            )
        }
    }

    @Test
    fun `keywords include OnPlay WhenAttacking OnKO and ActivateMain`() {
        for (config in OnePieceRules.configs) {
            val names = config.keywords!!.map { it.name }
            assertTrue(
                "Config ${config.rulesetId} missing On Play",
                names.any { it.contains("On Play", ignoreCase = true) }
            )
            assertTrue(
                "Config ${config.rulesetId} missing When Attacking",
                names.any { it.contains("When Attacking", ignoreCase = true) }
            )
            assertTrue(
                "Config ${config.rulesetId} missing On K.O.",
                names.any { it.contains("On K.O.", ignoreCase = true) || it.contains("On K.O", ignoreCase = true) }
            )
            assertTrue(
                "Config ${config.rulesetId} missing Activate: Main",
                names.any { it.contains("Activate: Main", ignoreCase = true) }
            )
        }
    }

    @Test
    fun `fieldContent includes an image asset`() {
        for (config in OnePieceRules.configs) {
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
        val validCategories = setOf("Combat", "Timing", "Activation", "Conditions", "Costs", "Misc")
        for (config in OnePieceRules.configs) {
            for (keyword in config.keywords!!) {
                assertTrue(
                    "Keyword '${keyword.name}' has category '${keyword.category}' not in $validCategories",
                    keyword.category in validCategories
                )
            }
        }
    }
}
