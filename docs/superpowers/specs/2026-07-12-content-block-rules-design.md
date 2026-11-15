> **Superseded by [2026-11-14-rules-tool-revamp-design.md](2026-11-14-rules-tool-revamp-design.md)** — the Rules tool was reimplemented with JSON-driven content; this document is historical.

# Content Block Rules — Design Doc

**Date:** 2026-07-12
**Status:** Approved

## Problem

The Rules tool renders rule content (`RuleStep.detail`, `WinCondition.description`, `Keyword.definition`) as raw markdown strings via `com.mikepenz.markdown.m3.Markdown`. The default paragraph composable from the library produces cramped inter-paragraph spacing, making distinct paragraphs blend together visually. Additionally, embedding images within markdown strings is awkward and prevents placing images between specific content blocks with proper spacing.

## Solution

Replace markdown strings with a structured `ContentBlock` sealed interface. Each block type maps to a specific Compose composable with explicit, configurable vertical spacing. Remove the `multiplatform-markdown-renderer-m3` dependency entirely.

## Data Model

### `ContentBlock` — `app/src/main/java/com/turnzero/tools/rules/RulesModels.kt`

```kotlin
sealed interface ContentBlock {
    data class Paragraph(val text: String) : ContentBlock
    data class SubHeading(val text: String) : ContentBlock
    data class BulletList(val items: List<String>) : ContentBlock
    data class Image(val uri: String) : ContentBlock
}
```

### Updated existing models

```kotlin
data class RuleStep(
    val title: String,
    val summary: String,
    val contents: List<ContentBlock>,   // was: detail: String
)

data class WinCondition(
    val title: String,
    val contents: List<ContentBlock>,   // was: description: String
)

data class Keyword(
    val name: String,
    val contents: List<ContentBlock>,   // was: definition: String
    val category: String? = null,
)
```

## Renderer

### `ContentBlockRenderer` — `app/src/main/java/com/turnzero/tools/rules/ContentBlockRenderer.kt`

A single `@Composable` function:

```kotlin
@Composable
fun ContentBlockRenderer(blocks: List<ContentBlock>, modifier: Modifier = Modifier)
```

**Spacing rules (Spacer between blocks in a Column):**

| Block type | Spacing above (unless first) |
|---|---|
| `Paragraph` | `8.dp` |
| `SubHeading` | `12.dp` |
| `BulletList` | `8.dp` |
| `Image` | `12.dp` |

**Bold parsing:** A shared `parseBoldText(text: String) -> AnnotatedString` function scans for `**...**` markers and applies `FontWeight.Bold` to matching spans. Used by `Paragraph`, `SubHeading`, and each bullet item in `BulletList`.

**Image rendering:** Same URI prefix convention currently in `TZMarkdown.kt`:
- `asset:<path>` — load from app assets, full-width
- `inline:<uri>` — 20dp inline icon
- Any other URL — load via Coil async, full-width

### Usage sites

| Tab composable | Before | After |
|---|---|---|
| `RulesSetupTab.kt` / `RulesTurnTab.kt` | `MarkdownContent(step.detail)` | `ContentBlockRenderer(step.contents)` |
| `RulesVictoryTab.kt` | `MarkdownContent(condition.description)` | `ContentBlockRenderer(condition.contents)` |
| `RulesKeywordsTab.kt` | `MarkdownContent(keyword.definition)` | `ContentBlockRenderer(keyword.contents)` |

### File changes

- **New:** `ContentBlockRenderer.kt`
- **Deleted:** `TZMarkdown.kt` (replaced)
- **Removed dependency:** `multiplatform-markdown-renderer-m3` from `libs.versions.toml` and `app/build.gradle.kts`

## Content Migration (`YuGiOhRules.kt`)

~139 content strings to convert across 6 config versions. Migration rules:

| Markdown pattern | Becomes |
|---|---|
| `\n\n` paragraph break | Separate `Paragraph` block |
| Line starting with `**Text:**` that anchors a section (followed by a bullet list or another block) | `SubHeading` |
| `**bold**` mid-paragraph (emphasis, not structural break) | Bold span within `Paragraph` |
| `\u2022 item` / `\u2022 **bold** rest` sequence | `BulletList` block |
| `![Alt](uri)` | `Image` block |

`WinCondition` (3 items) and `Keyword` (~55 items) are straightforward — mostly single `Paragraph` blocks with occasional `BulletList`.

`RuleStep` (~81 items) has the complex patterns (Battle Phase, Main Phase, Game Mat). Migration preserves semantic intent.

## Testing

- **`YuGiOhRulesTest.kt`:** Update assertions from `detail: String` to `contents: List<ContentBlock>`. Add structural assertions (e.g., Battle Phase has expected SubHeading + BulletList pattern).
- **`ContentBlockRendererTest.kt` (new):** Unit tests for `parseBoldText()` — plain text, single bold span, multiple bold spans, adjacent bold spans, edge cases (empty, no markers, nested markers treated as single).
- **`RulesModelsTest.kt`:** No structural changes needed (tests `RulesUiState` computed properties, which delegate to `Keyword`/`RuleStep` collections unaffected by the field rename).

## Architecture Notes

- `ContentBlock` lives in `com.turnzero.tools.rules` alongside the existing models — no new module needed.
- The `Tool` interface (`RulesTool.kt`) and `RulesProvider`/`RulesViewModel`/`RulesScreen` are unaffected — only the data models and tab composables change.
- `RulesPdfSheet.kt` is unaffected (renders PDF bitmaps, not markdown).
