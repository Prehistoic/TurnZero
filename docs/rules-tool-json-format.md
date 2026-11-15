# Rules tool — JSON content format

The Rules tool ships no rules in code. Every game's rules live in a folder of JSON files under
`app/src/main/assets/rules/<gameId>/`, and the app parses them at runtime. This guide is the
authoring reference: read it and you can add a game, add a parameter that changes the rules, or
edit content without touching Kotlin.

The source of truth is the code, in this order:

| Concern | File |
|---|---|
| Models, fields, defaults, `when` grammar | `app/src/main/java/com/turnzero/tools/rules/RulesContentModels.kt` |
| `when` evaluation, `requires` closure, satisfiability | `app/src/main/java/com/turnzero/tools/rules/RulesEngine.kt` |
| Asset loading and failure behaviour | `app/src/main/java/com/turnzero/tools/rules/RulesRepository.kt` |
| Filtering, page jumps, top-bar summary | `app/src/main/java/com/turnzero/tools/rules/RulesToolViewModel.kt`, `RulesToolUiState.kt` |
| Validation gate | `app/src/test/java/com/turnzero/tools/rules/RulesContentValidationTest.kt` |

Contents:

1. [Folder and file layout](#1-folder-and-file-layout)
2. [`manifest.json`](#2-manifestjson)
3. [Content files](#3-content-files)
4. [The `when` grammar](#4-the-when-grammar)
5. [Page references and the jump button](#5-page-references-and-the-jump-button)
6. [Recipes](#6-recipes)
7. [The validation gate](#7-the-validation-gate)
8. [Pitfalls](#8-pitfalls)

---

## 1. Folder and file layout

```
app/src/main/assets/
  rules/
    <gameId>/                  one folder per game, named exactly like Game.id
      manifest.json            specificities + rulebooks (required)
      setup.json               Setup content (required)
      turn.json                Turn Structure content (required)
      win.json                 Win Condition content (required)
      keywords.json            Keywords content (required)
  rulebooks/                   PDFs and diagram images referenced by the JSON
    <game>.pdf
    images/<game>-field.png
```

`<gameId>` is the `Game.id` the game declares in Kotlin — `yugioh`, `mtg`, `swu`, `digimon`,
`fab`, `pokemon`, `onepiece`, `vanguard`. There is no registration step: the folder *is* the
registration. `RulesTool` declares `supportedGames = setOf("*")`, so the Rules tile appears for
every game and shows the "No rules available for this game" empty state when the folder is
missing (today that is the case for `vanguard`).

### How the files are loaded

`AssetRulesRepository` (a singleton) reads the folder once per game per app process:

1. Read `rules/<gameId>/manifest.json`. Missing or unreadable → log a warning, **no rules for
   that game** (empty state).
2. Read `setup.json`, `turn.json`, `win.json`, `keywords.json`. If any of the four is missing →
   log "Incomplete rules folder", **no rules for that game**.
3. Parse all five with `Json { ignoreUnknownKeys = true }`. Any decode failure → **no rules for
   that game**. One bad character in one file hides the whole game's rules, so always run the
   validation gate (section 7).

The four content files are not optional even when a sub-tool has nothing in it: an empty JSON
object `{}` is enough. Parsed results are cached for the life of the process; failures are not
cached and are retried the next time the tool is opened.

`ignoreUnknownKeys = true` means stray keys are ignored (`"summary": "..."` is harmless), so
renaming a field silently drops it. Spell field names exactly as documented here.
JSON is strict: no comments, no trailing commas.

---

## 2. `manifest.json`

```json
{
  "specificities": [
    {
      "id": "format",
      "name": "Format",
      "type": "single",
      "options": [
        { "id": "v5-dm", "name": "v5.0 - DM" },
        { "id": "v6-gx", "name": "v6.0 - GX" }
      ]
    }
  ],
  "rulebooks": [
    {
      "id": "v6",
      "name": "Official Rulebook",
      "asset": "rulebooks/yugioh-v6-rulebook.pdf",
      "when": { "format": { "or": ["v5-dm", "v6-gx"] } }
    },
    {
      "id": "v7",
      "name": "Official Rulebook",
      "asset": "rulebooks/yugioh-v7-rulebook.pdf",
      "when": { "format": "v7-5ds" }
    }
  ]
}
```

(Trimmed from `app/src/main/assets/rules/yugioh/manifest.json`, which has six format options and
five rulebooks.)

| Field | Type | Default | Notes |
|---|---|---|---|
| `specificities` | array of specificity | `[]` | declaration order is UI order |
| `rulebooks` | array of rulebook | `[]` | declaration order is picker order |

### 2.1 Specificities

A specificity is a parameter the player picks that changes which rules text shows. The app
supports exactly three types.

| Field | Type | Default | Applies to |
|---|---|---|---|
| `id` | string, required | — | referenced by every `when` |
| `name` | string, required | — | section label in the editor |
| `type` | `"single"` \| `"number"` \| `"multi"`, required | — | see below |
| `options` | array of option | `[]` | `single`, `multi` |
| `min` | integer | `null` | `number` only, **required** there |
| `max` | integer | `null` | `number` only, **required** there |

| `type` | UI | Default on first open | Notes |
|---|---|---|---|
| `single` | radio list, one choice | first listed option | declare at least one option: an optionless `single` has the value `""`, so no value can ever match it |
| `number` | one chip per value from `min` to `max` | `min` | both bounds required; keep the range small (every value gets a chip) |
| `multi` | checkbox list, zero or more | nothing selected | at most **30** options |

An option is:

| Field | Type | Default | Notes |
|---|---|---|---|
| `id` | string, required | — | referenced by `when` values and by `requires` |
| `name` | string, required | — | label in the editor |
| `requires` | array of option ids, same specificity | `[]` | `multi` only — see 2.2 |

Ids are exact, case-sensitive strings and must be unique: specificity ids within the manifest,
option ids within their specificity (the gate enforces both). Prefer kebab-case
(`duel-commander`, `river-bridges`).

Do not name a specificity `and`, `or`, or `not`. Those three keys are reserved by the `when`
grammar (section 4.6).

### 2.2 `requires` (extension prerequisites)

`requires` lists the option ids that must be selected together with this one. It is AND, and it
is honoured only for `multi` options; `single` options carry the field but selection ignores it.

The engine keeps selections dependency-closed in both directions:

- **Selecting** an option also selects its whole transitive `requires` closure. Selecting
  `river-bridges` (requiring `river`) selects `river` too.
- **Deselecting** an option also deselects everything that transitively requires it. Unticking
  `river` also unticks `river-bridges`.
- The graph must be **acyclic** and may only reference options of the same specificity. A
  `requires` cycle or an unknown target fails validation.

Consequences worth designing around:

- `requires` is an ordering constraint, not a visibility rule. It never makes an item visible or
  hidden, so gate content with `when` rather than relying on prerequisites.
- A selection that is impossible for the game ("Bridges without River") cannot be reached, which
  in turn means a `when` that assumes it is a dead rule and fails validation.
- `requires` cannot express "B conflicts with C". Use `when` for that.

Multiply conditional rules by the selection and keep the JSON flat: one `when` per content item,
as shown below with `extensions`:

```json
{
  "id": "extensions",
  "name": "Extensions",
  "type": "multi",
  "options": [
    { "id": "base", "name": "Base Game" },
    { "id": "river", "name": "River" },
    { "id": "river-bridges", "name": "Bridges & Ferries", "requires": ["river"] },
    { "id": "inns", "name": "Inns & Cathedrals" }
  ]
}
```

This is an illustrative board-game manifest, not a shipped game.

### 2.3 Rulebooks

| Field | Type | Default | Notes |
|---|---|---|---|
| `id` | string, required | — | referenced by `PageRef.rulebook` |
| `name` | string, required | — | label in the "Choose Rules PDF" dialog |
| `asset` | string, required | — | path **relative to `assets/`**, e.g. `rulebooks/mtg-comprehensive-rulebook.pdf` |
| `when` | condition | `{}` (always) | whether this rulebook is offered for the current selection |

Behaviour:

- Opening the Rulebook sub-tool considers only rulebooks whose `when` matches the current
  selection: exactly one → it opens directly; several → a picker dialog; none → "No rulebook
  available".
- Digimon ships two rulebooks that both use `"when": {}`, so its Rulebook sub-tool always shows
  the picker. That is the intended way to offer a manual *and* a comprehensive rules PDF.
- The asset is copied to the app cache under its **file name only**, so keep PDF file names
  unique across the whole `assets/rulebooks/` folder.
- A listed `asset` that does not exist renders an empty viewer; the gate does not check file
  existence, so verify by hand (`ls app/src/main/assets/rulebooks/`).

### 2.4 What the app does with specificities

- **First open** for a game that has at least one specificity and no saved configuration: a
  centered dialog appears with the defaults from 2.1. "Start" persists the choice.
- **Afterwards** the top bar shows a pill summarising the selection in manifest order:
  `single` → the selected option's `name`; `number` → `<value>P` (`2P`); `multi` →
  `+<count> ext` when at least one is selected, omitted when none is. Parts are joined with
  ` · `. A game with zero specificities has no dialog and no pill (Pokémon is such a game).
- Confirming a selection re-filters every sub-tool and every page reference immediately.
- If you add a specificity to a game players have already configured, they get that
  specificity's default value; they are not re-prompted.

---

## 3. Content files

### 3.1 Which file feeds which sub-tool

| File | Top-level keys | Sub-tool | Presentation |
|---|---|---|---|
| `setup.json` | `pages`, `steps` | Setup | horizontal carousel, one numbered page per step (`1.`, `2.`, …) |
| `turn.json` | `pages`, `phases` | Turn Structure | phase list → step list → step detail with Prev/Next |
| `win.json` | `pages`, `winConditions` | Win Condition | vertical list of cards |
| `keywords.json` | `pages`, `keywords` | Keywords | searchable, category-filtered, expandable cards |

Both keys default to `[]`, so `{}` is a valid (empty) file. All four files share the top-level
`pages` array — see section 5.

A `phase` is:

| Field | Type | Default |
|---|---|---|
| `name` | string, required | — |
| `steps` | array of content item | `[]` |
| `when` | condition | `{}` (always) |

A phase whose every step is filtered out by the current selection disappears entirely (the
phase itself is dropped once it has no visible steps), which is usually what you want for
"only in this variant" phases.

### 3.2 A content item (`RuleItem`)

`steps`, `phases[].steps` and `winConditions` all hold the same shape:

| Field | Type | Default | Notes |
|---|---|---|---|
| `title` | string | `null` | bold sub-heading |
| `text` | array of paragraphs | `[]` | one string per paragraph |
| `bullets` | array of bullet strings | `[]` | rendered with a leading `• ` |
| `image` | string (asset path) | `null` | see 3.4 |
| `when` | condition | `{}` (always) | visibility for the current selection |

```json
{
  "title": "Once per turn:",
  "bullets": [
    "**Normal Summon** a monster in face-up Attack Position",
    "**Set** a monster in face-down Defense Position",
    "Levels 1–4 require no Tributes"
  ]
}
```

(From `app/src/main/assets/rules/yugioh/turn.json`.)

**Render order is fixed**: `title` → every paragraph of `text` → every bullet → `image`. Field
order inside the JSON does not matter. You cannot interleave a bullet between two paragraphs or
put an image in the middle; split into two items instead. In `setup.json` the carousel adds the
step number itself, so never prefix a Setup title with "1.".

Sub-headings that used to sit inside a paragraph become their own item: a `title`-less item whose
first `text` entry is the heading in bold, followed by its bullets — as in Pokémon's zones step:

```json
{
  "text": ["**The Zones**"],
  "bullets": [
    "**1. Active Spot** — The front row. Only the Active Pokemon can attack. You must always have exactly 1 Active Pokemon.",
    "**2. Bench** — Back row for Benched Pokemon (up to 5). They can use Abilities but cannot attack. Damage to Benched Pokemon ignores Weakness and Resistance."
  ]
}
```

### 3.3 Keywords

`keywords.json` holds `keywords`, an array of:

| Field | Type | Default | Notes |
|---|---|---|---|
| `name` | string, required | — | search matches this field only |
| `category` | string | `null` | becomes a filter chip; omit for uncategorised |
| `text` | array of paragraphs | `[]` | |
| `bullets` | array of bullet strings | `[]` | |
| `when` | condition | `{}` (always) | |

```json
{
  "name": "Spell Speed",
  "category": "Core Mechanics",
  "text": ["Determines which effects can chain to which."],
  "bullets": [
    "**Speed 1:** Normal/Equip/Continuous/Field/Ritual Spells, Ignition/Trigger/Flip effects",
    "**Speed 2:** Quick-Play Spells, Normal/Continuous Traps, Multi-Trigger effects"
  ]
}
```

(From `app/src/main/assets/rules/yugioh/keywords.json`.)

Keywords are sorted alphabetically by `name` for display, so author them in any order. Category
chips come from the distinct `category` values, also sorted. Search is case-insensitive and only
matches `name` — put searchable aliases in the name, not in the text.

### 3.4 Text, bullets, bold, images

- **Bold** is the only inline markup: `**like this**`. A `**` without a partner renders
  literally rather than swallowing the rest of the line.
- No other Markdown is rendered. `# headings`, `- lists`, `[links](...)`, `_italics_` and tables
  appear as raw characters. Use `title`, `bullets` and separate items instead.
- Do not start a bullet string with `-`, `*` or `•`: the renderer prepends `• ` itself.
- `image` is a path **relative to `assets/`**, not to the rules folder:
  `"rulebooks/images/pokemon-field.png"` resolves to
  `app/src/main/assets/rulebooks/images/pokemon-field.png`. A missing or undecodable image
  renders nothing (no placeholder, no error), so screenshot-test new images.
- Images are decoded with `BitmapFactory` and drawn full width. PNG/JPEG/WebP work; SVG does not.
  Keep diagrams reasonably small — they are decoded at full size.

---

## 4. The `when` grammar

Every `when` is a JSON **object**. It is evaluated against the player's current selection; when
it does not match, the item is hidden. An absent `when` means "always".

### 4.1 Condition shapes

| JSON | Meaning |
|---|---|
| `{}` | always matches |
| `{ "format": "standard" }` | one predicate |
| `{ "players": "4", "extensions": "river" }` | implicit **AND** of one predicate per key |
| `{ "and": [c1, c2, ...] }` | explicit AND |
| `{ "or": [c1, c2, ...] }` | explicit OR |
| `{ "not": c }` | negation of a whole condition |

- The keys of a plain object are specificity ids. Two different specs in one object are ANDed.
- `and` / `or` take an array of condition **objects**, not strings.
- `not` takes one condition object: `{ "not": { "extensions": "inns" } }`.
- An object that contains `and`, `or` or `not` may contain **nothing else**. Mixing an operator
  with another key is a parse error, and a parse error hides the whole game's rules.

### 4.2 Value shapes (the right-hand side of a spec id)

| JSON | Variant | Meaning |
|---|---|---|
| `"x"` | `Eq` | `single`: the selection is exactly `x`. `number`: the value is `x`. `multi`: `x` is selected |
| `["x", "y"]` | `AllOf` | **every** listed value matches — AND, not OR |
| `[]` | `NoneSelected` | the `multi` set is empty; **never** matches a `single` or `number` |
| `{ "or": ["x", "y"] }` | `AnyOf` | at least one listed value matches |
| `{ "and": ["x", "y"] }` | `AllOf` | same as the bare list |
| `{ "not": "x" }` / `{ "not": ["x", "y"] }` | `NoneOf` | none of the listed values match |
| `{ "not": [] }` | — | matches anything (useless; avoid) |

A value object must have exactly one key, and that key must be `or`, `and` or `not`.

**Values are always strings**, including numbers: write `"players": "4"`, not `"players": 4`. A
JSON number is rejected by the parser ("Expected a JSON string for spec value"), which fails the
whole file and hides the game's rules.

### 4.3 Evaluation rules per specificity type

`single`
: `Eq` compares the selected option id. `AllOf` requires every listed value to equal the same
  selection, so a one-item list is just another way to write `Eq`, and a two-value list is
  **never** true; `[]` is never true too. `AnyOf` and `NoneOf` behave as expected.

`number`
: The selected integer is compared **as a string**: `"4"`, `"12"`, no leading zeros or signs.
  Same rules as `single` otherwise. The value is always set (defaults to `min`), so `NoneOf`
  works as "not 4".

`multi`
: `Eq` = that option is selected. `AllOf` = every listed option is selected. `AnyOf` = at least
  one is selected. `NoneOf` = none of them is selected. `[]` = nothing is selected.

Fail-safe rules:

- An **unknown specificity id** makes that predicate false — the item is hidden. Under `not`, the
  negation then makes it visible, so typos are dangerous in negations (the gate catches every
  unknown id, in any position).
- An **unknown option id** simply never matches, but `{ "format": { "not": "typo" } }` is
  *always* true. Again: the gate catches it.

### 4.4 Worked examples

Always visible — use `{}` or omit `when` (both are identical):

```json
{ "title": "Reduce LP to 0", "text": ["Each player starts with 8000 Life Points."] }
```

One of several formats (real, from `yugioh/setup.json`):

```json
{ "format": { "or": ["v5-dm", "v6-gx", "v7-5ds", "v8-zexal"] } }
```

A single format (real, from `yugioh/setup.json`):

```json
{ "format": "v7-5ds" }
```

Two specs at once — implicit AND (illustrative board game):

```json
{ "players": "4", "extensions": "river" }
```

Explicit AND, identical in meaning to the line above:

```json
{ "and": [ { "players": "4" }, { "extensions": "river" } ] }
```

Either of two specs (illustrative):

```json
{ "or": [ { "players": "2" }, { "players": "5" } ] }
```

Any of several extensions selected (illustrative):

```json
{ "extensions": { "or": ["river", "river-bridges"] } }
```

Both extensions selected (illustrative) — the bare list is AND:

```json
{ "extensions": ["river", "inns"] }
```

No extension selected (illustrative) — `[]`:

```json
{ "extensions": [] }
```

Every extension except Inns & Cathedrals (illustrative) — a value-level `not`:

```json
{ "extensions": { "not": "inns" } }
```

A whole condition negated (illustrative):

```json
{ "not": { "extensions": "inns" } }
```

Three or four players (illustrative) — numbers are quoted strings:

```json
{ "players": { "or": ["3", "4"] } }
```

### 4.5 Full illustrative board-game example

This is a complete, made-up game used to show `number` and `multi` together. Nothing here ships.

`manifest.json`:

```json
{
  "specificities": [
    { "id": "players", "name": "Players", "type": "number", "min": 2, "max": 5 },
    {
      "id": "extensions",
      "name": "Extensions",
      "type": "multi",
      "options": [
        { "id": "base", "name": "Base Game" },
        { "id": "river", "name": "River" },
        { "id": "river-bridges", "name": "Bridges & Ferries", "requires": ["river"] },
        { "id": "inns", "name": "Inns & Cathedrals" }
      ]
    }
  ],
  "rulebooks": [
    { "id": "official", "name": "Official Rulebook", "asset": "rulebooks/example-rulebook.pdf", "when": {} },
    { "id": "river-rules", "name": "River Rules", "asset": "rulebooks/example-river.pdf", "when": { "extensions": "river" } }
  ]
}
```

`setup.json`:

```json
{
  "pages": [
    { "rulebook": "river-rules", "pages": [2], "when": { "extensions": "river" } },
    { "rulebook": "official", "pages": [3, 4], "when": {} }
  ],
  "steps": [
    { "title": "Lay out the board", "text": ["Place the starting tile face up in the middle of the table."] },
    { "title": "Five players only", "text": ["Use every starting tile."], "when": { "players": "5" } },
    {
      "title": "Add the river tiles",
      "text": ["Shuffle the river tiles and build the river before the first turn."],
      "when": { "extensions": { "or": ["river", "river-bridges"] } }
    }
  ]
}
```

Note that `base` is selectable but never referenced. `multi` forces nothing; content that always
applies uses `{}`, not `{ "extensions": "base" }`. Use a `base` option only if some content must
be hidden when the player unticks it.

### 4.6 Illegal conditions

```json
{ "and": [ { "format": "standard" } ], "players": "4" }
```

Operator plus another key in the same object → parse error.

```json
{ "or": { "format": "standard" } }
```

`or` / `and` with an object instead of an array → parse error.

```json
{ "not": "inns" }
```

`not` needs a condition object; negate a *value* with
`{ "extensions": { "not": "inns" } }` instead.

```json
{ "title": "Standard only", "when": ["format", "standard"] }
```

A `when` must be an object, not an array of strings.

```json
{ "and": "x" }
```

`and` / `or` / `not` cannot be used as specificity ids either — that is exactly why they are
reserved.

---

## 5. Page references and the jump button

Each of the four content files may carry a top-level `pages` array. It powers the
**jump-to-pages** button (a book icon, "Open rulebook pages") in the top bar of Setup, Turn
Structure, Win Condition and Keywords. It does not render anything else.

```json
{
  "pages": [
    { "rulebook": "official", "pages": [7, 8], "when": { "format": "standard" } },
    { "rulebook": "v6", "pages": [12], "when": {} }
  ]
}
```

| Field | Type | Default | Notes |
|---|---|---|---|
| `rulebook` | string, required | — | must be a `rulebooks[].id` from `manifest.json` |
| `pages` | array of integers, **1-based** | `[]` | must not be empty — omit the whole ref instead |
| `when` | condition | `{}` (always) | first matching ref wins |

Resolution when the player taps the button:

1. Take the `pages` array of the sub-tool currently open and pick the **first** ref whose `when`
   matches — so order the array most-specific first.
2. Look up that ref's `rulebook` id in the manifest.
3. If no ref matched, the id is unknown, or `pages` is empty, just open the Rulebook sub-tool
   (one matching rulebook → it opens at page 1, several → the picker, none → empty state).
4. Otherwise open that rulebook at `pages[0]`.

Details that bite:

- **Pages are 1-based in JSON.** The viewer converts: `pages[0] - 1`. Do not pre-subtract.
- **Only `pages[0]` is used.** The rest of the list documents the relevant range for authors; it
  is not navigated. Keep the whole range anyway — it is the bookkeeping that stops numbers
  drifting.
- **Empty list is a validation error.** If you do not know the pages, delete the whole ref;
  the button then falls back as in step 3.
- **Page numbers are not checked against the PDF.** A number past the end opens the last page
  (the viewer clamps), and `0`/negative opens page 1. Verify numbers against the actual PDF.
- **Keep the ref's `when` consistent with the referenced rulebook's `when`.** The Rulebook screen
  only resolves rulebooks that match the current selection; a ref pointing at a rulebook your
  selection hides falls back to the picker/empty state.
- One ref list per file, so each sub-tool jumps independently; it is fine for `win.json` to have
  no `pages` at all.
- No shipped game uses `pages` yet — every existing game relies on the fallback. The shape above
  is therefore illustrative, but it is exactly what the loader and gate accept.

---

## 6. Recipes

### 6.1 Add a new game

1. Confirm the game's `Game.id` (`app/src/main/java/com/turnzero/games/<name>/<Name>Game.kt`).
   The asset folder must use that exact string.
2. Create `app/src/main/assets/rules/<gameId>/`.
3. Add `manifest.json`. Start minimal and grow:

   ```json
   {
     "specificities": [],
     "rulebooks": [
       { "id": "official", "name": "Official Rulebook", "asset": "rulebooks/<game>-rulebook.pdf", "when": {} }
     ]
   }
   ```

4. Add the four content files. An empty object is a valid starting point:

   ```json
   {}
   ```

5. Put the PDF and any diagrams in `app/src/main/assets/rulebooks/` (images under
   `rulebooks/images/`). File names must be unique across the whole folder.
6. Fill in `steps`, `phases`, `winConditions`, `keywords`. Keep `when` for later — plain content
   with no `when` is correct for a single-variant game.
7. Add `specificities` only if the rules genuinely change with a parameter (format, player count,
   expansions). Zero specificities means no first-open dialog and no pill — that is the right
   default for a game with one ruleset.
8. Run the gate (section 7).

### 6.2 Add a specificity

1. Append an object to `specificities` in `manifest.json`:

   ```json
   { "id": "players", "name": "Players", "type": "number", "min": 2, "max": 5 }
   ```

   `single` needs `options` (first one is the default); `number` needs both `min` and `max`;
   `multi` gets a list of options (≤ 30) and may use `requires`.

2. Reference it in content with `when`. Existing content needs no edit: absent `when` means
   "always", so nothing disappears when you add a spec.
3. Add variant content beside the base content. Prefer one item per variant with an explicit
   `when` over stuffing variants into one paragraph:

   ```json
   [
     { "title": "Draw four cards", "text": ["..."], "when": { "players": "2" } },
     { "title": "Draw three cards", "text": ["..."] }
   ]
   ```

4. For a `single` spec that replaces content per variant (Yu-Gi-Oh formats), either give every
   variant its own `when` or make the last one unconditional as the fallback. Avoid leaving a
   variant combination with no content.
5. Add a `<name>P` / `+N ext`-style summary check: reopen the tool and confirm the pill reads
   sensibly in manifest order.
6. Run the gate.

### 6.3 Add or edit content

1. Pick the file by sub-tool (section 3.1).
2. Append an item to `steps` (Setup, or a phase's `steps`), `winConditions`, or `keywords`.
   Declaration order is display order; keywords are re-sorted alphabetically by name.
3. Fill only the fields you need — `title`, `text`, `bullets`, `image` and `when` are all
   optional; only `phases[].name` and `keywords[].name` are required.
4. Respect the fixed render order (title → text → bullets → image) and split the item if you
   need interleaving.
5. Wrap facts that only apply to some selections in a `when`.
6. Run the gate — it validates every `when` in every file and fails on any id typo.

### 6.4 Add an extension with prerequisites

1. Declare the prerequisite and the dependent in the same `multi` specificity, prerequisite
   first for readability:

   ```json
   {
     "id": "extensions",
     "name": "Extensions",
     "type": "multi",
     "options": [
       { "id": "river", "name": "River" },
       { "id": "river-bridges", "name": "Bridges & Ferries", "requires": ["river"] }
     ]
   }
   ```

2. Say what the extension adds, gated on the extension itself:

   ```json
   { "title": "Bridges", "text": ["..."], "when": { "extensions": "river-bridges" } }
   ```

   You do **not** need to add `"river"` to that `when`: selecting Bridges forces River on, so
   `{ "extensions": "river-bridges" }` already implies it. Adding the prerequisite is harmless,
   just redundant.
3. Rules that change with *either* River or Bridges:

   ```json
   { "when": { "extensions": { "or": ["river", "river-bridges"] } } }
   ```

4. Rules that only apply while no extension is selected:

   ```json
   { "when": { "extensions": [] } }
   ```

5. Rules that apply unless a specific expansion is in play:

   ```json
   { "when": { "extensions": { "not": "river-bridges" } } }
   ```

6. `requires` may chain (`ferries` requiring `river-bridges` which requires `river`), but must
   stay acyclic. Selecting the deepest option selects the whole chain; unticking any link unticks
   everything downstream. The gate rejects cycles and unknown targets.

---

## 7. The validation gate

`RulesContentValidationTest` runs against the real asset folder. It is the guard that turns a
content mistake into a red test instead of silently missing rules in the app.

```bash
ANDROID_HOME=/mnt/c/Users/matla/AppData/Local/Android/Sdk ./gradlew :app:testDebugUnitTest --tests 'com.turnzero.tools.rules.RulesContentValidationTest' --console=plain
```

(Unit tests run with `:app` as the working directory, so the test reads
`app/src/main/assets/rules` directly.)

Two tests walk every game folder:

- `every game folder parses and is well-formed` — parses the five files through the real
  `AssetRulesRepository`, then checks the manifest, every `when` reference, and every page ref.
- `every when condition is satisfiable` — enumerates every reachable selection and fails any
  `when` that can never be true (a dead rule).

A further group of tests pins the checkers themselves against a known-good fixture and known-bad
mutations, so the gate cannot silently stop catching things.

Every failure message starts with `game '<gameId>':` and, where possible, names the offending
item by JSON path.

| Message | Meaning | Fix |
|---|---|---|
| `game 'x' must parse` | the folder did not load: a file is missing, or one of the five has a JSON or grammar error | read the logcat warning (`TZ/Rules`), or decode the files with `python3 -m json.tool` |
| `duplicate specificity ids [...]` | two specificities share an `id` | rename one |
| `specificity 'x' has duplicate option ids [...]` | duplicate option ids in one specificity | rename one |
| `specificity 'x' option 'y' requires unknown options [...]` | `requires` points at a nonexistent option | fix the id (same specificity only) |
| `specificity 'x' has a requires cycle: a -> b -> a` | circular prerequisites | break the cycle |
| `number specificity 'x' must declare both min and max` | missing a bound | add both |
| `number specificity 'x' has min 5 > max 4` | inverted range | fix the bounds |
| `multi specificity 'x' has 31 options; at most 30 are supported (the engine enumerates 2^n subsets in an Int bitmask)` | too many checkboxes | split the specificity or the game |
| `<where> references unknown specificity 'ghost'` | `when` names a spec that does not exist | fix the key |
| `<where>: specificity 'x' has no options ['nope']` | `when` names an option that does not exist | fix the value |
| `<where>: specificity 'x' expects an int, got 'three'` | a number spec was compared to a non-numeric string | use a quoted integer |
| `<where>: specificity 'x' value 5 is outside 2..4` | number outside `min..max` | fix the value or the bounds |
| `<where> references unknown rulebook 'nope'` | `PageRef.rulebook` is not in the manifest | fix the id |
| `<where> lists no pages; omit the ref when the pages are unknown` | `pages: []` | delete the ref or list pages |
| `unsatisfiable when in <game> at <where>: <condition>` | the condition can never be true | see section 4 and the pitfalls |

`<where>` looks like `setup.steps[0] 'Shuffle & Cut'`, `turn.phases[2] 'Main Phase 1'`,
`turn.phases[2].steps[0] 'Once per turn:'`, `win.winConditions[0] 'Reduce LP to 0'`,
`keywords.keywords[3] 'Spell Speed'`, `manifest.rulebooks[0] 'Official Rulebook'`,
`setup.pages[1]` — indices are 0-based JSON positions.

The gate does **not** check: PDF or image files existing, page numbers being inside the PDF, or
whether content reads well. Those stay human review.

---

## 8. Pitfalls

1. **Bare list means AND, not OR.** For a `single` spec, `{ "format": ["standard", "commander"] }`
   is never true — it requires the selection to equal both. Write
   `{ "format": { "or": ["standard", "commander"] } }` for "one of these". The gate reports the
   AND form as an unsatisfiable `when`.
2. **`[]` is multi-only.** `{ "format": [] }` never matches a `single` or `number` and shows up
   as a dead rule. `[]` means "no extension selected".
3. **Numbers are strings.** `"players": 4` is a parse error that hides the whole game's rules.
   Write `"players": "4"`.
4. **Contradictory conditions.** `{ "format": "commander", "players": "5" }` is dead if commander
   is always 2 players. Prerequisites create contradictions too: a `when` of
   `{ "and": [ { "extensions": "river-bridges" }, { "not": { "extensions": "river" } } ] }` is
   dead, because selecting Bridges forces River. Compose one condition per item and let the gate
   prove each one reachable.
5. **Forgetting `requires`.** An expansion whose rules assume another expansion needs `requires`,
   otherwise the player can enable it alone and see rules that reference a component that is not
   in play. Remember it is ordering only: it does not hide anything.
6. **Fabricating page numbers.** The gate cannot see inside the PDF; a wrong number opens the
   wrong (or clamped) page. Check against the actual file, and keep `pages: []` out of the JSON.
7. **Duplicate ids.** Specificity ids must be unique in the manifest, option ids inside their
   specificity, and rulebook ids in the manifest. Duplicated ids make `when` resolution and page
   refs ambiguous.
8. **`and` / `or` / `not` as ids.** These are grammar keywords. A spec with one of these ids
   cannot be referenced at all.
9. **Typos inside a negation.** `{ "format": { "not": "commanderr" } }` is always true — it does
   not hide wrong content, it shows content to everyone. Run the gate, which validates values in
   every position.
10. **Unknown ids fail closed — except under `not`.** Unknown spec/option ids make a predicate
    false, but negating a false predicate yields true. There is no runtime warning.
11. **Markdown that is not supported.** Headings, italic, links, tables and `-` lists render as
    literal characters. Use `title`, `bullets`, `**bold**` and multiple items.
12. **Comments and trailing commas.** Assets are parsed by `kotlinx.serialization` as strict JSON;
    either one takes the whole game's rules down.
13. **A phase that is conditional all the way down disappears.** If every step of a phase is
    filtered out, the phase itself is hidden. If it must always be listed, leave one step with no
    `when`.
14. **Field names drift silently.** `ignoreUnknownKeys = true` means `"when"` misspelled as
    `"When"` is ignored and the item becomes unconditional — the gate will not catch a rename,
    only a wrong reference. Copy field names from real assets in `app/src/main/assets/rules/`.
15. **Huge number ranges.** The editor renders one chip per value in `min..max`; a range of 1..100
    is 100 chips. Keep number specificities short (player counts, not scores).
