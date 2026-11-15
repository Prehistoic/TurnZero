> **Superseded by [2026-11-14-rules-tool-revamp-design.md](2026-11-14-rules-tool-revamp-design.md)** — the Rules tool was reimplemented with JSON-driven content; this document is historical.

# Rules Tool — Design Spec

## Overview

The Rules tool provides in-app game rules reference for TurnZero. It supports per-game, per-format rulesets, with configurable parts (sections) that vary by game. The tool is universal (`*`) and registered once, while each game registers its own rules content via a `RulesProvider` interface.

## Architecture

### Data Model

```kotlin
data class RulesConfig(
    val rulesetId: String,
    val rulesetName: String,
    val setup: List<RuleStep>,
    val winCons: List<WinCondition>,
    val playSequence: List<RuleStep>,
    val keywords: List<Keyword>?,          // null = Keywords tab hidden
)

data class RuleStep(
    val title: String,
    val summary: String,                    // visible inline when collapsed
    val detail: String,                     // full explanation shown on expand
)

data class WinCondition(
    val title: String,
    val description: String,
)

data class Keyword(
    val name: String,
    val definition: String,
    val category: String? = null,
)
```

### Resolution

```
RulesTool.Screen(onBack)
  -> RulesViewModel reads gameId from SavedStateHandle
    -> RulesProvider.getConfigs(gameId): List<RulesConfig>
      -> InMemoryRulesProvider resolves to game-specific config list
```

**`RulesProvider` interface:**

```kotlin
interface RulesProvider {
    fun getConfigs(gameId: String): List<RulesConfig>
}
```

**`InMemoryRulesProvider`** maps `gameId` to hardcoded config lists. Games like Yu-Gi-Oh register their configs (e.g., `YuGiOhRules.configs`) in this provider.

### File Layout

All files in `:app` module, following the existing Dice/Coin tool pattern:

```
tools/rules/
  RulesTool.kt                 Tool impl, universal ("*")
  RulesViewModel.kt            @HiltViewModel, manages RulesUiState
  RulesModels.kt               RulesConfig, RuleStep, WinCondition,
                                Keyword, RulesProvider, RulesUiState, TabInfo
  RulesProvider.kt             InMemoryRulesProvider implementation
  RulesScreen.kt               Main screen: chip bar + TabRow + HorizontalPager
  RulesSetupTab.kt             Setup: scrollable expandable step list
  RulesWinConsTab.kt           WinCons: minimalistic bordered cards
  RulesPlaySequenceTab.kt      Play Sequence: same layout as Setup
  RulesKeywordsTab.kt          Keywords: searchable alphabetical expandable list

games/yugioh/
  YuGiOhRules.kt               Yu-Gi-Oh rules configs (content filled later)
```

### Tool Interface

```kotlin
class RulesTool : Tool {
    override val id = "rules"
    override val name = "Rules"
    override val icon = Icons.Default.MenuBook       // or AutoMirrored.Filled.MenuBook
    override val supportedGames = setOf("*")
}
```

No OptionsSheet, no TopBarActions. The tool is self-contained.

Registration in `ServiceLocator.tools`:

```kotlin
val tools: Set<Tool> = setOf(
    DiceTool(), CoinTool(),
    RulesTool(),                                    // NEW
    YuGiOhLifeCounter(), YuGiOhRulingsSearcher()
)
```

## Screen Layout

### Overall Structure

The tool screen is a single composable with three horizontal bands:

1. **Ruleset chip bar** — `LazyRow` of `FilterChip` components at the top. Switching resets to the first tab and collapses all expansions.
2. **Tab row** — Material 3 `TabRow` with `Tab` components. Tabs are dynamically built from `visibleTabs` (excluding parts where the data is `null`).
3. **HorizontalPager** — Swipeable pages, one per tab. Synchronized with the tab row bidirectionally.

**Deadzone for swipes:** The pager uses a `SnapFlingBehavior` with an increased horizontal velocity threshold to prevent accidental page changes during vertical scrolling of step/keyword lists.

### Setup Tab (`RulesSetupTab.kt`)

- Scrollable `LazyColumn` of `RuleStep` cards
- Each card shows: numbered circle (1, 2, 3...), title, summary (collapsed state)
- Tap expands: reveals full detail text below the summary
- Accordion behavior: only one step expanded at a time (tapping a different step collapses the previous one; tapping the currently expanded step collapses it)
- Uses `animateItem()` on the expanded content for smooth transitions

### WinCons Tab (`RulesWinConsTab.kt`)

- `Column` of `WinCondition` cards
- Each card: colored left border (2-3dp, `primary` color), title in `titleMedium`, description in `bodyMedium`
- No expand/collapse — always fully visible since content is concise
- Simple vertical `Column` with `verticalScroll`

### Play Sequence Tab (`RulesPlaySequenceTab.kt`)

- Identical layout and behavior to the Setup tab
- Reuses the same `RuleStep` data type
- Both tabs delegate to a shared `RulesStepList` composable that accepts a header and list of steps

### Keywords Tab (`RulesKeywordsTab.kt`)

- Search bar (`OutlinedTextField` or `BasicTextField` with `Search` decoration) at top, always visible
- Alphabetically ordered `LazyColumn` of `Keyword` entries
- Each entry: name in `titleMedium`, category chip (optional), definition hidden when collapsed
- Tap expands: reveals definition inline. Tap again collapses.
- Typing in the search bar filters the list to matching keywords (case-insensitive name match)
- Search query resets when switching rulesets

## State Management

### RulesUiState

```kotlin
data class RulesUiState(
    val gameId: String = "",
    val configs: List<RulesConfig> = emptyList(),
    val selectedRulesetIndex: Int = 0,
    val selectedTabIndex: Int = 0,
    val expandedStepIndex: Int? = null,
    val expandedKeywordIndex: Int? = null,
    val keywordSearchQuery: String = "",
    val isLoading: Boolean = true,
) {
    val currentRuleset: RulesConfig?
        get() = configs.getOrNull(selectedRulesetIndex)

    val visibleTabs: List<TabInfo>
        get() = buildList {
            add(TabInfo("setup", "Setup"))
            add(TabInfo("wincons", "WinCons"))
            add(TabInfo("sequence", "Sequence"))
            if (currentRuleset?.keywords != null) {
                add(TabInfo("keywords", "Keywords"))
            }
        }

    val filteredKeywords: List<Keyword>
        get() {
            val keywords = currentRuleset?.keywords ?: return emptyList()
            val sorted = keywords.sortedBy { it.name.lowercase() }
            if (keywordSearchQuery.isBlank()) return sorted
            return sorted.filter { it.name.contains(keywordSearchQuery, ignoreCase = true) }
        }
}
```

### RulesViewModel Events

| Event | Description |
|---|---|
| `selectRuleset(index: Int)` | Sets `selectedRulesetIndex`, resets `selectedTabIndex = 0`, clears expanded states |
| `selectTab(index: Int)` | Sets `selectedTabIndex`, triggers `animateScrollToPage` via `LaunchedEffect` |
| `onPageChanged(index: Int)` | Callback from `HorizontalPager` swipes, syncs `selectedTabIndex` |
| `toggleStep(index: Int)` | Toggles `expandedStepIndex` (sets to null if same index tapped) |
| `toggleKeyword(index: Int)` | Toggles `expandedKeywordIndex` |
| `setSearchQuery(query: String)` | Sets `keywordSearchQuery`, clears `expandedKeywordIndex` |

The ViewModel resolves `gameId` from `SavedStateHandle` and loads configs from `RulesProvider` in `init {}`.

## DI & Registration

- `RulesProvider` interface is bound to `InMemoryRulesProvider` via a Hilt module
- `RulesViewModel` injects `RulesProvider` via constructor
- No DataStore persistence needed (rules are read-only reference content)

## Testing

| Test Class | Coverage |
|---|---|
| `RulesViewModelTest` | State transitions: ruleset switching resets tabs & expansions, tab visibility when keywords is null, search filtering, accordion toggling |
| `RulesModelsTest` | `filteredKeywords` ordering & filtering correctness, `visibleTabs` computation for configs with/without keywords |
| `YuGiOhRulesTest` | All configs have non-empty required fields (setup, winCons, playSequence) |

Test naming follows convention: `` `method_scenario_expected` ``

## Future Extensions

- **Additional parts:** Add a new nullable field to `RulesConfig` (e.g., `val banlist: List<BannedCard>? = null`), add a new tab in `visibleTabs`, create a new tab composable, add a new page to the pager.
- **External rules loading:** Swap `InMemoryRulesProvider` with an asset-based or network-backed implementation.
- **Favorites/bookmarks:** If needed, add a bookmark icon to individual steps/keywords stored in DataStore.
