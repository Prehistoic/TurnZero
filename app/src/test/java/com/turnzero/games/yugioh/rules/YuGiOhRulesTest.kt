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
    fun `all ruleset ids are unique`() {
        val ids = YuGiOhRules.configs.map { it.rulesetId }
        assertEquals(ids.size, ids.toSet().size)
    }
}
