package com.turnzero.tools.rules

import java.io.File
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Gate for the JSON content under `app/src/main/assets/rules`. Every game folder must parse and be
 * internally consistent, so a content migration that introduces a typo'd id, a `requires` cycle or
 * a dead rule fails here instead of silently hiding content at runtime.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RulesContentValidationTest {

    private class FileRulesAssetSource(private val root: File) : RulesAssetSource {
        override fun read(path: String): String? =
            root.resolve(path).takeIf { it.isFile }?.readText()
    }

    private val repository = AssetRulesRepository(FileRulesAssetSource(File("src/main/assets")))
    // Unit tests run with the :app module dir as CWD, so src/main/assets resolves.

    @Test
    fun `every game folder parses and is well-formed`() = runTest {
        val gameFolders = gameFolders()
        assertTrue(
            "expected at least one game folder in ${RULES_DIR.path}",
            gameFolders.isNotEmpty()
        )
        gameFolders.forEach { folder ->
            val rules = rulesFor(folder)
            checkManifestWellFormed(folder.name, rules.manifest)
            checkWhenReferences(folder.name, rules)
            checkPageRefs(folder.name, rules)
        }
    }

    @Test
    fun `every when condition is satisfiable`() = runTest {
        gameFolders().forEach { folder ->
            val rules = rulesFor(folder)
            allConditions(rules).forEach { site ->
                assertTrue(
                    "unsatisfiable when in ${folder.name} at ${site.where}: ${site.condition}",
                    isSatisfiable(site.condition, rules.manifest)
                )
            }
        }
    }

    /**
     * The helpers below are what the gate above relies on, and the gate cannot exercise them until
     * content lands. These fixtures pin their behavior so a buggy check cannot ship: the valid case
     * proves no false positives (it must not block the migrations), the broken cases prove the
     * failure is reported with the game folder and the offending item.
     */
    @Test
    fun `validation accepts a consistent game`() {
        val rules = validRules()

        checkManifestWellFormed("test-game", rules.manifest)
        checkWhenReferences("test-game", rules)
        checkPageRefs("test-game", rules)
        allConditions(rules).forEach { site ->
            assertTrue("unsatisfiable when at ${site.where}", isSatisfiable(site.condition, rules.manifest))
        }
    }

    @Test
    fun `validation reports broken specificity definitions`() {
        val format = validRules().manifest.specificities.first()
        val cases = listOf(
            "duplicate specificity ids" to
                RulesManifest(specificities = listOf(format, format)),
            "duplicate option ids" to manifestWith(
                SpecificityDef(
                    id = "format", name = "Format", type = SpecificityType.SINGLE,
                    options = listOf(OptionDef("std", "Standard"), OptionDef("std", "Standard"))
                )
            ),
            "requires unknown options [ghost]" to manifestWith(
                SpecificityDef(
                    id = "ext", name = "Extensions", type = SpecificityType.MULTI,
                    options = listOf(OptionDef("a", "A", requires = listOf("ghost")))
                )
            ),
            "requires cycle: a -> b -> a" to manifestWith(
                SpecificityDef(
                    id = "ext", name = "Extensions", type = SpecificityType.MULTI,
                    options = listOf(
                        OptionDef("a", "A", requires = listOf("b")),
                        OptionDef("b", "B", requires = listOf("a"))
                    )
                )
            ),
            "must declare both min and max" to manifestWith(
                SpecificityDef(id = "players", name = "Players", type = SpecificityType.NUMBER, min = 2)
            ),
            "has min 5 > max 4" to manifestWith(
                SpecificityDef(
                    id = "players", name = "Players", type = SpecificityType.NUMBER, min = 5, max = 4
                )
            ),
            "at most 30 are supported" to manifestWith(
                SpecificityDef(
                    id = "ext", name = "Extensions", type = SpecificityType.MULTI,
                    options = (1..31).map { OptionDef("extension-$it", "Extension $it") }
                )
            ),
        )

        cases.forEach { (expected, manifest) ->
            assertValidationFailure(expected) { checkManifestWellFormed("test-game", manifest) }
        }
    }

    @Test
    fun `validation reports broken when references`() {
        val cases = listOf(
            "unknown specificity 'ghost'" to validRules().copy(
                setup = validRules().setup.copy(
                    steps = listOf(
                        RuleItem("Setup", `when` = Condition.Predicate("ghost", SpecValue.Eq("std")))
                    )
                )
            ),
            "specificity 'format' has no options [nope]" to validRules().copy(
                turn = validRules().turn.copy(
                    phases = listOf(
                        Phase("Draw", `when` = Condition.Predicate("format", SpecValue.Eq("nope")))
                    )
                )
            ),
            "expects an int, got 'three'" to validRules().copy(
                win = validRules().win.copy(
                    winConditions = listOf(
                        RuleItem("Win", `when` = Condition.Predicate("players", SpecValue.Eq("three")))
                    )
                )
            ),
            "value 5 is outside 2..4" to validRules().copy(
                keywords = validRules().keywords.copy(
                    keywords = listOf(
                        KeywordItem(
                            "Flying",
                            `when` = Condition.Predicate("players", SpecValue.NoneOf(listOf("5")))
                        )
                    )
                )
            ),
        )

        cases.forEach { (expected, rules) ->
            assertValidationFailure(expected) { checkWhenReferences("test-game", rules) }
        }
    }

    @Test
    fun `validation reports broken page refs`() {
        val cases = listOf(
            "references unknown rulebook 'nope'" to validRules().copy(
                setup = validRules().setup.copy(pages = listOf(PageRef("nope", listOf(1))))
            ),
            "lists no pages" to validRules().copy(
                win = validRules().win.copy(pages = listOf(PageRef("official")))
            ),
        )

        cases.forEach { (expected, rules) ->
            assertValidationFailure(expected) { checkPageRefs("test-game", rules) }
        }
    }

    @Test
    fun `validation reports duplicate rulebook ids`() {
        val official = validRules().manifest.rulebooks.single()
        val manifest = validRules().manifest.copy(rulebooks = listOf(official, official))

        assertValidationFailure("duplicate rulebook ids") { checkManifestWellFormed("test-game", manifest) }
    }

    private fun manifestWith(vararg extra: SpecificityDef): RulesManifest {
        val valid = validRules().manifest
        val replaced = extra.map { it.id }.toSet()
        return valid.copy(
            specificities = extra.toList() + valid.specificities.filter { it.id !in replaced }
        )
    }

    /** A game that satisfies every check; broken cases below are single mutations of it. */
    private fun validRules(): GameRules = GameRules(
        manifest = RulesManifest(
            specificities = listOf(
                SpecificityDef(
                    id = "format", name = "Format", type = SpecificityType.SINGLE,
                    options = listOf(OptionDef("std", "Standard"), OptionDef("cmd", "Commander"))
                ),
                SpecificityDef(id = "players", name = "Players", type = SpecificityType.NUMBER, min = 2, max = 4),
                SpecificityDef(
                    id = "ext", name = "Extensions", type = SpecificityType.MULTI,
                    options = listOf(
                        OptionDef("a", "Extension A"),
                        OptionDef("b", "Extension B", requires = listOf("a"))
                    )
                ),
            ),
            rulebooks = listOf(
                RulebookDef(id = "official", name = "Official Rules", asset = "rulebooks/official.pdf")
            )
        ),
        setup = SetupContent(
            pages = listOf(PageRef("official", listOf(3))),
            steps = listOf(
                RuleItem(
                    "Shuffle",
                    listOf("Shuffle your deck."),
                    `when` = Condition.Predicate("format", SpecValue.Eq("std"))
                )
            )
        ),
        turn = TurnContent(
            pages = listOf(PageRef("official", listOf(4, 5))),
            phases = listOf(
                Phase(
                    "Draw Phase",
                    steps = listOf(
                        RuleItem(
                            "Draw",
                            listOf("Draw a card."),
                            `when` = Condition.Predicate("players", SpecValue.Eq("3"))
                        )
                    ),
                    `when` = Condition.Predicate("format", SpecValue.NoneOf(listOf("cmd")))
                )
            )
        ),
        win = WinContent(
            winConditions = listOf(
                RuleItem("Reduce life", `when` = Condition.Predicate("ext", SpecValue.Eq("b")))
            )
        ),
        keywords = KeywordsContent(
            keywords = listOf(
                KeywordItem("Flying", `when` = Condition.Predicate("ext", SpecValue.NoneSelected))
            )
        )
    )

    private fun assertValidationFailure(expected: String, block: () -> Unit) {
        val error = try {
            block()
            null
        } catch (e: AssertionError) {
            e
        }
        assertNotNull("expected a validation failure containing '$expected'", error)
        val message = error!!.message.orEmpty()
        assertTrue("'$message' should name the game folder", message.startsWith("game 'test-game':"))
        assertTrue("'$message' should contain '$expected'", message.contains(expected))
    }

    private fun gameFolders(): List<File> =
        RULES_DIR.listFiles { file -> file.isDirectory }.orEmpty().sortedBy { it.name }

    private suspend fun rulesFor(folder: File): GameRules {
        val rules = repository.getRules(folder.name)
        assertNotNull("game '${folder.name}' must parse", rules)
        return rules!!
    }

    /** Unique spec/option/rulebook ids, `requires` targets existing options and stays acyclic, sane numbers. */
    private fun checkManifestWellFormed(gameId: String, manifest: RulesManifest) {
        val duplicateSpecs = manifest.specificities.map { it.id }
            .groupingBy { it }.eachCount().filterValues { it > 1 }.keys
        if (duplicateSpecs.isNotEmpty()) {
            invalid(gameId, "duplicate specificity ids $duplicateSpecs")
        }
        val duplicateRulebooks = manifest.rulebooks.map { it.id }
            .groupingBy { it }.eachCount().filterValues { it > 1 }.keys
        if (duplicateRulebooks.isNotEmpty()) {
            invalid(gameId, "duplicate rulebook ids $duplicateRulebooks")
        }
        manifest.specificities.forEach { checkSpecificity(gameId, it) }
    }

    private fun checkSpecificity(gameId: String, spec: SpecificityDef) {
        val optionIds = spec.options.map { it.id }
        val duplicateOptions = optionIds.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
        if (duplicateOptions.isNotEmpty()) {
            invalid(gameId, "specificity '${spec.id}' has duplicate option ids $duplicateOptions")
        }

        val known = optionIds.toSet()
        spec.options.forEach { option ->
            val unknown = option.requires.filterNot { it in known }
            if (unknown.isNotEmpty()) {
                invalid(
                    gameId,
                    "specificity '${spec.id}' option '${option.id}' requires unknown options $unknown"
                )
            }
        }
        checkRequiresAcyclic(gameId, spec)

        when (spec.type) {
            SpecificityType.NUMBER -> {
                val min = spec.min
                val max = spec.max
                if (min == null || max == null) {
                    invalid(gameId, "number specificity '${spec.id}' must declare both min and max")
                }
                if (min > max) {
                    invalid(gameId, "number specificity '${spec.id}' has min $min > max $max")
                }
            }
            // validSelections enumerates every subset with an Int bitmask, so 31 options overflow.
            SpecificityType.MULTI -> if (spec.options.size > MAX_MULTI_OPTIONS) {
                invalid(
                    gameId,
                    "multi specificity '${spec.id}' has ${spec.options.size} options; at most " +
                        "$MAX_MULTI_OPTIONS are supported (the engine enumerates 2^n subsets " +
                        "in an Int bitmask)"
                )
            }
            SpecificityType.SINGLE -> Unit
        }
    }

    private fun checkRequiresAcyclic(gameId: String, spec: SpecificityDef) {
        val requires = spec.options.associate { it.id to it.requires }
        val settled = mutableSetOf<String>()

        fun visit(optionId: String, path: List<String>) {
            if (optionId in settled) return
            if (optionId in path) {
                invalid(
                    gameId,
                    "specificity '${spec.id}' has a requires cycle: " +
                        (path + optionId).joinToString(" -> ")
                )
            }
            requires[optionId].orEmpty().forEach { visit(it, path + optionId) }
            settled += optionId
        }

        requires.keys.sorted().forEach { visit(it, emptyList()) }
    }

    /** Every `when` predicate names a declared spec and values that spec can actually hold. */
    private fun checkWhenReferences(gameId: String, rules: GameRules) {
        val specs = rules.manifest.specificities.associateBy { it.id }
        allConditions(rules).forEach { site ->
            predicatesIn(site.condition).forEach { predicate ->
                val spec = specs[predicate.specId] ?: invalid(
                    gameId,
                    "${site.where} references unknown specificity '${predicate.specId}'"
                )
                checkPredicateValue(gameId, site.where, spec, predicate.value)
            }
        }
    }

    private fun predicatesIn(condition: Condition): List<Condition.Predicate> = when (condition) {
        is Condition.Predicate -> listOf(condition)
        is Condition.And -> condition.conditions.flatMap { predicatesIn(it) }
        is Condition.Or -> condition.conditions.flatMap { predicatesIn(it) }
        is Condition.Not -> predicatesIn(condition.condition)
    }

    private fun checkPredicateValue(
        gameId: String,
        where: String,
        spec: SpecificityDef,
        value: SpecValue,
    ) {
        val values = when (value) {
            is SpecValue.Eq -> listOf(value.value)
            is SpecValue.AllOf -> value.values
            is SpecValue.AnyOf -> value.values
            is SpecValue.NoneOf -> value.values
            SpecValue.NoneSelected -> emptyList()
        }
        when (spec.type) {
            // Numbers compare through their string form in the engine, so parse them back.
            SpecificityType.NUMBER -> {
                val min = spec.min ?: Int.MIN_VALUE
                val max = spec.max ?: Int.MAX_VALUE
                values.forEach { raw ->
                    val number = raw.toIntOrNull() ?: invalid(
                        gameId,
                        "$where: specificity '${spec.id}' expects an int, got '$raw'"
                    )
                    if (number !in min..max) {
                        invalid(
                            gameId,
                            "$where: specificity '${spec.id}' value $number is outside $min..$max"
                        )
                    }
                }
            }
            SpecificityType.SINGLE, SpecificityType.MULTI -> {
                val optionIds = spec.options.map { it.id }.toSet()
                val unknown = values.filterNot { it in optionIds }
                if (unknown.isNotEmpty()) {
                    invalid(gameId, "$where: specificity '${spec.id}' has no options $unknown")
                }
            }
        }
    }

    /** Every page ref points at a declared rulebook and carries the pages it jumps to. */
    private fun checkPageRefs(gameId: String, rules: GameRules) {
        val rulebookIds = rules.manifest.rulebooks.map { it.id }.toSet()
        allPageRefs(rules).forEach { (where, ref) ->
            if (ref.rulebook !in rulebookIds) {
                invalid(gameId, "$where references unknown rulebook '${ref.rulebook}'")
            }
            if (ref.pages.isEmpty()) {
                invalid(gameId, "$where lists no pages; omit the ref when the pages are unknown")
            }
        }
    }

    /** Every `when` in [rules] with a location label, so failures point at the offending item. */
    private fun allConditions(rules: GameRules): List<WhenSite> = buildList {
        rules.manifest.rulebooks.forEachIndexed { index, rulebook ->
            add(WhenSite("manifest.rulebooks[$index] '${rulebook.name}'", rulebook.`when`))
        }
        allPageRefs(rules).forEach { (where, ref) -> add(WhenSite(where, ref.`when`)) }
        rules.setup.steps.forEachIndexed { index, item ->
            add(WhenSite(itemWhere("setup.steps[$index]", item), item.`when`))
        }
        rules.turn.phases.forEachIndexed { index, phase ->
            add(WhenSite("turn.phases[$index] '${phase.name}'", phase.`when`))
            phase.steps.forEachIndexed { stepIndex, item ->
                add(WhenSite(itemWhere("turn.phases[$index].steps[$stepIndex]", item), item.`when`))
            }
        }
        rules.win.winConditions.forEachIndexed { index, item ->
            add(WhenSite(itemWhere("win.winConditions[$index]", item), item.`when`))
        }
        rules.keywords.keywords.forEachIndexed { index, keyword ->
            add(WhenSite("keywords.keywords[$index] '${keyword.name}'", keyword.`when`))
        }
    }

    private fun allPageRefs(rules: GameRules): List<Pair<String, PageRef>> = buildList {
        rules.setup.pages.forEachIndexed { index, ref -> add("setup.pages[$index]" to ref) }
        rules.turn.pages.forEachIndexed { index, ref -> add("turn.pages[$index]" to ref) }
        rules.win.pages.forEachIndexed { index, ref -> add("win.pages[$index]" to ref) }
        rules.keywords.pages.forEachIndexed { index, ref -> add("keywords.pages[$index]" to ref) }
    }

    private fun itemWhere(where: String, item: RuleItem): String =
        item.title?.let { "$where '$it'" } ?: where

    private fun invalid(gameId: String, message: String): Nothing =
        throw AssertionError("game '$gameId': $message")

    private data class WhenSite(val where: String, val condition: Condition)

    private companion object {
        val RULES_DIR = File("src/main/assets/rules")

        /** Upper bound on options of one MULTI spec; `1 shl 31` overflows the engine's bitmask. */
        const val MAX_MULTI_OPTIONS = 30
    }
}
