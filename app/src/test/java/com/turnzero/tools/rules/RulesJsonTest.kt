package com.turnzero.tools.rules

import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class RulesJsonTest {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Each entry pairs a canonical `when` shape with the model it must parse into.
     * Canonical means `encode(parse(shape)) == shape`.
     */
    private val canonicalWhenShapes: List<Pair<String, Condition>> = listOf(
        "{}" to Condition.And(emptyList()),
        """{"format":"commander"}""" to
            Condition.And(listOf(Condition.Predicate("format", SpecValue.Eq("commander")))),
        """{"format":"commander","extensions":["exp-a"]}""" to
            Condition.And(
                listOf(
                    Condition.Predicate("format", SpecValue.Eq("commander")),
                    Condition.Predicate("extensions", SpecValue.AllOf(listOf("exp-a"))),
                )
            ),
        """{"format":["a","b"]}""" to
            Condition.And(listOf(Condition.Predicate("format", SpecValue.AllOf(listOf("a", "b"))))),
        """{"format":{"or":["a","b"]}}""" to
            Condition.And(listOf(Condition.Predicate("format", SpecValue.AnyOf(listOf("a", "b"))))),
        """{"extensions":{"not":"a"}}""" to
            Condition.And(listOf(Condition.Predicate("extensions", SpecValue.NoneOf(listOf("a"))))),
        """{"extensions":{"not":["a","b"]}}""" to
            Condition.And(
                listOf(Condition.Predicate("extensions", SpecValue.NoneOf(listOf("a", "b"))))
            ),
        """{"extensions":[]}""" to
            Condition.And(listOf(Condition.Predicate("extensions", SpecValue.NoneSelected))),
        """{"or":[{"format":"standard"},{"extensions":["a"]}]}""" to
            Condition.Or(
                listOf(
                    Condition.And(listOf(Condition.Predicate("format", SpecValue.Eq("standard")))),
                    Condition.And(
                        listOf(Condition.Predicate("extensions", SpecValue.AllOf(listOf("a"))))
                    ),
                )
            ),
        """{"not":{"extensions":["a"]}}""" to
            Condition.Not(
                Condition.And(
                    listOf(Condition.Predicate("extensions", SpecValue.AllOf(listOf("a"))))
                )
            ),
        """{"and":[{"format":"standard"},{"extensions":["a"]}]}""" to
            Condition.And(
                listOf(
                    Condition.And(listOf(Condition.Predicate("format", SpecValue.Eq("standard")))),
                    Condition.And(
                        listOf(Condition.Predicate("extensions", SpecValue.AllOf(listOf("a"))))
                    ),
                )
            ),
    )

    @Test
    fun `parses manifest with all specificity types and requires`() {
        val manifest = json.decodeFromString<RulesManifest>(
            """
            {
              "specificities": [
                {
                  "id": "format", "name": "Format", "type": "single",
                  "options": [
                    { "id": "standard", "name": "Standard" },
                    { "id": "commander", "name": "Commander" }
                  ]
                },
                { "id": "players", "name": "Player Count", "type": "number", "min": 2, "max": 6 },
                {
                  "id": "extensions", "name": "Extensions", "type": "multi",
                  "options": [
                    { "id": "exp-b", "name": "Extension B", "requires": ["exp-a"] },
                    { "id": "exp-a", "name": "Extension A" }
                  ]
                }
              ],
              "rulebooks": [
                { "id": "official", "name": "Official Rules", "asset": "rulebooks/official.pdf" }
              ]
            }
            """.trimIndent()
        )

        assertEquals(3, manifest.specificities.size)

        val format = manifest.specificities[0]
        assertEquals("format", format.id)
        assertEquals("Format", format.name)
        assertEquals(SpecificityType.SINGLE, format.type)
        assertEquals(listOf("standard", "commander"), format.options.map { it.id })
        assertEquals(listOf("Standard", "Commander"), format.options.map { it.name })
        assertTrue(format.options.all { it.requires.isEmpty() })
        assertNull(format.min)
        assertNull(format.max)

        val players = manifest.specificities[1]
        assertEquals(SpecificityType.NUMBER, players.type)
        assertEquals(2, players.min)
        assertEquals(6, players.max)
        assertTrue(players.options.isEmpty())

        val extensions = manifest.specificities[2]
        assertEquals(SpecificityType.MULTI, extensions.type)
        assertEquals(listOf("exp-b", "exp-a"), extensions.options.map { it.id })
        assertEquals(listOf("exp-a"), extensions.options[0].requires)
        assertTrue(extensions.options[1].requires.isEmpty())

        val rulebook = manifest.rulebooks.single()
        assertEquals("official", rulebook.id)
        assertEquals("Official Rules", rulebook.name)
        assertEquals("rulebooks/official.pdf", rulebook.asset)
        assertEquals(Condition.ALWAYS, rulebook.`when`)
    }

    @Test
    fun `parses every when grammar shape`() {
        for ((shape, expected) in canonicalWhenShapes) {
            assertEquals("parsing $shape", expected, decodeWhen(shape))
        }

        // Explicit "and" on a spec value is accepted but normalized to the list shorthand.
        val explicitAnd = decodeWhen("""{"format":{"and":["a","b"]}}""")
        assertEquals(
            Condition.And(listOf(Condition.Predicate("format", SpecValue.AllOf(listOf("a", "b"))))),
            explicitAnd
        )
        assertEquals(
            """{"format":["a","b"]}""",
            json.encodeToString(ConditionSerializer, explicitAnd)
        )
    }

    @Test
    fun `absent optional fields use defaults`() {
        val item = json.decodeFromString<RuleItem>("{}")

        assertNull(item.title)
        assertEquals(emptyList<String>(), item.text)
        assertEquals(emptyList<String>(), item.bullets)
        assertNull(item.image)
        assertEquals(Condition.ALWAYS, item.`when`)
    }

    @Test
    fun `serialize then parse round trips canonical forms`() {
        for ((shape, expected) in canonicalWhenShapes) {
            val parsed = decodeWhen(shape)
            assertEquals("parsing $shape", expected, parsed)

            val encoded = json.encodeToString(ConditionSerializer, parsed)
            assertEquals("canonical form of $shape", shape, encoded)

            assertEquals("round trip of $shape", parsed, decodeWhen(encoded))
        }

        val item = RuleItem(
            title = "Mulligan",
            `when` = Condition.And(
                listOf(Condition.Predicate("format", SpecValue.Eq("commander")))
            )
        )
        val encodedItem = json.encodeToString(item)
        assertEquals("""{"title":"Mulligan","when":{"format":"commander"}}""", encodedItem)
        assertEquals(item, json.decodeFromString<RuleItem>(encodedItem))
    }

    @Test
    fun `unknown top-level keys are ignored`() {
        val manifest = json.decodeFromString<RulesManifest>(
            """
            {
              "future_field": { "nested": true },
              "specificities": [ { "id": "format", "name": "Format", "type": "single" } ],
              "unknown": 42
            }
            """.trimIndent()
        )

        assertEquals(1, manifest.specificities.size)
        assertEquals("format", manifest.specificities.single().id)
        assertTrue(manifest.rulebooks.isEmpty())
    }

    @Test
    fun `malformed when shapes throw`() {
        val malformedShapes = listOf(
            """"standard"""",
            "[]",
            "null",
            """{"or":"standard"}""",
            """{"or":[{"format":"standard"},3]}""",
            """{"not":"standard"}""",
            """{"and":[],"format":"standard"}""",
            """{"format":3}""",
            """{"format":true}""",
            """{"format":null}""",
            """{"format":["a",3]}""",
            """{"format":{"or":"a"}}""",
            """{"format":{"unknown":["a"]}}""",
            """{"format":{}}"""
        )

        for (shape in malformedShapes) {
            try {
                decodeWhen(shape)
                fail("Expected malformed when shape to throw: $shape")
            } catch (expected: SerializationException) {
                // expected
            }
        }
    }

    private fun decodeWhen(shape: String): Condition =
        json.decodeFromString<RuleItem>("""{"when":$shape}""").`when`
}
