# Coin Tool Design

**Date:** 2026-07-03
**Status:** Approved

## Overview

A reusable Coin Flip tool for the TurnZero companion app. Mirrors the Dice tool's proven architecture (ViewModel + StateFlow, DataStore persistence, Hilt DI). Adds a 3D spin animation with randomized rotation axes.

## Requirements

- Choose number of coins to flip (1-8, configurable in OptionsSheet)
- Per-game presets (Yu-Gi-Oh! defaults to 1 coin)
- Combined results displayed as count: "3 Heads · 1 Tail"
- History of last 10 flips, accessible from top bar
- No color selection (unlike Dice)
- Cool 3D spin animation with random rotation axis per flip

## Architecture

```
CoinTool (implements Tool)
  └── CoinViewModel (@HiltViewModel, StateFlow<CoinUiState>)
        ├── CoinRepo interface → DataStoreCoinRepo (@Singleton, JSON → DataStore)
        ├── CoinPresets (pure function returning List<CoinPreset>)
        └── CoinUiState (immutable data class)
```

```
CoinTool.Screen() → CoinScreen
  ├── CoinAnimation (random-axis 3D spin via graphicsLayer)
  └── ResultBar ("3 Heads · 1 Tail")

CoinTool.OptionsSheet() → CoinOptionsSheet
  ├── Preset chips (per-game presets)
  └── Coin count stepper (1-8)

CoinTool.TopBarActions() → History toggle → CoinHistorySheet
```

## Data Model

### Coin
A single coin in the flip pool. Only an ID is needed — no face count or color.

```kotlin
data class Coin(val id: String)  // e.g., "coin-1" through "coin-8"
```

### CoinFace
Enum for flip results.

```kotlin
enum class CoinFace { HEAD, TAIL }
```

### CoinFlip
A recorded flip with all its coins, individual results, and derived counts.

```kotlin
data class CoinFlip(
    val id: String,
    val timestamp: Long,
    val coins: List<Coin>,
    val results: List<CoinFace>,
    val headsCount: Int,
    val tailsCount: Int
)
```

### CoinPreset
Game-specific preset defining how many coins to flip by default.

```kotlin
data class CoinPreset(
    val id: String,
    val name: String,
    val gameId: String,
    val coinCount: Int
)
```

### CoinUiState
Complete state exposed by the ViewModel as a StateFlow.

```kotlin
data class CoinUiState(
    val gameId: String,
    val coinCount: Int,
    val presets: List<CoinPreset>,
    val history: List<CoinFlip>,
    val isFlipping: Boolean,
    val results: List<CoinFace>,
    val headsCount: Int,
    val tailsCount: Int,
    val showHistory: Boolean
)
```

### CoinRepo
Repository interface for persisting state. Matches the DiceRepo pattern — `Flow` for reactive DataStore reads, `suspend` for writes.

```kotlin
interface CoinRepo {
    fun getCoinCount(gameId: String): Flow<Int>
    suspend fun saveCoinCount(gameId: String, count: Int)
    fun getHistory(): Flow<List<CoinFlip>>
    suspend fun saveHistory(history: List<CoinFlip>)
}
```

## Presets

Defined by a pure function `getCoinPresets(gameId: String): List<CoinPreset>`:

| Game | Preset |
|------|--------|
| `"yugioh"` | "Standard" — 1 coin |
| `"*"` (default for unlisted games) | "Standard" — 1 coin |

Future games can extend this with game-specific presets (e.g., MTG could have "First Play" at 1 coin and "Boss Rush" at 3 coins).

## Visual Design

### Coin Face Rendering
- **Shape:** Circle, ~64dp diameter
- **Heads:** Golden radial gradient (`#FFD700` → `#DAA520`) with person silhouette emoji centered
- **Tails:** Silver radial gradient (`#C0C0C0` → `#A0A0A0`) with star emoji centered
- **Rim:** Subtle circular border in a metallic color for depth
- **Layout:** Multiple coins stack horizontally, wrapping into rows as needed (adaptive layout like Dice)

### 3D Spin Animation

Uses `LaunchedEffect` with timed phases, same pattern as `DiceAnimation`:

| Phase | Duration | Behavior |
|-------|----------|----------|
| Rapid spin | 0-600ms | Random faces cycle at 80ms intervals, each with random `rotationX`/`rotationY` combo |
| Slow reveal | 600-800ms | Slow to 150ms intervals, fewer remaining coins switch |
| Settle | 800-1000ms | All coins lock on final results, spin decelerates to 0 |

**Random axis:** Each coin gets a random value for `rotationX` (0-720deg) and `rotationY` (0-720deg), animated with `animateFloatAsState` using a deceleration easing curve. The 3D effect uses `graphicsLayer { rotationX; rotationY }` with `cameraDistance = 8f * density` for realistic perspective depth.

**Tap scale:** A brief scale animation on the outer container triggers the flip, same pattern as the Dice tap-to-roll.

### Result Bar
Center-aligned text below coins, format: `"N Heads · M Tails"`. Uses `MaterialTheme.typography.titleMedium` in `onSurfaceVariant` color. Only visible when results are present (not during animation).

### CoinOptionsSheet
`ModalBottomSheet` with:
- **Presets section:** Row of `FilterChip`s if presets exist for the current game
- **Coin count stepper:** Center-aligned row with minus button, current count (e.g., "3 coins"), plus button. Min 1, max 8.

### CoinHistorySheet
`ModalBottomSheet` with:
- Header: "History" title + "Clear" text button
- `LazyColumn` of `FlipEntry` composables
- Each entry: flip number (reverse index), coin summary (e.g., "4 coins"), results tally (e.g., "3H · 1T"), timestamp (HH:mm)

## Registration

Add `CoinTool()` to `ServiceLocator.tools`:
```kotlin
val tools: Set<Tool> = setOf(
    DiceTool(), CoinTool(), YuGiOhLifeCounter(), YuGiOhRulingsSearcher()
)
```

Add `"coin"` to `YuGiOhGame.toolIds`:
```kotlin
override val toolIds = listOf("dice", "coin", "ygo-life-counter", "ygo-rulings-searcher")
```

Create Hilt `CoinModule` binding `DataStoreCoinRepo` to `CoinRepo` in `SingletonComponent`.

## File Structure

```
app/src/main/java/com/turnzero/tools/coin/
  ├── CoinTool.kt              # Tool interface implementation
  ├── CoinViewModel.kt         # State management
  ├── CoinScreen.kt            # Main screen composable
  ├── CoinAnimation.kt         # 3D spin animation
  ├── CoinModels.kt            # Data classes + CoinRepo interface
  ├── CoinPresets.kt           # Per-game preset definitions
  ├── CoinOptionsSheet.kt      # Bottom sheet for settings
  ├── CoinHistorySheet.kt      # Bottom sheet for history
  └── DataStoreCoinRepo.kt     # DataStore persistence

app/src/main/java/com/turnzero/di/
  └── CoinModule.kt            # Hilt DI module

app/src/test/java/com/turnzero/tools/coin/
  └── CoinViewModelTest.kt     # Unit tests with FakeCoinRepo
```

## Navigation

No new routes needed — the Tool interface discovery handles this automatically. The `ToolRegistry.getToolsForGame("yugioh")` will include the Coin tool.

## Testing

### CoinViewModelTest (unit, JUnit 4 + JUnit 5 assertions via standard library)
- `initial state has 1 coin and no results`
- `changeCoinCount increases count up to 8`
- `changeCoinCount does not go below 1`
- `flip generates HEAD or TAIL for each coin`
- `flip sets isFlipping to true then false`
- `animationComplete adds to history`
- `history caps at 10 entries newest first`
- `loadPreset replaces coin count`
- `clearHistory empties history`
- `toggleHistory toggles showHistory flag`

### DataStoreCoinRepoTest (integration)
- `getCoinCount returns 1 when no data stored`
- `saveCoinCount and getCoinCount roundtrip`
- `saveHistory and getHistory roundtrip`
- `saveHistory caps at 10 entries newest first`

## DataStore Keys

Add to `UserPreferences`:
```kotlin
fun coinCountKey(gameId: String): Preferences.Key<String>
val COIN_HISTORY: Preferences.Key<String>  // stored as JSON list of CoinFlip
```
