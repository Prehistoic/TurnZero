package com.turnzero.tools.rules

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RulesRepositoryTest {

    private val manifestJson = """
        {
          "specificities": [
            {
              "id": "format", "name": "Format", "type": "single",
              "options": [ { "id": "standard", "name": "Standard" } ]
            }
          ],
          "rulebooks": [
            { "id": "official", "name": "Official Rules", "asset": "rulebooks/official.pdf" }
          ],
          "futureField": true
        }
    """.trimIndent()

    private val setupJson = """
        {
          "pages": [ { "rulebook": "official", "pages": [1, 2] } ],
          "steps": [ { "title": "Shuffle", "text": ["Shuffle your deck."] } ]
        }
    """.trimIndent()

    private val turnJson = """
        {
          "pages": [ { "rulebook": "official", "pages": [3] } ],
          "phases": [
            { "name": "Draw Phase", "steps": [ { "title": "Draw", "bullets": ["Draw one card."] } ] }
          ]
        }
    """.trimIndent()

    private val winJson = """
        {
          "pages": [],
          "winConditions": [
            { "title": "Reduce life", "text": ["Reduce the opponent's life to zero."] }
          ]
        }
    """.trimIndent()

    private val keywordsJson = """
        {
          "pages": [],
          "keywords": [
            { "name": "Flying", "category": "Evasion", "text": ["Cannot be blocked."] }
          ]
        }
    """.trimIndent()

    private val completeFolder = mapOf(
        "rules/test-game/manifest.json" to manifestJson,
        "rules/test-game/setup.json" to setupJson,
        "rules/test-game/turn.json" to turnJson,
        "rules/test-game/win.json" to winJson,
        "rules/test-game/keywords.json" to keywordsJson,
    )

    @Test
    fun `getRules parses a complete game folder`() = runTest {
        val repository = AssetRulesRepository(FakeAssetSource(completeFolder.toMap()))

        val rules = repository.getRules("test-game")

        assertNotNull(rules)
        rules!!
        assertEquals("format", rules.manifest.specificities.single().id)
        assertEquals(listOf(1, 2), rules.setup.pages.single().pages)
        assertEquals("Shuffle", rules.setup.steps.single().title)
        assertEquals("Draw Phase", rules.turn.phases.single().name)
        assertEquals("Reduce life", rules.win.winConditions.single().title)
        assertEquals("Flying", rules.keywords.keywords.single().name)
        assertEquals(Condition.ALWAYS, rules.setup.steps.single().`when`)
    }

    @Test
    fun `getRules returns null when manifest missing`() = runTest {
        val withoutManifest = completeFolder - "rules/test-game/manifest.json"
        val repository = AssetRulesRepository(FakeAssetSource(withoutManifest))

        assertNull(repository.getRules("test-game"))
    }

    @Test
    fun `getRules returns null on malformed json`() = runTest {
        val malformed = completeFolder + ("rules/test-game/setup.json" to "not json")
        val repository = AssetRulesRepository(FakeAssetSource(malformed))

        assertNull(repository.getRules("test-game"))
    }

    @Test
    fun `getRules returns null when a content file is missing`() = runTest {
        val withoutKeywords = completeFolder - "rules/test-game/keywords.json"
        val repository = AssetRulesRepository(FakeAssetSource(withoutKeywords))

        assertNull(repository.getRules("test-game"))
    }

    @Test
    fun `getRules caches parsed rules`() = runTest {
        val source = CountingAssetSource(completeFolder.toMap())
        val repository = AssetRulesRepository(source)

        val first = repository.getRules("test-game")
        val second = repository.getRules("test-game")

        assertEquals(first, second)
        assertEquals(1, source.reads.count { it == "rules/test-game/manifest.json" })
        assertEquals(5, source.reads.size)
    }

    @Test
    fun `getRules retries after a failed load instead of caching null`() = runTest {
        val source = CountingAssetSource(completeFolder.toMap())
        val repository = AssetRulesRepository(source)

        source.remove("rules/test-game/manifest.json")
        assertNull(repository.getRules("test-game"))

        source.put("rules/test-game/manifest.json", manifestJson)
        assertNotNull(repository.getRules("test-game"))
    }

    private class FakeAssetSource(private val paths: Map<String, String>) : RulesAssetSource {
        override fun read(path: String): String? = paths[path]
    }

    private class CountingAssetSource(paths: Map<String, String>) : RulesAssetSource {
        private val paths = paths.toMutableMap()
        val reads = mutableListOf<String>()

        override fun read(path: String): String? {
            reads += path
            return paths[path]
        }

        fun put(path: String, content: String) {
            paths[path] = content
        }

        fun remove(path: String) {
            paths.remove(path)
        }
    }
}
