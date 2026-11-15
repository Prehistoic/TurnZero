package com.turnzero.tools.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RulesEngineTest {

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
                    OptionDef(id = "exp-a", name = "Extension A"),
                    OptionDef(id = "exp-b", name = "Extension B", requires = listOf("exp-a")),
                    OptionDef(id = "exp-c", name = "Extension C", requires = listOf("exp-b"))
                )
            )
        )
    )

    private fun sel(
        single: Map<String, String> = emptyMap(),
        number: Map<String, Int> = emptyMap(),
        multi: Map<String, Set<String>> = emptyMap()
    ) = Selection(single, number, multi)

    private fun predicate(specId: String, value: SpecValue): Condition =
        Condition.Predicate(specId, value)

    @Test
    fun `matches Eq AllOf AnyOf NoneOf NoneSelected for multi`() {
        val selected = sel(multi = mapOf("extensions" to setOf("exp-a", "exp-b")))

        assertTrue(matches(predicate("extensions", SpecValue.Eq("exp-a")), selected))
        assertFalse(matches(predicate("extensions", SpecValue.Eq("exp-c")), selected))

        assertTrue(
            matches(predicate("extensions", SpecValue.AllOf(listOf("exp-a", "exp-b"))), selected)
        )
        assertFalse(
            matches(predicate("extensions", SpecValue.AllOf(listOf("exp-a", "exp-c"))), selected)
        )

        assertTrue(
            matches(predicate("extensions", SpecValue.AnyOf(listOf("exp-c", "exp-b"))), selected)
        )
        assertFalse(matches(predicate("extensions", SpecValue.AnyOf(listOf("exp-c"))), selected))

        assertTrue(matches(predicate("extensions", SpecValue.NoneOf(listOf("exp-c"))), selected))
        assertFalse(matches(predicate("extensions", SpecValue.NoneOf(listOf("exp-a"))), selected))

        assertFalse(matches(predicate("extensions", SpecValue.NoneSelected), selected))
        assertTrue(
            matches(
                predicate("extensions", SpecValue.NoneSelected),
                sel(multi = mapOf("extensions" to emptySet()))
            )
        )
    }

    @Test
    fun `matches Eq AllOf AnyOf NoneOf NoneSelected for single`() {
        val commander = sel(single = mapOf("format" to "commander"))

        assertTrue(matches(predicate("format", SpecValue.Eq("commander")), commander))
        assertFalse(matches(predicate("format", SpecValue.Eq("standard")), commander))

        assertTrue(
            matches(
                predicate("format", SpecValue.AnyOf(listOf("standard", "commander"))),
                commander
            )
        )
        assertFalse(matches(predicate("format", SpecValue.AnyOf(listOf("standard"))), commander))

        assertTrue(matches(predicate("format", SpecValue.NoneOf(listOf("standard"))), commander))
        assertFalse(matches(predicate("format", SpecValue.NoneOf(listOf("commander"))), commander))

        assertTrue(matches(predicate("format", SpecValue.AllOf(listOf("commander"))), commander))
        assertFalse(
            matches(
                predicate("format", SpecValue.AllOf(listOf("commander", "standard"))),
                commander
            )
        )

        assertFalse(matches(predicate("format", SpecValue.NoneSelected), commander))
    }

    @Test
    fun `matches stringified number equality`() {
        val three = sel(number = mapOf("players" to 3))

        assertTrue(matches(predicate("players", SpecValue.Eq("3")), three))
        assertFalse(matches(predicate("players", SpecValue.Eq("2")), three))

        assertTrue(matches(predicate("players", SpecValue.AnyOf(listOf("2", "3"))), three))
        assertFalse(matches(predicate("players", SpecValue.AnyOf(listOf("2", "4"))), three))

        assertTrue(matches(predicate("players", SpecValue.NoneOf(listOf("2", "4"))), three))
        assertFalse(matches(predicate("players", SpecValue.NoneOf(listOf("3"))), three))

        assertTrue(matches(predicate("players", SpecValue.AllOf(listOf("3"))), three))
        assertFalse(matches(predicate("players", SpecValue.AllOf(listOf("2", "3"))), three))

        assertFalse(matches(predicate("players", SpecValue.NoneSelected), three))
    }

    @Test
    fun `matches And Or Not combinations`() {
        val selection = sel(
            single = mapOf("format" to "commander"),
            number = mapOf("players" to 4),
            multi = mapOf("extensions" to setOf("exp-a"))
        )

        assertTrue(
            matches(
                Condition.And(
                    listOf(
                        predicate("format", SpecValue.Eq("commander")),
                        predicate("players", SpecValue.Eq("4")),
                        predicate("extensions", SpecValue.Eq("exp-a"))
                    )
                ),
                selection
            )
        )
        assertFalse(
            matches(
                Condition.And(
                    listOf(
                        predicate("format", SpecValue.Eq("commander")),
                        predicate("players", SpecValue.Eq("2"))
                    )
                ),
                selection
            )
        )

        assertTrue(
            matches(
                Condition.Or(
                    listOf(
                        predicate("players", SpecValue.Eq("2")),
                        predicate("format", SpecValue.Eq("commander"))
                    )
                ),
                selection
            )
        )
        assertFalse(
            matches(
                Condition.Or(
                    listOf(
                        predicate("players", SpecValue.Eq("2")),
                        predicate("format", SpecValue.Eq("standard"))
                    )
                ),
                selection
            )
        )

        assertTrue(
            matches(Condition.Not(predicate("format", SpecValue.Eq("standard"))), selection)
        )
        assertFalse(
            matches(Condition.Not(predicate("format", SpecValue.Eq("commander"))), selection)
        )
        assertTrue(
            matches(
                Condition.Not(
                    Condition.Or(
                        listOf(
                            predicate("players", SpecValue.Eq("2")),
                            predicate("extensions", SpecValue.NoneSelected)
                        )
                    )
                ),
                selection
            )
        )
    }

    @Test
    fun `matches empty And as true`() {
        assertTrue(matches(Condition.And(emptyList()), sel()))
        assertTrue(matches(Condition.ALWAYS, sel()))
        // The dual of an empty And is an empty Or, which never matches.
        assertFalse(matches(Condition.Or(emptyList()), sel()))
    }

    @Test
    fun `matches unknown specId as false`() {
        val populated = sel(
            single = mapOf("format" to "commander"),
            number = mapOf("players" to 4),
            multi = mapOf("extensions" to setOf("exp-a"))
        )

        val values = listOf(
            SpecValue.Eq("exp-a"),
            SpecValue.AllOf(listOf("exp-a")),
            SpecValue.AnyOf(listOf("exp-a")),
            SpecValue.NoneOf(listOf("exp-a")),
            SpecValue.NoneSelected
        )

        for (value in values) {
            assertFalse(
                "unknown specId with $value",
                matches(predicate("unknown", value), populated)
            )
        }
        // ... even when the selection leaves every map empty.
        assertFalse(matches(predicate("unknown", SpecValue.NoneSelected), sel()))
    }

    @Test
    fun `defaultSelection picks first option and min`() {
        val defaults = defaultSelection(manifest)

        assertEquals(mapOf("format" to "standard"), defaults.single)
        assertEquals(mapOf("players" to 2), defaults.number)
        assertEquals(mapOf("extensions" to emptySet<String>()), defaults.multi)

        val bare = defaultSelection(
            RulesManifest(
                specificities = listOf(
                    SpecificityDef(id = "empty", name = "Empty", type = SpecificityType.SINGLE),
                    SpecificityDef(id = "count", name = "Count", type = SpecificityType.NUMBER)
                )
            )
        )
        assertEquals(mapOf("empty" to ""), bare.single)
        assertEquals(mapOf("count" to 0), bare.number)
        assertTrue(bare.multi.isEmpty())
    }

    @Test
    fun `selectOption adds transitive requires closure`() {
        val empty = defaultSelection(manifest)

        val all = selectOption(manifest, empty, "extensions", "exp-c")
        assertEquals(setOf("exp-a", "exp-b", "exp-c"), all.multi["extensions"])

        // Re-selecting an option keeps the selection unchanged.
        assertEquals(all, selectOption(manifest, all, "extensions", "exp-c"))

        // An option without requirements selects only itself.
        val onlyA = selectOption(manifest, empty, "extensions", "exp-a")
        assertEquals(setOf("exp-a"), onlyA.multi["extensions"])

        // Other specificities are untouched.
        assertEquals(empty.single, all.single)
        assertEquals(empty.number, all.number)

        // An unknown specificity is a no-op.
        assertEquals(empty, selectOption(manifest, empty, "unknown", "exp-a"))
    }

    @Test
    fun `deselectOption removes transitive dependents`() {
        val all = sel(multi = mapOf("extensions" to setOf("exp-a", "exp-b", "exp-c")))

        // exp-b requires exp-a and exp-c requires exp-b, so dropping exp-a drops everything.
        assertEquals(
            emptySet<String>(),
            deselectOption(manifest, all, "extensions", "exp-a").multi["extensions"]
        )
        assertEquals(
            setOf("exp-a"),
            deselectOption(manifest, all, "extensions", "exp-b").multi["extensions"]
        )
        assertEquals(
            setOf("exp-a", "exp-b"),
            deselectOption(manifest, all, "extensions", "exp-c").multi["extensions"]
        )

        // Other specificities are untouched.
        assertEquals(all.single, deselectOption(manifest, all, "extensions", "exp-a").single)
        assertEquals(all.number, deselectOption(manifest, all, "extensions", "exp-a").number)
    }

    @Test
    fun `selectOption on single replaces value`() {
        val commander = selectOption(manifest, defaultSelection(manifest), "format", "commander")
        assertEquals(mapOf("format" to "commander"), commander.single)

        val standardAgain = selectOption(manifest, commander, "format", "standard")
        assertEquals(mapOf("format" to "standard"), standardAgain.single)

        // NUMBER specs and unknown specificities ignore option selection.
        assertEquals(commander, selectOption(manifest, commander, "players", "4"))
        assertEquals(commander, selectOption(manifest, commander, "unknown", "standard"))

        // Deselecting from SINGLE or NUMBER is a no-op.
        assertEquals(commander, deselectOption(manifest, commander, "format", "commander"))
        assertEquals(commander, deselectOption(manifest, commander, "players", "4"))
    }

    @Test
    fun `withNumber clamps to min and max`() {
        val defaults = defaultSelection(manifest)

        assertEquals(2, withNumber(manifest, defaults, "players", 1).number["players"])
        assertEquals(4, withNumber(manifest, defaults, "players", 9).number["players"])
        assertEquals(3, withNumber(manifest, defaults, "players", 3).number["players"])

        // Other specificities are untouched.
        assertEquals(defaults.single, withNumber(manifest, defaults, "players", 9).single)
        assertEquals(defaults.multi, withNumber(manifest, defaults, "players", 9).multi)

        // Non-NUMBER and unknown specificities are no-ops.
        assertEquals(defaults, withNumber(manifest, defaults, "format", 9))
        assertEquals(defaults, withNumber(manifest, defaults, "unknown", 9))

        // A NUMBER spec with no bounds accepts any value.
        val unbounded = RulesManifest(
            specificities = listOf(
                SpecificityDef(id = "count", name = "Count", type = SpecificityType.NUMBER)
            )
        )
        val count = Selection(number = mapOf("count" to 5))
        assertEquals(7, withNumber(unbounded, count, "count", 7).number["count"])
        assertEquals(-3, withNumber(unbounded, count, "count", -3).number["count"])
    }
}
