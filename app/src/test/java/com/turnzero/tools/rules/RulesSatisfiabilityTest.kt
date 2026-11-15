package com.turnzero.tools.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RulesSatisfiabilityTest {

    private val manifest = RulesManifest(
        specificities = listOf(
            SpecificityDef(
                id = "format",
                name = "Format",
                type = SpecificityType.SINGLE,
                options = listOf(
                    OptionDef(id = "standard", name = "Standard"),
                    OptionDef(id = "commander", name = "Commander")
                )
            ),
            SpecificityDef(
                id = "players",
                name = "Players",
                type = SpecificityType.NUMBER,
                min = 2,
                max = 4
            ),
            SpecificityDef(
                id = "extensions",
                name = "Extensions",
                type = SpecificityType.MULTI,
                options = listOf(
                    OptionDef(id = "base", name = "Base"),
                    OptionDef(id = "exp-a", name = "Extension A"),
                    OptionDef(id = "exp-b", name = "Extension B", requires = listOf("exp-a"))
                )
            )
        )
    )

    private fun predicate(specId: String, value: SpecValue): Condition =
        Condition.Predicate(specId, value)

    @Test
    fun `validSelections enumerates only dependency-closed multi subsets`() {
        val selections = validSelections(manifest)

        assertEquals(
            setOf(
                emptySet<String>(),
                setOf("base"),
                setOf("exp-a"),
                setOf("exp-a", "exp-b"),
                setOf("base", "exp-a"),
                setOf("base", "exp-a", "exp-b")
            ),
            selections.map { it.multi.getValue("extensions") }.toSet()
        )

        // 2 formats × 3 player counts × 6 dependency-closed extension subsets.
        assertEquals(36, selections.size)

        // Every enumerated selection is dependency-closed by construction.
        assertTrue(
            selections.all { selection ->
                val selected = selection.multi.getValue("extensions")
                "exp-b" !in selected || "exp-a" in selected
            }
        )
    }

    @Test
    fun `validSelections enumerates full number range`() {
        val selections = validSelections(manifest)

        assertEquals(setOf(2, 3, 4), selections.map { it.number.getValue("players") }.toSet())
        assertEquals(
            setOf("standard", "commander"),
            selections.map { it.single.getValue("format") }.toSet()
        )
    }

    @Test
    fun `validSelections falls back to the lower bound for unbounded numbers`() {
        val twoSided = RulesManifest(
            specificities = listOf(
                SpecificityDef(
                    id = "count",
                    name = "Count",
                    type = SpecificityType.NUMBER,
                    min = 3
                )
            )
        )
        assertEquals(listOf(3), validSelections(twoSided).map { it.number.getValue("count") })

        val unbounded = RulesManifest(
            specificities = listOf(
                SpecificityDef(id = "count", name = "Count", type = SpecificityType.NUMBER)
            )
        )
        assertEquals(listOf(0), validSelections(unbounded).map { it.number.getValue("count") })

        // A manifest without specificities still has exactly one (empty) selection.
        assertEquals(listOf(Selection()), validSelections(RulesManifest()))
        assertTrue(isSatisfiable(Condition.ALWAYS, RulesManifest()))
    }

    @Test
    fun `isSatisfiable false when requires contradicted`() {
        val condition = Condition.And(
            listOf(
                predicate("extensions", SpecValue.Eq("exp-b")),
                predicate("extensions", SpecValue.NoneOf(listOf("exp-a")))
            )
        )

        assertFalse(isSatisfiable(condition, manifest))
    }

    @Test
    fun `isSatisfiable true for single extension condition`() {
        // exp-b requires exp-a, which the dependency closure supplies automatically.
        assertTrue(isSatisfiable(predicate("extensions", SpecValue.Eq("exp-b")), manifest))
        assertTrue(isSatisfiable(predicate("extensions", SpecValue.Eq("base")), manifest))
    }

    @Test
    fun `isSatisfiable true for NoneSelected`() {
        assertTrue(isSatisfiable(predicate("extensions", SpecValue.NoneSelected), manifest))
    }

    @Test
    fun `isSatisfiable respects format and extensions conjunction`() {
        assertTrue(
            isSatisfiable(
                Condition.And(
                    listOf(
                        predicate("format", SpecValue.Eq("commander")),
                        predicate("players", SpecValue.Eq("4")),
                        predicate("extensions", SpecValue.AllOf(listOf("base", "exp-b")))
                    )
                ),
                manifest
            )
        )

        // base + exp-b is fine, but forbidding exp-a at the same time can never be reached.
        assertFalse(
            isSatisfiable(
                Condition.And(
                    listOf(
                        predicate("format", SpecValue.Eq("commander")),
                        predicate("extensions", SpecValue.AllOf(listOf("base", "exp-b"))),
                        predicate("extensions", SpecValue.NoneOf(listOf("exp-a")))
                    )
                ),
                manifest
            )
        )
    }
}
