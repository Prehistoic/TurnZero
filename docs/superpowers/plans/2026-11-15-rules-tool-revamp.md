# Rules Tool Revamp Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the Rules tool's tab-based UI and hardcoded Kotlin content with a parameter-aware, JSON-driven sub-tool browser (Setup / Turn Structure / Win Condition / Keywords / Rulebook) supporting TCG formats and board-game player counts and extensions.

**Architecture:** Per-game JSON assets (`assets/rules/<gameId>/`) parsed with kotlinx.serialization into `@Serializable` models; a pure `Condition`/`Selection` engine filters content against a persisted per-game specificity selection; `RulesTool` renders its own top bar (new `Tool.providesOwnTopBar` opt-out) with the five fixed sub-tools as internal navigation state, bottom nav untouched.

**Tech Stack:** Kotlin 2.2.10, Jetpack Compose M3, Hilt, kotlinx-serialization-json 1.7.3 (already in libs.versions.toml), DataStore Preferences, JUnit4 + MockK + Robolectric.

**Spec:** docs/superpowers/specs/2026-11-14-rules-tool-revamp-design.md — the plan argues from the spec; read both.

**Model note:** Planning, design, and review tasks run on deepseek-v4-pro. **Implementation tasks run on DeepSeek V4 Flash** (cheaper) — when dispatching implementation subagents, pass `model: "opencode-go/deepseek-v4-flash"`. Review subagents stay on the default (Pro).

## Global Constraints

- Build commands from repo root (WSL): `ANDROID_HOME=/mnt/c/Users/matla/AppData/Local/Android/Sdk ./gradlew <task>`.
  - `TEST` := `ANDROID_HOME=/mnt/c/Users/matla/AppData/Local/Android/Sdk ./gradlew testDebugUnitTest`
  - `BUILD` := `ANDROID_HOME=/mnt/c/Users/matla/AppData/Local/Android/Sdk ./gradlew assembleDebug`
- Min SDK 28, target/compile 37, Kotlin 2.2.10 via AGP 9.2.1. Do NOT apply `org.jetbrains.kotlin.android`.
- No new dependencies. kotlinx-serialization-json is already applied in `:app`.
- All Rules content lives in `app/src/main/assets/rules/<gameId>/{manifest,setup,turn,win,keywords}.json`; images are asset paths (e.g. `rulebooks/images/foo.png`).
- `when` grammar exactly per spec: implicit AND for multi-key maps and value lists; explicit `and`/`or`/`not` objects; `[]` = "none selected" (multi only); unknown spec/option ids evaluate to non-match. JSON `pages` numbers are **1-based** (rulebook page numbers); viewer indices are 0-based (convert in the VM).
- Sub-tool order is fixed: SETUP, TURN, WIN, KEYWORDS, RULEBOOK.
- Naming: composables PascalCase, functions camelCase, constants SCREAMING_SNAKE_CASE. New state holder is named **`RulesToolUiState`** (legacy `RulesUiState` lives in the legacy `RulesModels.kt` until Task 23 — do not reuse the name).
- `**bold**` inline emphasis in `text`/`bullets` strings; reuse `parseBoldText` from `core:ui`.
- Tests mandatory; test names use `` `method_scenario_expected` ``; every task ends with `TEST` green and a Conventional Commit on branch `feature/rules-tool-revamp` (Task 0 creates it).
- Logging: `android.util.Log`, tag `TZ/Rules`; never log user data.

## Review Focus

1. **Missing/unparseable manifest** (bad JSON, missing file): Rules tool shows an empty state, never crashes. → Task 5 repo test + Task 14 empty-state test.
2. **`when` referencing undeclared ids** (typo'd specId, option id, or `PageRef.rulebook`): content hidden at runtime, flagged by validation test. → Task 15.
3. **Extension dependency chains** (a←b←c): select c ⇒ {a,b,c}; deselect a ⇒ {} removed all. → Task 3.
4. **Unsatisfiable `when`** (e.g. extension b selected while prerequisite a is `not`): validation test reports the dead rule. → Task 4 + Task 15.
5. **Jump-to-pages with no matching `PageRef`**: falls back to the Rulebook sub-tool (auto-selected single rulebook at page 1, or the selector), never opens a wrong page. → Task 6 ViewModel test.

---

### Task 0: Workspace setup

**Files:** none

- [ ] **Step 1: Create the feature branch**

```bash
git checkout main && git pull
git checkout -b feature/rules-tool-revamp
```

- [ ] **Step 2: Baseline build**

Run: `TEST`
Expected: all existing tests pass (they include the legacy Rules tests that still compile).

---

### Task 1: `Tool.providesOwnTopBar` opt-out

**Files:**
- Modify: `core/tools-api/src/main/java/com/turnzero/core/tools/api/Tool.kt`
- Modify: `app/src/main/java/com/turnzero/navigation/TZNavHost.kt` (ToolDetail `composable`, lines ~279-310)
- Test: `core/tools-api/src/test/java/com/turnzero/core/tools/api/ToolDefaultsTest.kt`

**Interfaces:**
- Produces: `Tool.providesOwnTopBar: Boolean` (default `false`). When `true`, `TZNavHost` renders `tool.Screen(onBack)` directly (no default `TZTopBar`, no wrapping `Column`/`Box`); the tool renders its own top bar.

- [ ] **Step 1: Write the failing test**

```kotlin
class ToolDefaultsTest {
    private val tool = object : Tool {
        override val id = "t"
        override val name = "T"
        override val icon = Icons.Filled.Build
        override val supportedGames = setOf("*")
        @Composable override fun Screen(onBack: () -> Unit) {}
    }

    @Test fun `providesOwnTopBar defaults to false`() {
        assertFalse(tool.providesOwnTopBar)
    }

    @Test fun `showBottomBar defaults to true`() {
        assertTrue(tool.showBottomBar)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `TEST`
Expected: FAIL — `providesOwnTopBar` unresolved.

- [ ] **Step 3: Implement**

Add `val providesOwnTopBar: Boolean get() = false` to `Tool` (below `showBottomBar`). In `TZNavHost`'s ToolDetail composable, inside the existing `CompositionLocalProvider`, branch:

```kotlin
if (tool.providesOwnTopBar) {
    tool.Screen(onBack = { navController.popBackStack() })
} else {
    Column {
        TZTopBar(/* unchanged */)
        Box(/* unchanged */) { tool.Screen(onBack = { navController.popBackStack() }) }
    }
}
```

The tool-switcher/options-sheet blocks below stay unchanged.

- [ ] **Step 4: Run test to verify it passes**

Run: `TEST`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add core/tools-api app/src/main/java/com/turnzero/navigation/TZNavHost.kt
git commit -m "feat: add providesOwnTopBar opt-out to Tool contract"
```

---

### Task 2: JSON content models + `Condition` serializer

**Files:**
- Create: `app/src/main/java/com/turnzero/tools/rules/RulesContentModels.kt`
- Test: `app/src/test/java/com/turnzero/tools/rules/RulesJsonTest.kt`

**Interfaces:**
- Produces (all `@Serializable`, package `com.turnzero.tools.rules`):

```kotlin
data class RulesManifest(val specificities: List<SpecificityDef> = emptyList(), val rulebooks: List<RulebookDef> = emptyList())
enum class SpecificityType { @SerialName("single") SINGLE, @SerialName("number") NUMBER, @SerialName("multi") MULTI }
data class SpecificityDef(val id: String, val name: String, val type: SpecificityType, val options: List<OptionDef> = emptyList(), val min: Int? = null, val max: Int? = null)
data class OptionDef(val id: String, val name: String, val requires: List<String> = emptyList())
data class RulebookDef(val id: String, val name: String, val asset: String, @SerialName("when") val `when`: Condition = Condition.ALWAYS)
data class PageRef(val rulebook: String, val pages: List<Int> = emptyList(), @SerialName("when") val `when`: Condition = Condition.ALWAYS)
data class RuleItem(val title: String? = null, val text: List<String> = emptyList(), val bullets: List<String> = emptyList(), val image: String? = null, @SerialName("when") val `when`: Condition = Condition.ALWAYS)
data class Phase(val name: String, val steps: List<RuleItem> = emptyList(), @SerialName("when") val `when`: Condition = Condition.ALWAYS)
data class SetupContent(val pages: List<PageRef> = emptyList(), val steps: List<RuleItem> = emptyList())
data class TurnContent(val pages: List<PageRef> = emptyList(), val phases: List<Phase> = emptyList())
data class WinContent(val pages: List<PageRef> = emptyList(), val winConditions: List<RuleItem> = emptyList())
data class KeywordsContent(val pages: List<PageRef> = emptyList(), val keywords: List<KeywordItem> = emptyList())
data class KeywordItem(val name: String, val category: String? = null, val text: List<String> = emptyList(), val bullets: List<String> = emptyList(), @SerialName("when") val `when`: Condition = Condition.ALWAYS)
data class GameRules(val manifest: RulesManifest, val setup: SetupContent, val turn: TurnContent, val win: WinContent, val keywords: KeywordsContent)
```

- Produces: `Condition` sealed interface + `SpecValue` sealed interface with custom serializers.

```kotlin
@Serializable(with = ConditionSerializer::class)
sealed interface Condition {
    data class Predicate(val specId: String, val value: SpecValue) : Condition
    data class And(val conditions: List<Condition>) : Condition
    data class Or(val conditions: List<Condition>) : Condition
    data class Not(val condition: Condition) : Condition
    companion object { val ALWAYS: Condition = And(emptyList()) }
}

@Serializable(with = SpecValueSerializer::class)
sealed interface SpecValue {
    data class Eq(val value: String) : SpecValue
    data class AllOf(val values: List<String>) : SpecValue
    data class AnyOf(val values: List<String>) : SpecValue
    data class NoneOf(val values: List<String>) : SpecValue
    data object NoneSelected : SpecValue
}
```

**Serializer mapping** (JSON → model):
- `Condition`: `{}` → `And([])`; `{"and":[...]}` → `And`; `{"or":[...]}` → `Or`; `{"not":X}` → `Not(X as Condition)`; any other object `{specId: value, ...}` → `And(listOf(Predicate(specId, specValue)))` one per entry (entries keep file order).
- `SpecValue`: JSON string → `Eq`; `[]` → `NoneSelected`; `["a","b"]` → `AllOf`; `{"or":["a","b"]}` → `AnyOf`; `{"and":["a","b"]}` → `AllOf`; `{"not":"a"}` / `{"not":["a","b"]}` → `NoneOf`.
- Serialize back canonically: `And([])` → `{}`; `And(all Predicates with distinct specIds)` → merged map; `And(other)` → `{"and":[...]}`; `Or` → `{"or":[...]}`; `Not(c)` → `{"not":c}`; `Predicate` → `{specId: value}`; `Eq` → string; `AllOf` → list; `AnyOf` → `{"or":[...]}`; `NoneOf(v)` → `{"not":v}` (single value as bare string); `NoneSelected` → `[]`.

Implement with custom `KSerializer`s (`JsonDecoder.decodeJsonElement()` then a shape-based parse; serialize builds `JsonObject`/`JsonArray`/`JsonPrimitive`).

- [ ] **Step 1: Write the failing tests**

```kotlin
class RulesJsonTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test fun `parses manifest with all specificity types and requires`()
        // manifest JSON with single/number/multi; assert ids, names, types, min/max, requires list

    @Test fun `parses every when grammar shape`()
        // "when": {} → And(empty); {"format":"commander"} → And([Predicate("format", Eq)]);
        // {"format":["a","b"]} → AllOf; {"format":{"or":["a","b"]}} → AnyOf;
        // {"extensions":{"not":"a"}} → NoneOf; {"extensions":[]} → NoneSelected;
        // {"or":[{"format":"standard"},{"extensions":["a"]}]} → Or([And(...), And(...)]);
        // {"not":{"extensions":["a"]}} → Not(And(...))

    @Test fun `absent optional fields use defaults`()
        // item without title/text/bullets/image/when → defaults + ALWAYS

    @Test fun `serialize then parse round trips canonical forms`()
        // for each shape above: parse(encodeToString(parsed)) == parsed

    @Test fun `unknown top-level keys are ignored`()
        // extra "future_field" in manifest parses fine
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `TEST`
Expected: FAIL — unresolved references.

- [ ] **Step 3: Implement** `RulesContentModels.kt` per the Interfaces block. Parse errors from malformed input must throw (the repository catches them — Task 5).

- [ ] **Step 4: Run tests to verify they pass**

Run: `TEST`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/turnzero/tools/rules/RulesContentModels.kt app/src/test/java/com/turnzero/tools/rules/RulesJsonTest.kt
git commit -m "feat: add JSON content models and when-condition serializer for rules"
```

---

### Task 3: `RulesEngine` — `matches`, select/deselect with dependency closure

**Files:**
- Create: `app/src/main/java/com/turnzero/tools/rules/RulesEngine.kt`
- Test: `app/src/test/java/com/turnzero/tools/rules/RulesEngineTest.kt`

**Interfaces:**
- Consumes: `Condition`, `SpecValue`, `RulesManifest` (Task 2).
- Produces:

```kotlin
data class Selection(
    val single: Map<String, String> = emptyMap(),      // specId -> optionId
    val number: Map<String, Int> = emptyMap(),         // specId -> value
    val multi: Map<String, Set<String>> = emptyMap(),  // specId -> selected optionIds
)

fun matches(condition: Condition, selection: Selection): Boolean
fun defaultSelection(manifest: RulesManifest): Selection
fun selectOption(manifest: RulesManifest, selection: Selection, specId: String, optionId: String): Selection
fun deselectOption(manifest: RulesManifest, selection: Selection, specId: String, optionId: String): Selection
fun withNumber(manifest: RulesManifest, selection: Selection, specId: String, value: Int): Selection
```

Semantics:
- `matches`: `Predicate(specId, v)` resolves the spec by which map holds `specId` (`single` → string equality set: `Eq`=equal, `AllOf`=equal to every value, `AnyOf`=in list, `NoneOf`=not in list, `NoneSelected`=false; `number` → `selection.number[specId]?.toString()` compared the same way; `multi` → `Eq`=in set, `AllOf`=all in set, `AnyOf`=any in set, `NoneOf`=none in set, `NoneSelected`=set null-or-empty). `And`=all; `Or`=any; `Not`=negation; `And([])`=true. Unknown specId → false.
- `defaultSelection`: SINGLE → first option id (`""` if none); NUMBER → `min ?: 0`; MULTI → empty set.
- `selectOption` (SINGLE: replace value; MULTI: `set + optionId + transitive closure of requires` walking `OptionDef.requires` of the same spec; NUMBER: no-op): `deselectOption` (MULTI: remove `optionId` plus every option that transitively `requires` it; SINGLE/NUMBER: no-op): `withNumber` (clamp to `min..max`).
- Assume `requires` is acyclic (Task 15 validates); do not guard against cycles here.

- [ ] **Step 1: Write the failing tests**

```kotlin
class RulesEngineTest {
    // helpers: multiManifest(requires = mapOf("exp-b" to listOf("exp-a"), "exp-c" to listOf("exp-b")))
    //          singleManifest(), numberManifest(min=2, max=4), sel(...)

    @Test fun `matches Eq AllOf AnyOf NoneOf NoneSelected for multi`()
    @Test fun `matches Eq AnyOf NoneOf for single`()
    @Test fun `matches stringified number equality`()
    @Test fun `matches And Or Not combinations`()
    @Test fun `matches empty And as true`()
    @Test fun `matches unknown specId as false`()
    @Test fun `defaultSelection picks first option and min`()
    @Test fun `selectOption adds transitive requires closure`()
        // select exp-c on {} → setOf(exp-a, exp-b, exp-c)
    @Test fun `deselectOption removes transitive dependents`()
        // deselect exp-a from {a,b,c} → emptySet
    @Test fun `selectOption on single replaces value`()
    @Test fun `withNumber clamps to min and max`()
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `TEST`
Expected: FAIL.

- [ ] **Step 3: Implement** `RulesEngine.kt` per the Interfaces block.

- [ ] **Step 4: Run tests to verify they pass**

Run: `TEST`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/turnzero/tools/rules/RulesEngine.kt app/src/test/java/com/turnzero/tools/rules/RulesEngineTest.kt
git commit -m "feat: add rules condition matcher and specificity selection engine"
```

---

### Task 4: `validSelections` + `isSatisfiable`

**Files:**
- Modify: `app/src/main/java/com/turnzero/tools/rules/RulesEngine.kt`
- Test: `app/src/test/java/com/turnzero/tools/rules/RulesSatisfiabilityTest.kt`

**Interfaces:**
- Consumes: `matches`, `Selection`, `RulesManifest` (Tasks 2-3).
- Produces:

```kotlin
fun validSelections(manifest: RulesManifest): List<Selection>
fun isSatisfiable(condition: Condition, manifest: RulesManifest): Boolean
```

Semantics: `validSelections` = cross-product of every SINGLE option × every NUMBER value in `min..max` × every dependency-closed MULTI subset (all 2ⁿ subsets, keep those where every selected option's `requires` is also selected). `isSatisfiable` = `validSelections(manifest).any { matches(condition, it) }`.

- [ ] **Step 1: Write the failing tests**

```kotlin
class RulesSatisfiabilityTest {
    @Test fun `validSelections enumerates only dependency-closed multi subsets`()
        // manifest with exps base/exp-a/exp-b(requires exp-a): multi parts == {}, {base}, {a}, {a,b}, {base,a}, {base,a,b} — never {b} or {base,b}

    @Test fun `validSelections enumerates full number range`()

    @Test fun `isSatisfiable false when requires contradicted`()
        // And(Predicate(exp-b, Eq), Predicate(exp-a, NoneOf)) with b requires a → false

    @Test fun `isSatisfiable true for single extension condition`()
        // Eq(exp-b) → true (closure selects a automatically)

    @Test fun `isSatisfiable true for NoneSelected`()

    @Test fun `isSatisfiable respects format and extensions conjunction`()
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `TEST`
Expected: FAIL.

- [ ] **Step 3: Implement** per the Interfaces block.

- [ ] **Step 4: Run tests to verify they pass**

Run: `TEST`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/turnzero/tools/rules/RulesEngine.kt app/src/test/java/com/turnzero/tools/rules/RulesSatisfiabilityTest.kt
git commit -m "feat: add valid-selection enumeration and when-satisfiability check"
```

---

### Task 5: `RulesAssetSource` + `RulesRepository`

**Files:**
- Create: `app/src/main/java/com/turnzero/tools/rules/RulesAssetSource.kt`
- Create: `app/src/main/java/com/turnzero/tools/rules/RulesRepository.kt`
- Modify: `app/src/main/java/com/turnzero/di/RulesModule.kt` (add bindings; keep the legacy `RulesProvider` binding until Task 23)
- Test: `app/src/test/java/com/turnzero/tools/rules/RulesRepositoryTest.kt`

**Interfaces:**
- Consumes: `GameRules` models (Task 2).
- Produces:

```kotlin
interface RulesAssetSource { fun read(path: String): String? }

class AndroidRulesAssetSource @Inject constructor(
    @ApplicationContext private val context: Context,
) : RulesAssetSource   // context.assets.open(path).bufferedReader().use { it.readText() }; IOException → null

interface RulesRepository { suspend fun getRules(gameId: String): GameRules? }

@Singleton
class AssetRulesRepository @Inject constructor(
    private val source: RulesAssetSource,
) : RulesRepository
```

Semantics: `getRules` runs on `Dispatchers.IO`; reads `rules/<gameId>/manifest.json`, then `setup.json`, `turn.json`, `win.json`, `keywords.json`; returns `null` (and `Log.w("TZ/Rules", ...)`) if the manifest is missing or any parse fails; caches **non-null** results in a `ConcurrentHashMap<String, GameRules>` (nulls are retried). JSON config: `Json { ignoreUnknownKeys = true }`.

- [ ] **Step 1: Write the failing tests**

```kotlin
class RulesRepositoryTest {
    // FakeRulesAssetSource(paths: Map<String, String>) : RulesAssetSource { read = paths[path] }

    @Test fun `getRules parses a complete game folder`()
        // fake source with all 5 files → non-null GameRules with manifest/content fields

    @Test fun `getRules returns null when manifest missing`()

    @Test fun `getRules returns null on malformed json`()
        // setup.json contains "not json" → null, no exception

    @Test fun `getRules caches parsed rules`()
        // counting source: getRules twice → read(manifest) called once
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `TEST`
Expected: FAIL.

- [ ] **Step 3: Implement** per the Interfaces block. In `RulesModule.kt`, add:

```kotlin
@Binds @Singleton abstract fun bindRulesAssetSource(impl: AndroidRulesAssetSource): RulesAssetSource
@Binds @Singleton abstract fun bindRulesRepository(impl: AssetRulesRepository): RulesRepository
```

(keep the existing `bindRulesProvider` — removed in Task 23).

- [ ] **Step 4: Run tests to verify they pass**

Run: `TEST`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/turnzero/tools/rules/RulesAssetSource.kt app/src/main/java/com/turnzero/tools/rules/RulesRepository.kt app/src/main/java/com/turnzero/di/RulesModule.kt app/src/test/java/com/turnzero/tools/rules/RulesRepositoryTest.kt
git commit -m "feat: add asset-backed rules repository with caching"
```

---

### Task 6: `RulesToolUiState`, prefs keys, `RulesToolViewModel`

**Files:**
- Modify: `core/data/src/main/java/com/turnzero/core/data/UserPreferences.kt`
- Create: `app/src/main/java/com/turnzero/tools/rules/RulesToolUiState.kt`
- Create: `app/src/main/java/com/turnzero/tools/rules/RulesToolViewModel.kt` (NEW class — the legacy `RulesViewModel` stays untouched until Task 23, so every task keeps compiling)
- Test: `app/src/test/java/com/turnzero/tools/rules/RulesToolViewModelTest.kt`

**Interfaces:**
- Consumes: `RulesRepository`, `GameRules`, `Selection`, `matches`, `defaultSelection`, `selectOption`, `deselectOption`, `withNumber` (Tasks 2-5).
- Produces:

```kotlin
// UserPreferences.kt additions (legacy RULES_DEFAULT_RULESET / RULES_ENABLED_RULESETS stay until Task 23):
fun rulesSpecStringKey(gameId: String, specId: String) = stringPreferencesKey("rules_spec_string_${gameId}_$specId")
fun rulesSpecIntKey(gameId: String, specId: String) = intPreferencesKey("rules_spec_int_${gameId}_$specId")
fun rulesSpecSetKey(gameId: String, specId: String) = stringSetPreferencesKey("rules_spec_set_${gameId}_$specId")
fun rulesConfiguredKey(gameId: String) = booleanPreferencesKey("rules_configured_$gameId")
fun rulesSubToolKey(gameId: String) = stringPreferencesKey("rules_subtool_$gameId")
val RULES_LAYOUT = stringPreferencesKey("rules_layout")
```

```kotlin
enum class RuleSubTool(val id: String, val title: String, val icon: ImageVector) {
    SETUP("setup", "Setup", Icons.Filled.Build),
    TURN("turn", "Turn Structure", Icons.Filled.Loop),
    WIN("win", "Win Condition", Icons.Filled.EmojiEvents),
    KEYWORDS("keywords", "Keywords", Icons.Filled.TextFields),
    RULEBOOK("rulebook", "Rulebook", Icons.AutoMirrored.Filled.MenuBook),
}
```

```kotlin
data class RulesToolUiState(
    val gameId: String = "",
    val rules: GameRules? = null,
    val selection: Selection = Selection(),
    val draftSelection: Selection? = null,     // non-null while the specificity editor is open
    val showSpecificityDialog: Boolean = false, // centered-dialog variant (first open + pill)
    val subTool: RuleSubTool? = null,           // null = main panel
    val layout: LayoutMode = LayoutMode.GRID,
    val showLayoutSheet: Boolean = false,
    val turnPhaseIndex: Int? = null,
    val turnStepIndex: Int? = null,
    val keywordQuery: String = "",
    val keywordCategory: String? = null,
    val expandedKeywordIndex: Int? = null,
    val showPdfSelector: Boolean = false,
    val selectedRulebookId: String? = null,
    val pdfInitialPage: Int = 0,                // 0-based viewer page
    val isLoading: Boolean = true,
) {
    val hasSpecificities: Boolean get() = !rules?.manifest?.specificities.isNullOrEmpty()
    val specificitySummary: String? get() // single: option name · number: "${value}P" · multi: "+N ext" if N>0 else omit part · join with " · " · null when empty
    val setupSteps: List<RuleItem> get() = rules?.setup?.steps.orEmpty().filter { matches(it.when, selection) }
    val turnPhases: List<Phase> get() = rules?.turn?.phases.orEmpty()
        .filter { matches(it.when, selection) }
        .mapNotNull { p -> val s = p.steps.filter { matches(it.when, selection) }; if (s.isEmpty()) null else p.copy(steps = s) }
    val winConditions: List<RuleItem> get() = rules?.win?.winConditions.orEmpty().filter { matches(it.when, selection) }
    val keywords: List<KeywordItem> get() = rules?.keywords?.keywords.orEmpty().filter { matches(it.when, selection) }
    val filteredKeywords: List<KeywordItem> get() // keywords sorted by name.lowercase(), then query contains (ignoreCase) on name, then category == keywordCategory
    val keywordCategories: List<String> get() = keywords.mapNotNull { it.category }.distinct().sorted()
    val matchingRulebooks: List<RulebookDef> get() = rules?.manifest?.rulebooks.orEmpty().filter { matches(it.when, selection) }
}
```

```kotlin
@HiltViewModel
class RulesToolViewModel @Inject constructor(
    private val repository: RulesRepository,
    private val dataStore: DataStore<Preferences>,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val uiState: StateFlow<RulesToolUiState>

    fun openSubTool(subTool: RuleSubTool)      // resets turn/keyword/pdf transient state; for RULEBOOK: 1 match → selectedRulebookId+page 0; >1 → showPdfSelector; persists rules_subtool_<gameId>
    fun closeSubTool()                          // subTool = null; persists removal
    fun openSpecificityEditor()                 // draftSelection = draftSelection ?: selection (no dialog flag; used by the Options sheet)
    fun openSpecificityDialog()                 // openSpecificityEditor() + showSpecificityDialog = true (first open + top-bar pill)
    fun dismissSpecificityDialog()              // draftSelection = null; showSpecificityDialog = false
    fun confirmSpecificities()                  // persist draft per spec type + configured=true; selection = draft; close dialog
    fun selectSpecificityOption(specId: String, optionId: String)  // applies selectOption/deselectOption closure to draft
    fun setSpecificityNumber(specId: String, value: Int)           // withNumber on draft
    fun setLayout(mode: LayoutMode)             // persists RULES_LAYOUT
    fun dismissLayoutSheet()
    fun openPhase(index: Int)  fun closePhase()
    fun openStep(index: Int)   fun closeStep()
    fun nextStep()  fun previousStep()          // across phase boundaries on turnPhases (see Semantics)
    fun setKeywordQuery(query: String)          // clears expandedKeywordIndex
    fun filterKeywordCategory(category: String?) // clears expandedKeywordIndex
    fun toggleKeyword(index: Int)
    fun selectRulebook(rulebookId: String)      // showPdfSelector=false; selectedRulebookId=id; pdfInitialPage=0
    fun dismissPdfSelector()
    fun jumpToPages()                           // see Semantics
}
```

Semantics:
- `init`: load `repository.getRules(gameId)`; read prefs (`dataStore.data.first()`); `layout` from `RULES_LAYOUT` (fallback GRID on any exception); if manifest has specificities and `rulesConfiguredKey` is false → selection = `defaultSelection(manifest)`, `draftSelection = selection`, `showSpecificityDialog = true`; else selection = persisted per-spec values (fall back per spec to `defaultSelection` values); restore `subTool` from `rulesSubToolKey` when rules != null; `isLoading = rules == null`.
- `nextStep`/`previousStep`: work on `turnPhases` (already filtered); next: `stepIndex+1` within the phase, else advance to `(phaseIndex+1, stepIndex=0)`; previous: `stepIndex-1`, else `(phaseIndex-1, last visible step)`. No-op at the boundaries.
- `jumpToPages` (only when `subTool` is SETUP/TURN/WIN/KEYWORDS): resolve `pages` from the corresponding content; take `firstOrNull { matches(it.when, selection) }`; if it references an existing rulebook and `pages` is non-empty → `subTool = RULEBOOK`, `selectedRulebookId = ref.rulebook`, `pdfInitialPage = ref.pages.first() - 1` (JSON is 1-based), persist sub-tool. Otherwise fall back to `openSubTool(RULEBOOK)`.

- [ ] **Step 1: Write the failing tests** (new `RulesToolViewModelTest.kt`; use `mockk<DataStore<Preferences>>(relaxed = true)` with `every { dataStore.data } returns flowOf(...)` as the legacy tests did, and a fake `RulesRepository`):

```kotlin
class RulesToolViewModelTest : TZViewModelTest() {
    @Test fun `init without rules leaves empty state`()
    @Test fun `init with rules and no specificities shows no dialog and no summary`()
    @Test fun `init with specificities and not configured shows dialog with defaults`()
    @Test fun `init with specificities and configured restores persisted selection`()
        // prefs: rules_spec_string_game_fmt=commander, configured=true → selection.single["format"]=="commander", dialog false
    @Test fun `confirmSpecificities persists selection and closes dialog`()
        // verify { dataStore.edit(any()) } called; state draft null, dialog false
    @Test fun `dismissSpecificityDialog reverts draft without persisting`()
    @Test fun `selectSpecificityOption applies requires closure to draft`()
    @Test fun `openSubTool persists sub tool and fresh view model restores it`()
    @Test fun `closeSubTool clears persisted sub tool`()
    @Test fun `jumpToPages opens rulebook at referenced page`()
        // setup pages: [{rulebook:"official", pages:[4,5], when:{}}] → selectedRulebookId=="official", pdfInitialPage==3, subTool==RULEBOOK
    @Test fun `jumpToPages falls back when no page ref matches`()
        // pages [{..., when:{format:"commander"}}] with format=standard → openSubTool(RULEBOOK) path (single rulebook auto-selected, page 0)
    @Test fun `nextStep advances within phase then across phase boundary`()
    @Test fun `previousStep goes back across phase boundary`()
    @Test fun `turnPhases drops phases with no visible steps`()
    @Test fun `filteredKeywords filters by query and category`()
    @Test fun `specificitySummary formats single number and multi parts`()
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `TEST`
Expected: FAIL — `RulesToolViewModel` unresolved.

- [ ] **Step 3: Implement** per the Interfaces block. The legacy `RulesViewModel` and its tests are left untouched (they keep passing; removed in Task 23).

- [ ] **Step 4: Run tests to verify they pass**

Run: `TEST`
Expected: PASS (legacy Rules tests still pass alongside).

- [ ] **Step 5: Commit**

```bash
git add core/data/src/main/java/com/turnzero/core/data/UserPreferences.kt app/src/main/java/com/turnzero/tools/rules/RulesToolUiState.kt app/src/main/java/com/turnzero/tools/rules/RulesToolViewModel.kt app/src/test/java/com/turnzero/tools/rules/RulesToolViewModelTest.kt
git commit -m "feat: add rules tool view model with specificity selection and sub-tool state"
```

---

### Task 7: `RulesItemContent` renderer

**Files:**
- Create: `app/src/main/java/com/turnzero/tools/rules/RulesItemContent.kt`
- Test: `app/src/test/java/com/turnzero/tools/rules/RulesItemContentTest.kt`

**Interfaces:**
- Consumes: `RuleItem` (Task 2), `parseBoldText` from `com.turnzero.core.ui.components`.
- Produces:

```kotlin
@Composable
fun RulesItemContent(item: RuleItem, modifier: Modifier = Modifier)

@Composable
fun RulesAssetImage(assetPath: String, modifier: Modifier = Modifier, contentDescription: String? = null)
```

Rendering: optional `title` (titleSmall, SemiBold) → each `text` entry (bodyMedium, `parseBoldText`) → each `bullets` entry ("• " prefix, bodyMedium, `parseBoldText`) → optional `image` (`RulesAssetImage`: decode via `context.assets.open(assetPath)` with `BitmapFactory`, `fillMaxWidth()`, `RoundedCornerShape(8.dp)`; null bitmap renders nothing).

- [ ] **Step 1: Write the failing test** (Robolectric + `createComposeRule`, same pattern as `DiceAnimationInitialValueTest`):

```kotlin
@RunWith(RobolectricTestRunner::class)
class RulesItemContentTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun `renders title text bullets and skips missing image`()
        // setContent { RulesItemContent(RuleItem(title="T", text=listOf("para"), bullets=listOf("bul"))) }
        // onNodeWithText("T") / onNodeWithText("para") / onNodeWithText("• bul") assertIsDisplayed

    @Test fun `bold markers render without literal asterisks`()
        // text "has **bold** inside" → onNodeWithText("has bold inside") displayed
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `TEST`
Expected: FAIL.

- [ ] **Step 3: Implement** per the Interfaces block.

- [ ] **Step 4: Run test to verify it passes**

Run: `TEST`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/turnzero/tools/rules/RulesItemContent.kt app/src/test/java/com/turnzero/tools/rules/RulesItemContentTest.kt
git commit -m "feat: add rules content item renderer with asset images"
```

---

### Task 8: Setup carousel

**Files:**
- Create: `app/src/main/java/com/turnzero/tools/rules/RulesSetupScreen.kt`
- Test: `app/src/test/java/com/turnzero/tools/rules/RulesSetupScreenTest.kt`

**Interfaces:**
- Consumes: `RulesItemContent` (Task 7).
- Produces:

```kotlin
@Composable
fun SetupCarousel(steps: List<RuleItem>, modifier: Modifier = Modifier)
```

Layout: `HorizontalPager` (one page per visible step; visible index = page); each page is a vertically scrollable `Column` with the step number rendered as a large `${index + 1}.` (headlineMedium, `colorScheme.primary`) at the top, then `RulesItemContent(step)` below; page-indicator dots under the pager (Material3 style, custom dot row is fine); `steps.isEmpty()` → centered "No setup steps available" (`TZEmptyState`).

- [ ] **Step 1: Write the failing test**

```kotlin
@RunWith(RobolectricTestRunner::class)
class RulesSetupScreenTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun `shows first step number and swipes to second`()
        // 3 steps "One"/"Two"/"Three" → "1." and "One" displayed; swipe left → "2." and "Two" displayed

    @Test fun `empty steps shows empty state`()
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `TEST`
Expected: FAIL.

- [ ] **Step 3: Implement** per the Interfaces block.

- [ ] **Step 4: Run test to verify it passes**

Run: `TEST`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/turnzero/tools/rules/RulesSetupScreen.kt app/src/test/java/com/turnzero/tools/rules/RulesSetupScreenTest.kt
git commit -m "feat: add setup carousel screen for rules"
```

---

### Task 9: Turn Structure drill-down screen

**Files:**
- Create: `app/src/main/java/com/turnzero/tools/rules/RulesTurnScreen.kt`
- Test: `app/src/test/java/com/turnzero/tools/rules/RulesTurnScreenTest.kt`

**Interfaces:**
- Consumes: `Phase`, `RulesItemContent` (Tasks 2, 7).
- Produces:

```kotlin
@Composable
fun TurnStructureScreen(
    phases: List<Phase>,
    phaseIndex: Int?,
    stepIndex: Int?,
    onOpenPhase: (Int) -> Unit,
    onOpenStep: (Int) -> Unit,
    onBackToOverview: () -> Unit,
    onBackToSteps: () -> Unit,
    onNextStep: () -> Unit,
    onPreviousStep: () -> Unit,
    modifier: Modifier = Modifier,
)
```

Layout (three levels):
- `phaseIndex == null`: `LazyColumn` of phase cards (numbered `1.` … `N.`, phase `name`, "X steps" subtitle). Tap → `onOpenPhase(index)`.
- `stepIndex == null`: `LazyColumn` of step rows (`1.` … + `title`). Tap → `onOpenStep(index)`; the screen has no internal back (the tool's top bar back calls `onBackToOverview` via the VM — still pass `onBackToOverview` for completeness).
- else: detail — `Step X of Y · <phase name>` readout, then `RulesItemContent(steps[stepIndex])`, then a bottom bar with `← Prev` and `Next →` (label `Next: <next phase name> →` when on the last step of a non-last phase; disabled at the very first step / very last step of the last phase). Buttons only, no swipe.
- `phases.isEmpty()` → empty state.

- [ ] **Step 1: Write the failing test**

```kotlin
@RunWith(RobolectricTestRunner::class)
class RulesTurnScreenTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun `overview shows phases then opens step list then detail`()
        // 2 phases (A: steps a1,a2; B: steps b1) → "A"/"B" displayed; click A → "a1"/"a2"; click a1 → "Step 1 of 2 · A"

    @Test fun `next advances steps and shows next phase label at boundary`()
        // detail of a2 → button "Next: B →" displayed; click → "Step 1 of 1 · B"

    @Test fun `previous is disabled at first step and back works`()
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `TEST`
Expected: FAIL.

- [ ] **Step 3: Implement** per the Interfaces block.

- [ ] **Step 4: Run test to verify it passes**

Run: `TEST`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/turnzero/tools/rules/RulesTurnScreen.kt app/src/test/java/com/turnzero/tools/rules/RulesTurnScreenTest.kt
git commit -m "feat: add turn structure drill-down screen for rules"
```

---

### Task 10: Win Condition screen

**Files:**
- Create: `app/src/main/java/com/turnzero/tools/rules/RulesWinScreen.kt`
- Test: `app/src/test/java/com/turnzero/tools/rules/RulesWinScreenTest.kt`

**Interfaces:**
- Consumes: `RulesItemContent` (Task 7).
- Produces:

```kotlin
@Composable
fun WinConditionScreen(conditions: List<RuleItem>, modifier: Modifier = Modifier)
```

Layout: scrollable column of bordered cards (3dp primary border, `surfaceContainer`, like the legacy VictoryTab) each wrapping `RulesItemContent`; empty → `TZEmptyState("No win conditions available")`.

- [ ] **Step 1: Write the failing test**

```kotlin
@RunWith(RobolectricTestRunner::class)
class RulesWinScreenTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun `renders each condition title`()
        // 2 conditions "Way A"/"Way B" → both displayed

    @Test fun `empty list shows empty state`()
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `TEST`
Expected: FAIL.

- [ ] **Step 3: Implement** per the Interfaces block.

- [ ] **Step 4: Run test to verify it passes**

Run: `TEST`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/turnzero/tools/rules/RulesWinScreen.kt app/src/test/java/com/turnzero/tools/rules/RulesWinScreenTest.kt
git commit -m "feat: add win condition screen for rules"
```

---

### Task 11: Keywords screen

**Files:**
- Create: `app/src/main/java/com/turnzero/tools/rules/RulesKeywordsScreen.kt`
- Test: `app/src/test/java/com/turnzero/tools/rules/RulesKeywordsScreenTest.kt`

**Interfaces:**
- Consumes: `KeywordItem`, `RulesItemContent` (Tasks 2, 7).
- Produces:

```kotlin
@Composable
fun RulesKeywordsScreen(
    keywords: List<KeywordItem>,
    searchQuery: String,
    selectedCategory: String?,
    allCategories: List<String>,
    expandedIndex: Int?,
    onToggle: (Int) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onFilterCategory: (String?) -> Unit,
    modifier: Modifier = Modifier,
)
```

Layout: port the legacy `RulesKeywordsTab` layout (search `OutlinedTextField`, active-filter `AssistChip` with count, category `SuggestionChip` row, expandable `Card` list) but render keyword content with `RulesItemContent(keyword as RuleItem-shaped)` — build a `RuleItem(title = keyword.name, text = keyword.text, bullets = keyword.bullets)`; category chip per card stays; empty → "No matching keywords".

- [ ] **Step 1: Write the failing test**

```kotlin
@RunWith(RobolectricTestRunner::class)
class RulesKeywordsScreenTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun `search filters displayed keywords`()
        // pass 2 keywords, type "aa" → only matching visible

    @Test fun `category chip filters and clear chip resets`()

    @Test fun `tap expands keyword card to show text`()
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `TEST`
Expected: FAIL.

- [ ] **Step 3: Implement** per the Interfaces block.

- [ ] **Step 4: Run test to verify it passes**

Run: `TEST`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/turnzero/tools/rules/RulesKeywordsScreen.kt app/src/test/java/com/turnzero/tools/rules/RulesKeywordsScreenTest.kt
git commit -m "feat: adapt keywords screen to new rules content model"
```

---

### Task 12: Rulebook screen + PDF viewer refactor

**Files:**
- Create: `app/src/main/java/com/turnzero/tools/rules/RulesRulebookScreen.kt`
- Create: `app/src/main/java/com/turnzero/tools/rules/RulebookPdfViewer.kt` (content moved from `RulesPdfSheet.kt`: zoom/pan/swipe/page-nav/go-to-page; drop the "Done" row; add `initialPage`)
- Note: `RulesPdfSheet.kt` stays in place (the legacy `RulesScreen.kt` still references it) — it is deleted in Task 14.
- Test: `app/src/test/java/com/turnzero/tools/rules/RulesRulebookScreenTest.kt`

**Interfaces:**
- Consumes: `RulebookDef` (Task 2).
- Produces:

```kotlin
@Composable
fun RulebookScreen(
    rulebooks: List<RulebookDef>,      // already filtered to the current selection
    selectedRulebookId: String?,
    initialPage: Int,
    showSelector: Boolean,
    onSelectRulebook: (String) -> Unit,
    onDismissSelector: () -> Unit,
    modifier: Modifier = Modifier,
)

@Composable
fun RulebookPdfViewer(assetPath: String, initialPage: Int = 0, modifier: Modifier = Modifier)
```

Layout: `showSelector` → `AlertDialog` "Choose Rules PDF" listing `rulebooks` (name buttons) + Cancel (as the legacy selector); `selectedRulebookId` resolves to a `RulebookDef` → `RulebookPdfViewer(asset = def.asset, initialPage = initialPage)`; zero matching rulebooks → `TZEmptyState("No rulebook available")`.

- [ ] **Step 1: Write the failing test**

```kotlin
@RunWith(RobolectricTestRunner::class)
class RulesRulebookScreenTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun `selector dialog lists matching rulebooks and cancel dismisses`()
        // showSelector=true, 2 rulebooks → names visible; click Cancel → onDismissSelector recorded

    @Test fun `no matching rulebooks shows empty state`()

    @Test fun `selector tap reports the chosen rulebook id`()
}
```

(The PDF renderer itself is not unit-tested — it needs a real `PdfRenderer`; verified by the migration/validation tasks and manual smoke.)

- [ ] **Step 2: Run test to verify it fails**

Run: `TEST`
Expected: FAIL.

- [ ] **Step 3: Implement** per the Interfaces block. `RulebookPdfViewer` keeps the existing `copyAssetToCache` + `PdfRenderer` logic; `initialPage` replaces the hardcoded `0` start (clamped to `0..pageCount-1` once `pageCount` is known).

- [ ] **Step 4: Run test to verify it passes**

Run: `TEST`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/turnzero/tools/rules/RulesRulebookScreen.kt app/src/main/java/com/turnzero/tools/rules/RulebookPdfViewer.kt app/src/test/java/com/turnzero/tools/rules/RulesRulebookScreenTest.kt
git commit -m "feat: add rulebook screen with reusable page-aware pdf viewer"
```

---

### Task 13: Specificity editor, dialog, pill, and tool top bar

**Files:**
- Create: `app/src/main/java/com/turnzero/tools/rules/RulesSpecificityUi.kt`
- Create: `app/src/main/java/com/turnzero/tools/rules/RulesToolTopBar.kt`
- Test: `app/src/test/java/com/turnzero/tools/rules/RulesSpecificityUiTest.kt`

**Interfaces:**
- Consumes: `RulesManifest`, `Selection`, `SpecificityType` (Tasks 2-3).
- Produces:

```kotlin
@Composable
fun SpecificityPill(text: String, onClick: () -> Unit, modifier: Modifier = Modifier)

@Composable
fun SpecificityEditor(
    manifest: RulesManifest,
    selection: Selection,
    onSelectOption: (String, String) -> Unit,
    onNumberChange: (String, Int) -> Unit,
    modifier: Modifier = Modifier,
)

@Composable
fun SpecificityDialog(
    manifest: RulesManifest,
    selection: Selection,
    onSelectOption: (String, String) -> Unit,
    onNumberChange: (String, Int) -> Unit,
    onConfirm: () -> Unit,
)

@Composable
fun RulesToolTopBar(
    title: String,
    onBack: () -> Unit,
    pillText: String?,
    onPillClick: () -> Unit,
    actionIcon: ImageVector?,
    actionContentDescription: String?,
    onActionClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
)
```

Layout:
- `SpecificityPill`: `Surface` (shape `CircleShape`, `surfaceVariant`) with text (labelLarge) — the compact top-bar chip.
- `SpecificityEditor`: for each specificity in manifest order, a labeled section: SINGLE → column of `RadioButton` + option name rows; NUMBER → row of `FilterChip`s for `min..max` ("2P"…"5P"); MULTI → column of `Checkbox` + option name rows (0..all allowed; option names appended with " · requires X" hint is optional). The editor only reports taps; the VM applies select/deselect closure.
- `SpecificityDialog`: centered `AlertDialog` (`DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)`) titled "Choose your game settings", containing the editor + confirm `TextButton` "Start".
- `RulesToolTopBar`: `TopAppBar` (surface container as `TZTopBar`) with back `IconButton`, title `Row { Text(title); if (pillText != null) SpecificityPill }`, actions: optional single `IconButton` (`actionIcon` + description) when `onActionClick != null`.

- [ ] **Step 1: Write the failing test**

```kotlin
@RunWith(RobolectricTestRunner::class)
class RulesSpecificityUiTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun `editor renders radio segmented and checkbox sections`()
        // manifest with single/number/multi → option names, "2P".."4P" chips, checkbox labels visible

    @Test fun `dialog shows Start and confirm reports clicks`()
        // click option radio → onSelectOption("format","commander") recorded; click Start → onConfirm recorded

    @Test fun `pill shows text and click fires`()
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `TEST`
Expected: FAIL.

- [ ] **Step 3: Implement** per the Interfaces block.

- [ ] **Step 4: Run test to verify it passes**

Run: `TEST`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/turnzero/tools/rules/RulesSpecificityUi.kt app/src/main/java/com/turnzero/tools/rules/RulesToolTopBar.kt app/src/test/java/com/turnzero/tools/rules/RulesSpecificityUiTest.kt
git commit -m "feat: add specificity editor dialog and rules tool top bar"
```

---

### Task 14: `RulesTool` rewrite (integration) + main panel

**Files:**
- Rewrite: `app/src/main/java/com/turnzero/tools/rules/RulesTool.kt`
- Create: `app/src/main/java/com/turnzero/tools/rules/RulesMainPanel.kt`
- Delete: `app/src/main/java/com/turnzero/tools/rules/RulesScreen.kt`, `RulesSetupTab.kt`, `RulesTurnTab.kt`, `RulesVictoryTab.kt`, `RulesKeywordsTab.kt`, `ContentBlockRenderer.kt` (rules-package wrapper — the shared `core:ui` `ContentBlockRenderer` stays), `RulesPdfSheet.kt` (superseded by `RulebookPdfViewer` from Task 12)
- Test: `app/src/test/java/com/turnzero/tools/rules/RulesMainPanelTest.kt`

**Interfaces:**
- Consumes: `RulesToolViewModel`, `RulesToolUiState`, `RuleSubTool`, all screens (Tasks 7-13), `TZTileLayout`, `LayoutMode`, `LayoutBottomSheet`, `TZEmptyState`, `LocalGameId`.
- Produces: the wiring only — no new public API.

```kotlin
@Composable
fun RulesMainPanel(
    subTools: List<RuleSubTool>,
    layoutMode: LayoutMode,
    onSubToolClick: (RuleSubTool) -> Unit,
    modifier: Modifier = Modifier,
)
```

`RulesMainPanel` uses `TZTileLayout` (grid 2 columns) with a new private tile: grid = icon 64dp primary + name (titleMedium bold, centered); list = icon 36dp + name + trailing arrow — mirror `ToolTile` visuals.

`RulesTool` (`providesOwnTopBar = true`, `supportedGames = setOf("*")`, no `TopBarActions` override):
- `Screen(onBack)`: `hiltViewModel<RulesToolViewModel>()`; collect `uiState`.
  - `rules == null` → `Column { RulesToolTopBar(title = "Rules", onBack = onBack, …no pill/action); TZEmptyState("No rules available for this game") }`.
  - else → `Column { RulesToolTopBar(...); when(subTool) { null → RulesMainPanel(...); SETUP → SetupCarousel(state.setupSteps); TURN → TurnStructureScreen(state.turnPhases, state.turnPhaseIndex, state.turnStepIndex, vm::openPhase, vm::openStep, vm::closePhase, vm::closeStep, vm::nextStep, vm::previousStep); WIN → WinConditionScreen(...); KEYWORDS → RulesKeywordsScreen(...); RULEBOOK → RulebookScreen(...) } }`.
  - Top bar: `title = subTool?.title ?: "Rules"`; `onBack = { if (subTool != null) vm.closeSubTool() else onBack() }`; `pillText = state.specificitySummary` → `vm.openSpecificityDialog()`; action: `subTool == null` → layout icon (`Icons.Filled.GridView`, "Change layout") → `setLayout`/`showLayoutSheet`; `subTool` in SETUP/TURN/WIN/KEYWORDS → `Icons.AutoMirrored.Filled.MenuBook` ("Open rulebook pages") → `vm.jumpToPages()`; RULEBOOK → none.
  - `state.showSpecificityDialog && state.draftSelection != null` → `SpecificityDialog(manifest, draft, vm::selectSpecificityOption, vm::setSpecificityNumber, onConfirm = vm::confirmSpecificities)`.
  - `state.showLayoutSheet` → `LayoutBottomSheet(currentLayout = state.layout, onLayoutSelected = vm::setLayout, onDismiss = vm::dismissLayoutSheet)`.
- `OptionsSheet(onDismiss)`: `ModalBottomSheet` containing `SpecificityEditor(manifest, draft ?: selection, …)` + "Save" `Button` → `vm.confirmSpecificities(); onDismiss()`; `LaunchedEffect(Unit) { vm.openSpecificityEditor() }` to initialize the draft.

- [ ] **Step 1: Write the failing test**

```kotlin
@RunWith(RobolectricTestRunner::class)
class RulesMainPanelTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun `tiles render in fixed sub tool order`()
        // setContent { RulesMainPanel(RuleSubTool.entries, LayoutMode.LIST) {} }
        // assert texts "Setup","Turn Structure","Win Condition","Keywords","Rulebook" appear in that order
        // (use onAllNodesWithText + layout ordering assertions, or assertIsDisplayed per node)

    @Test fun `tile click reports the sub tool`()
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `TEST`
Expected: FAIL.

- [ ] **Step 3: Implement** per the Interfaces block. Also delete the six legacy UI files. (See Task 6 Step 4 note: if Tasks 6 and 14 are done in one working session, run `TEST` once after this task and commit Tasks 6-14 work incrementally.)

- [ ] **Step 4: Run test to verify it passes**

Run: `TEST` then `BUILD`
Expected: PASS; BUILD succeeds (legacy `RulesViewModel`, `RulesProvider`, `InMemoryRulesProvider`, and the legacy `RulesConfig` models still compile — they are removed in Task 23).

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/turnzero/tools/rules/RulesTool.kt app/src/main/java/com/turnzero/tools/rules/RulesMainPanel.kt app/src/test/java/com/turnzero/tools/rules/RulesMainPanelTest.kt
git rm app/src/main/java/com/turnzero/tools/rules/RulesScreen.kt app/src/main/java/com/turnzero/tools/rules/RulesSetupTab.kt app/src/main/java/com/turnzero/tools/rules/RulesTurnTab.kt app/src/main/java/com/turnzero/tools/rules/RulesVictoryTab.kt app/src/main/java/com/turnzero/tools/rules/RulesKeywordsTab.kt app/src/main/java/com/turnzero/tools/rules/ContentBlockRenderer.kt app/src/main/java/com/turnzero/tools/rules/RulesPdfSheet.kt
git commit -m "feat: rewrite rules tool as sub-tool panel with own top bar"
```

---

### Task 15: Content validation test

**Files:**
- Create: `app/src/test/java/com/turnzero/tools/rules/RulesContentValidationTest.kt`

**Interfaces:**
- Consumes: `AssetRulesRepository` (Task 5), `isSatisfiable` (Task 4), `RulesManifest` (Task 2).

- [ ] **Step 1: Write the failing test**

```kotlin
class RulesContentValidationTest {

    private class FileRulesAssetSource(private val root: File) : RulesAssetSource {
        override fun read(path: String): String? =
            root.resolve(path).takeIf { it.isFile }?.readText()
    }

    private val repository = AssetRulesRepository(FileRulesAssetSource(File("src/main/assets")))
    // Note: unit tests run with the :app module dir as CWD, so src/main/assets resolves.

    @Test fun `every game folder parses and is well-formed`() = runTest {
        val gameFolders = File("src/main/assets/rules").listFiles { f -> f.isDirectory }!!.sortedBy { it.name }
        assertTrue("expected at least one game folder", gameFolders.isNotEmpty())
        gameFolders.forEach { folder ->
            val rules = repository.getRules(folder.name)
            assertNotNull("game ${folder.name} must parse", rules)
            checkManifestWellFormed(folder.name, rules!!.manifest)
            checkWhenReferences(folder.name, rules)
            checkPageRefs(folder.name, rules)
        }
    }

    @Test fun `every when condition is satisfiable`() = runTest {
        File("src/main/assets/rules").listFiles { f -> f.isDirectory }!!.forEach { folder ->
            val rules = repository.getRules(folder.name)!!
            allConditions(rules).forEach { cond ->
                assertTrue("unsatisfiable when in ${folder.name}: $cond", isSatisfiable(cond, rules.manifest))
            }
        }
    }
    // helpers: checkManifestWellFormed (unique spec/option ids, requires target existing options, no requires cycles, number min<=max)
    //          checkWhenReferences (every Predicate specId declared; option values exist in that spec; number values parse as int in min..max)
    //          checkPageRefs (PageRef.rulebook exists; pages non-empty)
    //          allConditions(rules) = steps/phases/step items/keywords/winConditions/rulebooks/pages `when`s
}
```

`getRules` is `suspend` — wrap with `runTest` (or `runBlocking` in a plain JUnit test).

- [ ] **Step 2: Run test to verify it fails**

Run: `TEST`
Expected: FAIL — no `assets/rules` folders exist yet.

- [ ] **Step 3: Implement** — nothing to implement beyond the test itself.

- [ ] **Step 4: Run test to verify it passes (trivially)**

Run: `TEST`
Expected: FAIL with "expected at least one game folder" is NOT acceptable — this test is the gate for Task 16+. Expected state at the end of THIS task: test compiles; it will fail until the first game folder lands (Task 16). Commit the failing gate test alone.

- [ ] **Step 5: Commit**

```bash
git add app/src/test/java/com/turnzero/tools/rules/RulesContentValidationTest.kt
git commit -m "test: add rules content validation gate (fails until first migration)"
```

---

### Tasks 16-22: Content migration (one task per game)

For each game: read the legacy Kotlin rules file, convert it to the five JSON files, run the validation test + `TEST`, commit. Conversion mapping (from the spec, §Content migration):

- `RuleStep(title, summary, contents)` → `RuleItem`: `title`; `summary` appended as first `text` paragraph; `contents` flattened: `Paragraph` → `text` entry, `BulletList` → `bullets` entries, `Image("asset:<path>")` → `image` with the `asset:` prefix stripped. `SubHeading` inside Setup/Win/Keyword items → a `text` entry wrapped in `**…**`; inside Turn content a `SubHeading` starts a NEW step (see below).
- Turn content: each legacy `RuleStep` in `playSequence` becomes a `Phase(name = title)`; `contents` before the first `SubHeading` become the phase's first step (title = phase name); each `SubHeading` starts a new `RuleItem` step whose `title` is the sub-heading text (bold markers stripped from the title) followed by its paragraphs/bullets.
- `InfoCard` (victoryRules) → `win.json` `winConditions` entries; `deckRules` → Setup steps titled "Deck — <card title>"; `fieldContent` → Setup steps (diagram `Image` → first step's `image`; zone `BulletList` → its own step; paragraphs → text).
- `PdfAsset(name, path)` → manifest `rulebooks` entries `{ id, name, asset: path, when }`; the id is a stable slug of the name.
- Legacy `Keyword(name, contents, category)` → `keywords.json` entry (`name`, `category`, flattened `text`/`bullets`; `SubHeading` → bold `text` entry).
- Multi-`RulesConfig` games (Yu-Gi-Oh!, MtG, FaB) declare a `format` `single` specificity whose options are the legacy `(rulesetId, rulesetName)` pairs **in config order**; items shared by all formats carry no `when`; format-specific items carry `when: { "format": [ids…] }` (group ids that share the identical item). Single-config games (Pokémon, SWU, One Piece, Digimon) declare `"specificities": []`.
- Legacy games registered as `"yugioh"`, `"mtg"`, `"fab"`, `"swu"`, `"onepiece"`, `"digimon"`, `"pokemon"` (see `TZApplication.kt`).
- Rulebook `when`: empty `{}` when always matching; per-format PDFs get `"when": { "format": ["<id>", …] }`.
- Add `"pages"` refs only where the legacy PDF is known to contain the section; otherwise omit `pages` entirely (the jump button then falls back — Review Focus 5). Keep JSON minimal; never fabricate page numbers.

### Task 16: Pokémon migration (reference game)

**Files:**
- Create: `app/src/main/assets/rules/pokemon/manifest.json`, `setup.json`, `turn.json`, `win.json`, `keywords.json`
- Source: `app/src/main/java/com/turnzero/games/pokemon/rules/PokemonRules.kt`

- [ ] **Step 1: Convert content** per the mapping. Manifest: `specificities: []`; `rulebooks: [{ "id": "official", "name": "Official Rulebook", "asset": "rulebooks/pokemon-rulebook.pdf", "when": {} }]`. Fold `deckRules` (Main Deck, Special Card Limits) and `fieldContent` (diagram image + zones + damage counters) into Setup steps. Turn: 4 phases (Draw / Action / Attack / Checkup); the Action phase's A-F sub-headings become steps.
- [ ] **Step 2: Run validation + tests**

Run: `TEST`
Expected: PASS (the Task 15 gate now has one game and must be green).

- [ ] **Step 3: Commit**

```bash
git add app/src/main/assets/rules/pokemon
git commit -m "feat: migrate pokemon rules content to json"
```

### Task 17: Yu-Gi-Oh! migration

**Files:** Create `app/src/main/assets/rules/yugioh/{manifest,setup,turn,win,keywords}.json`
**Source:** `app/src/main/java/com/turnzero/games/yugioh/rules/YuGiOhRules.kt`

- [ ] **Step 1: Convert content.** `format` options in config order: `v5-dm` "v5.0 - DM", `v6-gx` "v6.0 - GX", `v7-5ds` "v7.0 - 5D's", `v8-zexal` "v8.0 - ZEXAL", `v9-arcv` "v9.0 - ARC-V", `v10-vrains` "v10 - VRAINS". Rulebooks: `{ "id": "v6", "name": "Official Rulebook", "asset": "rulebooks/yugioh-v6-rulebook.pdf", "when": { "format": ["v5-dm", "v6-gx"] } }` and one per remaining format (`v7` → yugioh-v7-rulebook.pdf, `v8` → …, `v9` → …, `v10` → …) each `when` = its format. Shared content (`victory`, `v56Setup`/`v56Turn`/`v56Keywords`, `v56DeckRules`) carries no `when` or the shared-group `when` where the configs diverge (`v7*`/`v8*`/`v9*`/`v10*` lists).
- [ ] **Step 2: Run validation + tests**

Run: `TEST`
Expected: PASS.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/assets/rules/yugioh
git commit -m "feat: migrate yugioh rules content to json"
```

### Task 18: MtG migration

**Files:** Create `app/src/main/assets/rules/mtg/{manifest,setup,turn,win,keywords}.json`
**Source:** `app/src/main/java/com/turnzero/games/mtg/rules/MagicRules.kt`

- [ ] **Step 1: Convert content.** `format` options in config order: `standard` "Standard", `modern` "Modern", `pioneer` "Pioneer", `legacy` "Legacy", `vintage` "Vintage", `premodern` "Premodern", `pauper` "Pauper", `commander` "Commander", `duel-commander` "Duel Commander". One rulebook: `{ "id": "comp", "name": "Comprehensive Rules", "asset": "rulebooks/mtg-comprehensive-rulebook.pdf", "when": {} }`. Shared: `victory`, `baseTurn`, `keywords`, `standardField`/`commanderField`/`duelCommanderField` (per-group `when`), base/commander/duel setups; per-format deck rules → Setup steps with `when` grouping the seven "std" formats.
- [ ] **Step 2: Run validation + tests**

Run: `TEST`
Expected: PASS.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/assets/rules/mtg
git commit -m "feat: migrate mtg rules content to json"
```

### Task 19: Flesh and Blood migration

**Files:** Create `app/src/main/assets/rules/fab/{manifest,setup,turn,win,keywords}.json`
**Source:** `app/src/main/java/com/turnzero/games/fab/rules/FaBRules.kt`

- [ ] **Step 1: Convert content.** `format` options: `classic-constructed` "Classic Constructed", `silver-age` "Silver Age". One rulebook `{ "id": "comp", "name": "Comprehensive Rules", "asset": "rulebooks/fab-comprehensive-rulebook.pdf", "when": {} }`. Shared content carries no `when`; the per-format "Match Format" setup step gets `when: { "format": ["<id>"] }` per variant.
- [ ] **Step 2: Run validation + tests**

Run: `TEST`
Expected: PASS.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/assets/rules/fab
git commit -m "feat: migrate flesh and blood rules content to json"
```

### Task 20: Star Wars Unlimited migration

**Files:** Create `app/src/main/assets/rules/swu/{manifest,setup,turn,win,keywords}.json`
**Source:** `app/src/main/java/com/turnzero/games/swu/rules/SwuRules.kt`

- [ ] **Step 1: Convert content.** `specificities: []`. Rulebooks: `{ "id": "quickstart", "name": "Quickstart Rules", "asset": "rulebooks/swu-quickstart-rulebook.pdf", "when": {} }` and `{ "id": "comp", "name": "Comprehensive Rules 8.0", "asset": "rulebooks/swu-comprehensive-rulebook.pdf", "when": {} }` (both always → Rulebook sub-tool shows the multi-PDF selector).
- [ ] **Step 2: Run validation + tests**

Run: `TEST`
Expected: PASS.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/assets/rules/swu
git commit -m "feat: migrate star wars unlimited rules content to json"
```

### Task 21: One Piece migration

**Files:** Create `app/src/main/assets/rules/onepiece/{manifest,setup,turn,win,keywords}.json`
**Source:** `app/src/main/java/com/turnzero/games/onepiece/rules/OnePieceRules.kt`

- [ ] **Step 1: Convert content.** `specificities: []`. Rulebooks: `{ "id": "manual", "name": "Official Rule Manual", "asset": "rulebooks/onepiece-rule-manual-rulebook.pdf", "when": {} }` and `{ "id": "comp", "name": "Comprehensive Rules", "asset": "rulebooks/onepiece-comprehensive-rulebook.pdf", "when": {} }`.
- [ ] **Step 2: Run validation + tests**

Run: `TEST`
Expected: PASS.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/assets/rules/onepiece
git commit -m "feat: migrate one piece rules content to json"
```

### Task 22: Digimon migration

**Files:** Create `app/src/main/assets/rules/digimon/{manifest,setup,turn,win,keywords}.json`
**Source:** `app/src/main/java/com/turnzero/games/digimon/rules/DigimonRules.kt`

- [ ] **Step 1: Convert content.** `specificities: []`. Rulebooks: `{ "id": "manual", "name": "Official Rule Manual", "asset": "rulebooks/digimon-rule-manual-rulebook.pdf", "when": {} }` and `{ "id": "comp", "name": "Comprehensive Rules", "asset": "rulebooks/digimon-comprehensive-rulebook.pdf", "when": {} }`.
- [ ] **Step 2: Run validation + tests**

Run: `TEST`
Expected: PASS.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/assets/rules/digimon
git commit -m "feat: migrate digimon rules content to json"
```

---

### Task 23: Legacy cleanup

**Files:**
- Delete: `app/src/main/java/com/turnzero/tools/rules/RulesModels.kt`, `RulesProvider.kt`, `RuleContentHelpers.kt`, `RulesViewModel.kt` (legacy)
- Delete: all `app/src/main/java/com/turnzero/games/*/rules/*Rules.kt` (7 files)
- Delete: `app/src/test/java/com/turnzero/tools/rules/RulesModelsTest.kt`, `app/src/test/java/com/turnzero/tools/rules/RulesViewModelTest.kt` (legacy)
- Modify: `app/src/main/java/com/turnzero/TZApplication.kt` (remove `rulesProvider` injection + `onCreate` registrations + game-rules imports)
- Modify: `app/src/main/java/com/turnzero/di/RulesModule.kt` (remove `bindRulesProvider` + `InMemoryRulesProvider` import)
- Modify: `core/data/src/main/java/com/turnzero/core/data/UserPreferences.kt` (remove the now-unused `RULES_DEFAULT_RULESET` and `RULES_ENABLED_RULESETS` keys)
- Keep: `app/src/test/java/com/turnzero/tools/rules/ContentBlockRendererTest.kt` (it tests `core:ui`'s public `parseBoldText`)
- Modify: `app/src/test/java/com/turnzero/games/onepiece/rules/OnePieceRulesTest.kt` and `app/src/test/java/com/turnzero/games/pokemon/rules/PokemonRulesTest.kt` — delete both (they assert legacy Kotlin content).

- [ ] **Step 1: Delete legacy code and fix references**

Remove everything listed; `grep -r "RulesConfig\|InMemoryRulesProvider\|RulesProvider\|RULES_DEFAULT_RULESET\|RULES_ENABLED_RULESETS" app/src core/data` must return nothing afterward.

- [ ] **Step 2: Run the full suite + build**

Run: `TEST` then `BUILD`
Expected: PASS; BUILD succeeds.

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "refactor: remove legacy rules provider, models, and kotlin content"
```

---

### Task 24: Documentation

**Files:**
- Create: `docs/rules-tool-json-format.md`
- Modify: `docs/superpowers/specs/2026-07-03-rules-tool-design.md`, `docs/superpowers/specs/2026-07-12-content-block-rules-design.md` (add a one-line "Superseded by 2026-11-14-rules-tool-revamp-design.md" banner at the top)

- [ ] **Step 1: Write the JSON authoring guide**

`docs/rules-tool-json-format.md` must cover: folder/file layout; the full `manifest.json` schema (specificity types, `requires` ordering + closure behavior, rulebooks + `when`); the content-item schema (`title`/`text`/`bullets`/`image`, `**bold**`); the complete `when` grammar with semantics and worked examples (implicit AND, explicit `and`/`or`/`not`, `[]` = none selected, unknown ids = hidden, `pages` are 1-based); the `pages` jump-button refs and the fallback; how to add a new game, add a specificity, add/edit content, add an extension with prerequisites; how to run the validation test (`RulesContentValidationTest`) and what its failures mean (dead rules, unknown ids, cycles); common pitfalls (contradictory `when`, forgetting `requires`).

- [ ] **Step 2: Mark old specs superseded** (one-line banners).

- [ ] **Step 3: Commit** (note: `docs/superpowers/` is gitignored — force-add the spec and plan so the decisions are versioned)

```bash
git add docs/rules-tool-json-format.md docs/superpowers/specs/2026-07-03-rules-tool-design.md docs/superpowers/specs/2026-07-12-content-block-rules-design.md
git add -f docs/superpowers/specs/2026-11-14-rules-tool-revamp-design.md docs/superpowers/plans/2026-11-15-rules-tool-revamp.md
git commit -m "docs: document rules tool json format and supersede old rules specs"
```

---

### Task 25: Version bump + CHANGELOG

**Files:**
- Modify: `app/build.gradle.kts` — `versionCode = 43`, `versionName = "0.21.0"`
- Modify: `CHANGELOG.md`

- [ ] **Step 1: Bump the version** (medium bump per AGENTS.md — substantial new capability).

- [ ] **Step 2: Write the CHANGELOG entry**

Move any `[Unreleased]` content into a new section (currently empty, so only add the new one):

```markdown
## [0.21.0] - 2026-11-15

### Added
- Rules tool revamp: sub-tool panel (Setup, Turn Structure, Win Condition, Keywords, Rulebook) with grid/list layouts
- Per-game specificities: Format for card games, Player Count and ordered Extensions for board games, selectable on first open and from Options, always visible in the top bar
- JSON-driven rules content with parameter-aware `when` conditions and per-tool rulebook page jump buttons

### Changed
- Rules content migrated from hardcoded Kotlin to per-game JSON assets
- Deck and Field sections folded into Setup

### Removed
- Legacy Rules tabs (Setup/Turn/Victory/Deck/Field/Keywords) and format visibility controls
```

Keep `## [Unreleased]` empty.

- [ ] **Step 3: Run full verification**

Run: `TEST` then `BUILD`
Expected: both green.

- [ ] **Step 4: Commit**

```bash
git add app/build.gradle.kts CHANGELOG.md
git commit -m "chore: bump version to 0.21.0"
```

---

## Self-review notes

- Spec coverage: sub-tool panel + fixed order (Task 14); specificities model + first-open dialog + pill + Options (Tasks 6, 13, 14); `when` grammar + satisfiability + `requires` (Tasks 2-4, 15); Setup carousel (8); Turn drill-down + Prev/Next (9); Win (10); Keywords (11); Rulebook + selector + open-at-page (12, 6); jump-to-pages per sub-tool (6, 14); sub-tool persistence per game (6); bottom nav preservation (Task 1 opt-out keeps the ToolDetail scaffold untouched); JSON migration of 7 games (16-22); docs + old spec superseding (24); CHANGELOG + version (25).
- Type consistency: `Selection` maps and `matches` signatures are identical across Tasks 3-6 and the validation test; `RuleSubTool` ids are the DataStore values; `pdfInitialPage` is 0-based everywhere after the VM converts 1-based JSON `pages`.
- Review Focus lines are pinned to the tasks listed in the table above.
- The plan intentionally leaves these to the implementer: exact JSON formatting (2-space indent, key order follows the spec examples), animation polish, and the phase-card visuals beyond the stated layout.
