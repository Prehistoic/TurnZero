# Rules Tool Revamp — Design

Date: 2026-11-14
Status: Approved for planning (implementation in a later session)

## Intent & Why

The Rules tool is being revamped end-to-end: UI and content. The goals are (1) improve
legibility, and (2) prepare for the addition of **board games** (until now the app only
shipped trading-card games). Board games introduce a different shape of rules content —
player counts, expansions (with dependency ordering), "player turn vs. automata turn" — so
the tool must stop being a card-game-specific tab list and become a generic, parameter-aware
rules browser.

This document supersedes `2026-07-03-rules-tool-design.md` and
`2026-07-12-content-block-rules-design.md`.

## Goals

- The Rules tool's main screen mirrors the Tools screen: a panel of **sub-tools** in a fixed
  order, with Grid/List display layouts.
- Five fixed sub-tools, in this order: **Setup, Turn Structure, Win Condition, Keywords,
  Rulebook**.
- A per-game, extensible **specificities** system (parameters that change the rules):
  - Card games: **Format** (single-select).
  - Board games: **Player Count** (number) + **Extensions** (multi-select checklist, 0..all,
    with dependency ordering).
  - Selectable (a) once via a popup on first open, (b) later from Options; always visible as a
    compact pill in the top bar across the main screen and every sub-tool.
- All content moves from hardcoded Kotlin to **minimal, parameter-aware JSON** assets.
- The bottom nav (Tool switcher + Options) is preserved on every sub-tool; the currently-open
  sub-tool is remembered per game when switching tools.

## Non-goals

- No runtime-editable content. JSON is bundled at build time and edited in the repo.
- No new board game is added in this change — only the capability to support one.
- No network/runtime content loading.

## Anti-goals

- A combinatorial explosion of JSON files (one per specificity combination).
- A complex nested condition DSL in the JSON (a small, uniform `and`/`or`/`not` is the limit).
- Re-introducing the removed "Visible Formats" / "Default Format" controls.

## Current State (baseline)

- `RulesTool` is a universal (`*`) `Tool` whose screen is a `ScrollableTabRow` +
  `HorizontalPager` with tabs: Setup, Turn, Victory, Deck (optional), Field (optional),
  Keywords (optional).
- Content is hardcoded Kotlin per game (`games/<name>/rules/*Rules.kt`), registered at
  startup in `TZApplication.onCreate()` via `InMemoryRulesProvider.register(gameId, configs)`.
- "Format" is modeled today as multiple `RulesConfig` entries per game (Yu-Gi-Oh has 6, MtG 3,
  Flesh & Blood 2), selected via a top-bar dropdown and an Options sheet (Visible Formats +
  Default Format).
- Content model: `RulesConfig`, `RuleStep(title, summary, contents)`,
  `InfoCard(title, contents)`, `Keyword(name, contents, category)`, `PdfAsset`,
  `ContentBlock` (Paragraph / SubHeading / BulletList / Image).
- `RulesPdfSheet` is a full-screen PDF viewer with zoom/pan/swipe/page navigation (opened as a
  takeover, not a tab).
- `GameCategory` already defines `TCG` and `BOARD_GAME` (the latter is unused).
- `kotlinx-serialization-json` (1.7.3) and the serialization Gradle plugin are already
  declared and applied in `:app` — no new dependency is needed.

## Decisions (made with the product owner)

### D1 — Specificities are generic typed selectors

A per-game specificity is one of three types, declared in the game's manifest (not hardcoded):

| Type | Example | UI |
|---|---|---|
| `single` | Format | radio list |
| `number` | Player Count (`min`..`max`) | segmented control |
| `multi` | Extensions (0..all, ordered) | checklist |

Adding a future specificity type = new enum value + a renderer + a `when`-matching rule. No
other types are foreseen for now.

### D2 — Specificities UI

- **First open** (no saved selection for a game): a **centered dialog** presents the game's
  specificities; "Start" saves them and opens the main Rules screen.
  - `single` is pre-selected to its **first option**; `number` is pre-selected to **`min`**;
    `multi` starts empty. "Start" is therefore always enabled.
- **Top bar**: a compact, tappable summary **pill** next to the title
  (`2P · +2 ext` for a board game, `Standard` for a card game). Tapping it reopens the
  specificity editor. It is present on the main screen and every sub-tool.
- **Options** (bottom nav): reopens the same specificity editor.
- **Games with zero specificities** (e.g. a single-format TCG after migration): no first-open
  dialog and no pill.

### D3 — Parameter-aware JSON uses a small boolean `when` expression

One JSON file per sub-tool; each content item optionally carries a `when` predicate. Chosen
over (B) per-variant files (combinatorial for multi-select) and (C) additive sections (a second
mechanism). The expression is **uniform**, defaults to **AND**, and supports `or`/`not` at any
level (including across specificities). Full grammar in [JSON schema](#json-schema).

### D4 — Sub-tool order (final)

`Setup → Turn Structure → Win Condition → Keywords → Rulebook`. Not alphabetical, always this
order.

### D5 — Deck and Field fold into Setup

The current "Deck" tab (deck construction rules) and "Field" tab (play-area layout + diagram)
become Setup steps. The play-area diagram becomes a Setup step's optional `image`; the field
zones become one or more Setup steps.

### D6 — Turn Structure layout: drill-down with step navigation

Chosen design C (drill-down) over A (phase chips + accordion) and B (nested accordion):

1. **Phase overview** — a list of phase cards (at-a-glance).
2. Tap a phase → **step list** (flat, tap any step; no auto-highlight).
3. Tap a step → **step detail** with **Prev/Next** buttons, a `Step X of Y · Phase` readout, and
   `Next: <next phase> →` at phase boundaries so a player can walk the whole turn without
   backing out.

Buttons only (no swipe between steps).

### D7 — Kept from the current tool

- The **Keywords** screen behavior (search + category chips + expandable cards), adapted to the
  new layout.
- The **PDF viewer** (`RulesPdfSheet`), adapted as a sub-tool and extended to open at a specific
  page.
- The **multi-PDF selector dialog**, shown only when several rulebooks match the current
  specificities.

Removed: "Visible Formats" and "Default Format" (superseded by the specificity selection).

### D8 — Extension dependencies (ordering)

Extensions can require other extensions (`requires`, a list of option ids, all mandatory).
This is expressed generically on any `multi` option. Consequences:

- **Selection logic** (pure functions): selecting an extension also selects its transitive
  `requires`; deselecting one also deselects everything transitively depending on it.
- **Dead-rule validation**: `isSatisfiable(when, manifest)` enumerates every dependency-closed
  selection and flags any rule whose `when` can never be satisfied (e.g. "ext B selected but
  its prerequisite A not"). Runs as a content-validation test.

### D9 — Content flattening (consequence of "lists of strings")

Rich, interleaved `ContentBlock` sequences are flattened. Sub-headings become step (or phase)
boundaries; an item renders `title` then `text` then `bullets` then `image` (fixed order, no
interleaving). Inline `**bold**` inside `text`/`bullets` strings is preserved (rendering already
supports it). Interleaving that truly matters is expressed by splitting into multiple
steps/phases.

## Architecture

### Data models (Kotlin, `@Serializable`)

```kotlin
@Serializable
data class RulesManifest(
    val specificities: List<SpecificityDef> = emptyList(),
    val rulebooks: List<RulebookDef> = emptyList(),
)

@Serializable
data class SpecificityDef(
    val id: String,
    val name: String,
    val type: SpecificityType,                       // "single" | "number" | "multi"
    val options: List<OptionDef> = emptyList(),      // single / multi
    val min: Int? = null,                            // number
    val max: Int? = null,                            // number
)

@Serializable
data class OptionDef(
    val id: String,
    val name: String,
    val requires: List<String> = emptyList(),        // multi only: other option ids that must be selected
)

@Serializable
data class RulebookDef(
    val id: String,
    val name: String,
    val asset: String,
    val `when`: Condition = Condition.And(emptyList()),   // empty = always matches
)
```

The `when` expression is modeled as a small sealed type (a custom
`JsonContentPolymorphicSerializer` maps the compact JSON shapes below onto it):

```kotlin
@Serializable
sealed interface Condition {
    data class Predicate(val specId: String, val value: SpecValue) : Condition
    data class And(val conditions: List<Condition>) : Condition   // explicit; map shorthand is implicit AND
    data class Or(val conditions: List<Condition>) : Condition
    data class Not(val condition: Condition) : Condition
}

@Serializable
sealed interface SpecValue {
    data class Eq(val value: String) : SpecValue                  // "x"
    data class AllOf(val values: List<String>) : SpecValue        // ["x","y"] — AND
    data class AnyOf(val values: List<String>) : SpecValue        // { "or": ["x","y"] }
    data class NoneOf(val values: List<String>) : SpecValue       // { "not": ["x","y"] }
    data class NoneSelected : SpecValue                           // [] — multi only
}
```

Content item and per-sub-tool files:

```kotlin
@Serializable
data class RuleItem(
    val title: String? = null,
    val text: List<String> = emptyList(),
    val bullets: List<String> = emptyList(),
    val image: String? = null,
    val `when`: Condition = Condition.And(emptyList()),
)

@Serializable data class PageRef(val rulebook: String, val pages: List<Int> = emptyList(), val `when`: Condition = Condition.And(emptyList()))

@Serializable data class SetupContent(val pages: List<PageRef> = emptyList(), val steps: List<RuleItem> = emptyList())
@Serializable data class TurnContent(val pages: List<PageRef> = emptyList(), val phases: List<Phase> = emptyList())
@Serializable data class Phase(val name: String, val steps: List<RuleItem> = emptyList(), val `when`: Condition = Condition.And(emptyList()))
@Serializable data class WinContent(val pages: List<PageRef> = emptyList(), val winConditions: List<RuleItem> = emptyList())
@Serializable data class KeywordsContent(val pages: List<PageRef> = emptyList(), val keywords: List<KeywordItem> = emptyList())
@Serializable data class KeywordItem(val name: String, val category: String? = null, val text: List<String> = emptyList(), val bullets: List<String> = emptyList(), val `when`: Condition = Condition.And(emptyList()))
```

`SpecificityType` is serialized with `@SerialName("single")` / `"number"` / `"multi"`.

### JSON file layout

```
app/src/main/assets/rules/<gameId>/
  manifest.json     specificities + rulebooks
  setup.json        Setup carousel (+ top-level "pages")
  turn.json         Turn Structure (+ top-level "pages")
  win.json          Win Condition (+ top-level "pages")
  keywords.json     Keywords (+ top-level "pages")
```

The game is discovered by its `gameId`; no code registration is needed. If
`manifest.json` is absent, the Rules tool shows an empty state.

### JSON schema

**`when` grammar** (uniform, on any item, `Phase`, or `RulebookDef`/`PageRef`):

```
when = condition

condition =
    { spec: value, ... }            // ≥1 specIds → implicit AND of those predicates
  | { "and": [condition, ...] }     // explicit AND
  | { "or":  [condition, ...] }     // explicit OR (any level, incl. across specificities)
  | { "not": condition }            // negation (of a predicate, or, and, ...)

value (under a specId) =
    "x"                             // single/number: == x · multi: x selected
  | ["x", "y", ...]                 // implicit AND (multi: all selected)
  | { "or": ["x", "y"] }            // explicit OR of values
  | { "not": "x" | ["x", "y"] }     // not x / none of x,y
  | []                              // multi only: "none selected" (base game)
```

Semantics: `Predicate` evaluates against the current selection; `single`/`number` use equality,
`multi` uses "is selected". `AllOf` = every value matches; `AnyOf` = at least one; `NoneOf` =
none; `NoneSelected` = the `multi` set is empty. An unknown `specId` or option id in a predicate
evaluates to **not matching** (fail-safe: hide rather than show wrong content).

**manifest.json**

```json
{
  "specificities": [
    { "id": "format", "name": "Format", "type": "single",
      "options": [ { "id": "standard", "name": "Standard" },
                   { "id": "commander", "name": "Commander" } ] },
    { "id": "players", "name": "Players", "type": "number", "min": 2, "max": 5 },
    { "id": "extensions", "name": "Extensions", "type": "multi",
      "options": [ { "id": "base",   "name": "Base Game" },
                   { "id": "exp-a",  "name": "Expansion A" },
                   { "id": "exp-b",  "name": "Expansion B", "requires": ["exp-a"] },
                   { "id": "exp-c",  "name": "Expansion C", "requires": ["exp-b"] } ] }
  ],
  "rulebooks": [
    { "id": "official", "name": "Official Rulebook",
      "asset": "rulebooks/game-rulebook.pdf", "when": {} },
    { "id": "comp", "name": "Comprehensive Rules",
      "asset": "rulebooks/game-comp.pdf", "when": { "format": "commander" } }
  ]
}
```

**setup.json**

```json
{
  "pages": [ { "rulebook": "official", "pages": [4, 5, 6], "when": { "format": "standard" } } ],
  "steps": [
    { "title": "Shuffle & draw",
      "text": ["Shuffle your deck.", "Draw 7 cards."],
      "image": "rulebooks/images/setup-1.png" },
    { "text": ["With 4 players, deal 5 cards instead of 7."],
      "when": { "players": "4" } },
    { "title": "Add expansion module",
      "when": { "extensions": "exp-a" } }
  ]
}
```

**turn.json**

```json
{
  "pages": [ { "rulebook": "official", "pages": [7, 8], "when": {} } ],
  "phases": [
    { "name": "Action Phase",
      "steps": [
        { "title": "Attach Energy",
          "text": ["Attach 1 Energy card from your hand to one of your Pokémon."] },
        { "title": "Play Trainer",
          "bullets": ["Play Items freely.", "One Supporter per turn."] }
      ] },
    { "name": "Monster Turn",
      "when": { "extensions": "exp-b" },
      "steps": [ { "title": "Activate monsters", "text": ["..."] } ] }
  ]
}
```

**win.json**

```json
{
  "pages": [ { "rulebook": "official", "pages": [12], "when": {} } ],
  "winConditions": [
    { "title": "Take all prize cards", "text": ["The first player to take all 6 wins."] }
  ]
}
```

**keywords.json**

```json
{
  "pages": [ { "rulebook": "official", "pages": [30], "when": {} } ],
  "keywords": [
    { "name": "<Asleep>", "category": "Special Conditions",
      "text": ["Turn the Pokémon counterclockwise. It cannot attack or retreat."] }
  ]
}
```

### Loading pipeline

1. `RulesRepository` (injected singleton) loads and caches `manifest.json` + the four content
   files for a `gameId` from assets, parsed with `kotlinx.serialization`.
2. `RulesViewModel` reads the persisted specificity selection and the first-open flag from
   DataStore.
3. If not yet set (first open), the UI shows the centered dialog (with D2 defaults); "Start"
   persists the selection and flag.
4. A pure `matches(condition, selection)` function filters content per sub-tool against the
   current selection. This function is the unit-test core.

### Selection & dependency logic (pure functions)

```kotlin
// A concrete selection of specificity values.
data class Selection(
    val single: Map<String, String> = emptyMap(),      // specId -> optionId
    val number: Map<String, Int> = emptyMap(),         // specId -> value
    val multi: Map<String, Set<String>> = emptyMap(),  // specId -> set of optionIds
)

fun matches(condition: Condition, selection: Selection): Boolean

fun select(specId: String, optionId: String, manifest: RulesManifest, selection: Selection): Selection
    // adds optionId + transitive closure of `requires`

fun deselect(specId: String, optionId: String, manifest: RulesManifest, selection: Selection): Selection
    // removes optionId + everything that transitively `requires` it

fun validSelections(manifest: RulesManifest): Sequence<Selection>
    // all dependency-closed combinations of specificity values

fun isSatisfiable(condition: Condition, manifest: RulesManifest): Boolean
    // exists s in validSelections(manifest) with matches(condition, s)
```

`validSelections` enumerates the cross-product of `single` options × `number` range ×
dependency-closed `multi` subsets. Extension counts are small, so the exhaustive enumeration is
cheap. `isSatisfiable` is used by the content-validation test (below) to flag dead rules.

### State

`RulesUiState` holds: `gameId`, `manifest`, `selection`, `subTool` (currently open sub-tool or
`null` = main panel), `filtered` content for the current sub-tool, keyword search/filter state,
PDF viewer state. Selection changes re-filter all sub-tools.

### Persistence (DataStore)

- `rules_spec_<gameId>_<specId>` — single (`string`), number (`int`), multi (`string set`).
- `rules_spec_set_<gameId>` — boolean; set once the first-open dialog is completed.
- `rules_subtool_<gameId>` — string; last open sub-tool id, or absent/empty = main panel.

Removed keys: `RULES_DEFAULT_RULESET`, `RULES_ENABLED_RULESETS` (old format controls).

### Navigation & top bar

Sub-tools are **internal navigation state** within the Rules tool, not separate nav routes, so
the existing `ToolDetail` route (and its bottom nav / tool switcher) is unchanged. This requires
the Rules tool to drive its own top bar:

- **Main panel**: title "Rules", back → Tools screen, specificity pill (if any specificities),
  layout toggle.
- **Sub-tool**: title = sub-tool name, back → main panel, specificity pill, and (for all but
  Rulebook) a **jump-to-pages** button. Tapping it resolves the first `PageRef` whose `when`
  matches the current selection and opens that rulebook at `pages[0]` (the list documents the
  full relevant range; only `pages[0]` is jumped to).

Recommendation: add a lightweight opt-out to the `Tool` contract (e.g.
`Tool.providesOwnTopBar(): Boolean = false`); `TZNavHost` renders the default `TZTopBar` only
when the tool does not opt in. The builder may choose an equivalent mechanism, but the Rules
tool must own title + back + pill + page action.

When switching to another tool via the Tool switcher, the ViewModel is destroyed; the
currently-open sub-tool is persisted to `rules_subtool_<gameId>` so reopening Rules for that
game returns to the same sub-tool.

### UI spec

- **Main Rules screen**: `TZTileLayout` (Grid/List via `LayoutMode`, reuse the layout button +
  `LayoutBottomSheet`) rendering the five sub-tool tiles in the fixed order. Each tile: icon +
  name. Empty sub-tools still show, with a friendly empty state when opened.
- **Setup**: a horizontal carousel of step cards; each card shows the auto-generated number with
  a dot (`1.`), the title/text below, and an optional image. Prev/Next to page through steps.
- **Turn Structure**: drill-down per D6.
- **Win Condition**: a scrollable list of bordered cards (title + text + bullets), as today.
- **Keywords**: current screen (search field, category chips, expandable cards), categories
  derived from each keyword's `category`.
- **Rulebook**: resolve matching rulebooks from the manifest; one → open directly, several →
  multi-PDF selector dialog, zero → empty state. Reuses `RulesPdfSheet`, extended to accept a
  starting page.
- **Top bar pill**: compact summary of the current selection, tappable to edit.
- **Specificity dialog**: `single` = radio list, `number` = segmented control, `multi` =
  checklist with the auto-select/deselect behavior from [Selection logic](#selection--dependency-logic-pure-functions).

### Content migration (7 existing games)

Each `games/<name>/rules/*Rules.kt` is converted to a `assets/rules/<gameId>/` folder:

- `RuleStep(title, summary, contents)` → `RuleItem` (summary becomes part of `text`; `contents`
  map to `title`/`text`/`bullets`/`image`; sub-headings become separate steps/phases per D9).
- `InfoCard` (Victory + Deck) → `win.json` entries and Setup steps respectively.
- `Keyword` → `keywords.json` entries (`category` preserved).
- `fieldContent` (diagram + zones) → Setup step(s); diagram becomes `image`.
- `PdfAsset` → manifest `rulebooks` entries with appropriate `when` (per-format PDFs).
- Multiple `RulesConfig` per game (Yu-Gi-Oh, MtG, FaB) → a `format` `single` specificity whose
  options are the former rulesets.
- Single-`RulesConfig` games (Pokémon, One Piece, Digimon, SWU) → **zero specificities** (no
  dialog, no pill) unless a genuine parameter emerges.

## Testing

- `matches`: every `Condition`/`SpecValue` variant (Eq/AllOf/AnyOf/NoneOf/NoneSelected,
  And/Or/Not), unknown ids, across `single`/`number`/`multi`.
- `select`/`deselect`: transitive `requires` closure, dependent cascade on deselect, cycles.
- `validSelections` / `isSatisfiable`: correct enumeration under dependencies; flags dead rules.
- JSON parsing: round-trips, absent optional fields default correctly, custom `Condition`
  serializer accepts all grammar shapes.
- Content well-formedness (per game): manifest references valid; `when` keys/options are
  declared; `PageRef.rulebook` ids exist; every rule is satisfiable (`isSatisfiable`).
- `RulesViewModel`: first-open flag + defaults, selection persistence, sub-tool restore,
  filtering on selection change.
- Compose UI: Setup carousel navigation, Turn Structure drill-down + prev/next across phase
  boundaries, Keywords search/filter, extension auto-select/deselect in the dialog.

## Docs to write/update

- Add `docs/rules-tool-json-format.md` — comprehensive JSON authoring guide (file layout, full
  schema, `when` grammar and semantics, extension `requires`, how to add a game / a specificity
  / content, worked examples, and how to run the dead-rule validation).
- Replace the Rules-tool section of the old specs (mark `2026-07-03-rules-tool-design.md` and
  `2026-07-12-content-block-rules-design.md` as superseded, pointing to this doc and the JSON
  guide).

## Left to the builder

- Exact `Tool` top-bar opt-out API shape (must satisfy the "Rules tool owns its top bar"
  requirement).
- Choice of `RulesPdfSheet` API change for "open at page N".
- Sub-tool tile composable (generalize `ToolTile` or add a sibling).
- The custom `Condition`/`SpecValue` polymorphic serializer.
- Migration ordering of the 7 games (any order; each is independent).
