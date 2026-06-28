# Dice Tool — Design Spec

**Date:** 2026-06-28  
**Status:** Approved  
**Approach:** Hilt + ViewModel + Repository (per AGENTS.md target architecture)

## Overview

Implement the first fully-functional Tool in TurnZero: a dice roller. Users configure a pool of dice (count, face type, color), roll them with a shuffle animation, and view history.

## Requirements

| # | Requirement |
|---|------------|
| R1 | Support standard RPG dice: d2, d4, d6, d8, d10, d12, d20, d100 |
| R2 | Add/remove dice from a pool, with face count and color per die |
| R3 | Dice colors: Red, Blue, Green, Yellow, White, Black |
| R4 | Number-shuffle animation on roll with bounce settle, results clearly displayed |
| R5 | Total shown after roll |
| R6 | Roll history accessible via popup from top bar (history icon button) |
| R7 | History capped at 50 entries |
| R8 | Dice pool and selected color persist across sessions per game |
| R9 | Presets are dev-defined per game (not user-creatable), scoped to current gameId |
| R10 | Tool navbar (all tools): Tools (left) → tool switcher sheet, Options (right) → tool-specific options sheet |
| R11 | All changes in Options sheet apply instantly (no Apply button) |
| R12 | Main screen shows dice pool individually (not grouped), roll button, and total post-roll |

## Screen Layouts

### Main Screen

```
┌──────────────────────────────────┐
│  ← Dice                      📜  │  TZTopBar: back + history icon
├──────────────────────────────────┤
│                                   │
│      ┌──┐ ┌──┐ ┌──┐             │
│      │d6│ │d6│ │d6│             │  Dice pool — each die individually
│      │ 4│ │ 2│ │ 5│             │  Results after roll, blank before
│      └──┘ └──┘ └──┘             │
│                                   │
│             Total: 11            │  Only visible after roll
│                                   │
│          ╔══════════╗           │
│          ║  ROLL!   ║           │
│          ╚══════════╝           │
│                                   │
├──────────────────────────────────┤
│  🔄 Tools           ⚙️ Options   │  Tool navbar (all tools)
└──────────────────────────────────┘
```

### Options Sheet

```
┌──────────────────────────────────┐
│  Options                     ✕   │  TZTopBar-style header
├──────────────────────────────────┤
│                                   │
│  [Fireball 8d6] [Stealth d20]   │  Dev-defined presets, centered
│                                   │
├──────────────────────────────────┤
│                                   │
│  d6 × 2  🔴  [−] [+]           │  Pool grouped by face+color
│  d20     🟢  [−] [+]           │  [−] removes one, [+] adds one
│                                   │
├──────────────────────────────────┤
│                                   │
│  ┌──┐ ┌──┐ ┌──┐ ┌──┐           │  Add dice row (2 rows, 4 per row)
│  │d2│ │d4│ │d6│ │d8│           │  Tokens colored with current color
│  └──┘ └──┘ └──┘ └──┘           │
│  ┌──┐ ┌──┐ ┌──┐ ┌───┐         │
│  │10│ │12│ │20│ │100│          │
│  └──┘ └──┘ └──┘ └───┘         │
│     ● ● ● ● ● ●                 │  Color selector, centered
│                                   │
└──────────────────────────────────┘
```

### History Popup

Full-screen modal bottom sheet triggered by history icon in top bar. Scrollable list with:
- Timestamp (relative: "2 min ago", "Today 3:15 PM")
- Dice summary (e.g., "3d6 + 1d20")
- Individual results with colors
- Total

### Preset Data

Presets are defined in Kotlin code, per game. The Dice tool loads presets matching the current `gameId`. Example:

Defined in `DicePresets.kt` in the dice tool package. A function `getPresetsForGame(gameId: String): List<DicePreset>` returns presets for the current game context. Universal presets (when no game context) are returned for `"*"`.

```kotlin
fun getPresetsForGame(gameId: String): List<DicePreset> = when (gameId) {
    "yugioh" -> listOf(
        DicePreset(name = "Standard 2d6", dice = listOf(
            Die(faceCount = 6, color = RED), Die(faceCount = 6, color = RED)
        )),
        DicePreset(name = "Attack Roll d20", dice = listOf(
            Die(faceCount = 20, color = GREEN)
        ))
    )
    else -> listOf(
        DicePreset(name = "Standard 2d6", dice = listOf(
            Die(faceCount = 6, color = RED), Die(faceCount = 6, color = RED)
        ))
    )
}
```

## Data Model

```kotlin
enum class DiceColor(val hex: Long) {
    RED(0xFFE53935), BLUE(0xFF1E88E5), GREEN(0xFF43A047),
    YELLOW(0xFFFDD835), WHITE(0xFFFFFFFF), BLACK(0xFF212121)
}

data class Die(
    val id: String = UUID.randomUUID().toString(),
    val faceCount: Int,
    val color: DiceColor
)

data class DiceRoll(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val dice: List<Die>,
    val results: List<Int>,
    val total: Int
)

data class DiceUiState(
    val gameId: String = "*",
    val dice: List<Die> = emptyList(),
    val selectedColor: DiceColor = DiceColor.RED,
    val presets: List<DicePreset> = emptyList(),
    val activePresetId: String? = null,
    val history: List<DiceRoll> = emptyList(),
    val historyExpanded: Boolean = false,
    val isRolling: Boolean = false,
    val results: List<Int>? = null,
    val total: Int? = null
)
```

## Persistence

All through `DataStore<Preferences>` via `DiceRepository`:

| Key | Type | Scope | Format |
|-----|------|-------|--------|
| `DICE_POOL_<gameId>` | String | Per-game | JSON array of `Die` |
| `DICE_SELECTED_COLOR` | String | Global | `DiceColor` name |
| `DICE_HISTORY` | String | Global | JSON array of last 50 `DiceRoll` |

- `kotlinx.serialization` handles JSON encode/decode
- Pool is per-game so switching context preserves different setups
- History is global, capped at 50, newest first
- Presets are NOT persisted — they are hardcoded per game in Kotlin

## Architecture

### File Map

```
app/src/main/java/com/turnzero/tools/dice/
├── DiceTool.kt              (modified) — Tool interface, wires ViewModel
├── DiceViewModel.kt          (new) — @HiltViewModel, state + actions
├── DiceRepository.kt         (new) — @Inject, DataStore read/write
├── DiceScreen.kt             (new) — main composable
├── DiceUiState.kt            (new) — UiState data class + model types
├── DicePoolArea.kt           (new) — dice pool display (main screen)
├── DiceAnimation.kt          (new) — shuffle + settle composable
├── DiceOptionsSheet.kt       (new) — Options bottom sheet composable
├── DiceHistorySheet.kt       (new) — History modal bottom sheet
├── DicePresets.kt            (new) — per-game preset definitions

core/data/src/main/java/com/turnzero/core/data/
└── UserPreferences.kt        (modified) — add dice preference keys

core/tools-api/src/main/java/com/turnzero/core/tools/api/
└── Tool.kt                   (modified) — add OptionsSheet()

app/src/main/java/com/turnzero/navigation/
└── TZNavHost.kt              (modified) — tool bottom nav bar

Infrastructure:
├── app/src/main/java/com/turnzero/TZApplication.kt   (modified) — @HiltAndroidApp
├── app/src/main/java/com/turnzero/MainActivity.kt    (modified) — @AndroidEntryPoint
├── app/build.gradle.kts                              (modified) — Hilt, ksp, serialization
└── gradle/libs.versions.toml                         (modified) — serialization dep
```

### Data Flow (UDF)

```
User action → ViewModel event handler
    → Repository persists to DataStore
    → StateFlow<DiceUiState> emits new state
    → Screen collects via collectAsStateWithLifecycle()
    → Compose recomposes
```

### ViewModel Actions

| Action | Effect |
|--------|--------|
| `changeDieCount(faceCount, color, delta)` | Adds or removes dice of given type+color (clamped >=0), persists pool |
| `setSelectedColor(color)` | Changes default color for new dice added from the add area |
| `roll()` | Generates random results, starts animation, prepends history, persists |
| `animationComplete()` | Sets isRolling=false, displays results |
| `loadPreset(preset)` | Replaces pool with preset dice, persists pool |
| `toggleHistory()` | Toggles history sheet visibility |
| `clearHistory()` | Empties history, persists |

### Tool Interface Change

```kotlin
interface Tool {
    val id: String
    val name: String
    val icon: ImageVector
    val supportedGames: Set<String>
    
    @Composable fun Screen(onBack: () -> Unit)
    @Composable fun OptionsSheet(onDismiss: () -> Unit) {}
}
```

### DiceTool Integration

```kotlin
class DiceTool : Tool {
    override val id = "dice"
    override val name = "Dice"
    override val icon = Icons.Default.Casino
    override val supportedGames = setOf("*")

    @Composable
    override fun Screen(onBack: () -> Unit) {
        val viewModel: DiceViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        DiceScreen(uiState = uiState, onBack = onBack, ...)
    }

    @Composable
    override fun OptionsSheet(onDismiss: () -> Unit) {
        val viewModel: DiceViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        DiceOptionsSheet(uiState = uiState, onDismiss = onDismiss, ...)
    }
}
```

The `gameId` is extracted from `SavedStateHandle` in the ViewModel — Hilt automatically provides the NavBackStackEntry's SavedStateHandle, which contains the route's `gameId` argument. No change needed to the Tool interface signature.

## Animation

Shuffle + settle using Compose animation APIs only:

1. `isRolling = true` → LaunchedEffect drives number cycling:
   - Fast phase: random digit every 50ms for ~600ms
   - Deceleration: every 100ms → 200ms → 400ms over ~400ms
2. Settle on actual results with `animateFloatAsState(spring())` scale bounce (1.0 → 1.15 → 1.0)
3. `animationComplete()` → `isRolling = false`, results display as static
4. Die cards: rounded rectangle, 2dp colored border, face label, center number (bold)

## Tool Navbar (all tools)

When on `ToolDetail` route, `TZNavHost` renders:
- **Tools (left):** Opens `ToolSwitcherSheet` (existing component, moves from FAB to navbar)
- **Options (right):** Calls `tool.OptionsSheet(onDismiss)`
- FAB is removed from ToolDetail screen

## Testing

- `DiceViewModelTest`: state transitions, roll logic, preset loading, history trimming
- `DiceRepositoryTest`: DataStore read/write, JSON serialization, history cap
- `DiceScreenTest`: Compose UI smoke test (presence of pool, roll button, navbar)
