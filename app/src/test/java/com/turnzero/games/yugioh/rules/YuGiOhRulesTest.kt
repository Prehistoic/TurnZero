package com.turnzero.games.yugioh.rules

import org.junit.Assert.*
import org.junit.Test

class YuGiOhRulesTest {

    @Test
    fun `each config has non-empty setup`() {
        for (config in YuGiOhRules.configs) {
            assertTrue(
                "Config ${config.rulesetId} has empty setup",
                config.setup.isNotEmpty()
            )
        }
    }

    @Test
    fun `each config has non-empty winCons`() {
        for (config in YuGiOhRules.configs) {
            assertTrue(
                "Config ${config.rulesetId} has empty winCons",
                config.winCons.isNotEmpty()
            )
        }
    }

    @Test
    fun `each config has non-empty playSequence`() {
        for (config in YuGiOhRules.configs) {
            assertTrue(
                "Config ${config.rulesetId} has empty playSequence",
                config.playSequence.isNotEmpty()
            )
        }
    }

    @Test
    fun `each config has keywords`() {
        for (config in YuGiOhRules.configs) {
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
    fun `all ruleset ids are unique`() {
        val ids = YuGiOhRules.configs.map { it.rulesetId }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `all ruleset names follow vX_Y - Era pattern`() {
        for (config in YuGiOhRules.configs) {
            assertTrue(
                "Config ${config.rulesetId} name '${config.rulesetName}' does not match pattern",
                config.rulesetName.matches(Regex("^v\\d+(\\.\\d+)? - (DM|GX|5D's|ZEXAL|ARC-V|VRAINS)$"))
            )
        }
    }

    @Test
    fun `keyword counts increase monotonically across versions`() {
        val configs = YuGiOhRules.configs
        for (i in 1 until configs.size) {
            val prevCount = configs[i - 1].keywords!!.size
            val currCount = configs[i].keywords!!.size
            assertTrue(
                "Keywords decreased from ${configs[i - 1].rulesetName} ($prevCount) to ${configs[i].rulesetName} ($currCount)",
                currCount >= prevCount
            )
        }
    }

    @Test
    fun `all pdfAsset paths point to rulebooks directory`() {
        for (config in YuGiOhRules.configs) {
            assertNotNull(
                "Config ${config.rulesetId} has null pdfAsset",
                config.pdfAsset
            )
            assertTrue(
                "Config ${config.rulesetId} pdfAsset '${config.pdfAsset}' doesn't start with rulebooks/",
                config.pdfAsset!!.startsWith("rulebooks/")
            )
        }
    }

    @Test
    fun `play sequence includes Draw Phase and Battle Phase`() {
        for (config in YuGiOhRules.configs) {
            val titles = config.playSequence.map { it.title }
            assertTrue(
                "Config ${config.rulesetId} missing Draw Phase",
                titles.any { it.contains("Draw Phase") }
            )
            assertTrue(
                "Config ${config.rulesetId} missing Battle Phase",
                titles.any { it.contains("Battle Phase") }
            )
        }
    }

    @Test
    fun `setup includes deck preparation step`() {
        for (config in YuGiOhRules.configs) {
            val titles = config.setup.map { it.title }
            assertTrue(
                "Config ${config.rulesetId} missing deck preparation",
                titles.any { it.lowercase().contains("deck") }
            )
        }
    }

    @Test
    fun `win conditions include LP to 0 and deck out`() {
        for (config in YuGiOhRules.configs) {
            val titles = config.winCons.map { it.title }
            assertTrue(
                "Config ${config.rulesetId} missing LP reduction win condition",
                titles.any { it.lowercase().contains("lp") }
            )
            assertTrue(
                "Config ${config.rulesetId} missing deck out win condition",
                titles.any { it.lowercase().contains("deck") }
            )
        }
    }
}
