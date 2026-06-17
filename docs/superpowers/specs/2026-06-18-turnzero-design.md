# TurnZero — Application Design Specification

**Date:** 2026-06-18
**Target:** Android (Kotlin, Jetpack Compose, min SDK 28)

## 1. Overview

TurnZero is an offline-first, all-in-one Android companion for boardgame and TCG players. It eliminates the need for multiple third-party apps by bundling game-agnostic tools (dice, coin) and game-specific tools (life counters, rulings searchers) into a single app navigated via tile grids.

## 2. Architecture

### Module Structure (Approach C — Hybrid)

```
:app                          Application module
├── games/yugioh/             Yu-Gi-Oh game screen + tools
├── tools/dice/               Dice roller (universal)
├── ui/                       Nav host, homepage tiles, layout sheet
└── di/                       Hilt DI wiring

:core:ui                      Shared UI primitives
├── GameTile, ToolTile composables
├── TZTheme (Material 3, dark/light)
├── LayoutSwitchingBottomSheet
├── ToolSwitcher FAB + Sheet
└── Navigation contracts

:core:tools-api               Tool contracts (pure interface)
├── interface Tool
├── interface Game
└── object ToolRegistry

:core:data                    Offline storage
├── DataStore (user prefs, favorites, layout choice)
└── Room DB (future: card data)
```

**Dependency flow:** `:app` → all core modules. `:core:ui` → `:core:tools-api`. Core modules are otherwise independent.

## 3. Tool Registry Pattern

```kotlin
interface Tool {
    val id: String
    val name: String
    val icon: ImageVector
    val supportedGames: Set<String>  // "*" = universal
    @Composable fun Screen(onBack: () -> Unit)
}

interface Game {
    val id: String
    val name: String
    val description: String
    val imageRes: Int
    val toolIds: List<String>
}

object ToolRegistry {
    fun getGames(): List<Game>
    fun getToolsForGame(gameId: String): List<Tool>
}
```

Tools register via Hilt `@Binds @IntoSet`. `ToolRegistry` resolves game→tool relationships at runtime. Universal tools appear for every game. Game-specific tools appear only for their game.

## 4. Navigation & Layout

### Screen Hierarchy
```
BottomNav (Games | Favorites | Settings)
  └─ Games tab
       └─ Game Grid (tiles for each supported game)
            └─ tap game → Tool Grid (tiles for tools available for that game)
                 └─ tap tool → Tool Screen (full-screen immersive tool)
```

### Fast Tool Switching (FAB)
- Small FAB in bottom-right corner of tool screen with swap icon
- Tap → modal bottom sheet showing all tools for current game as compact cards
- Tap a tool → replaces current tool screen (pop + push, no stack growth)
- Current tool highlighted in sheet as "Current"

### Layout Switching
- Button in top bar opens a Modal Bottom Sheet
- Options: Compact Grid (2-3 cols), Comfortable Grid (1-2 cols), List (full width)
- Preference persisted per-screen in DataStore

### Back Stack
Grid → Tool: pushes onto stack. FAB-switch: pops current tool, pushes new. System back returns to parent grid.

## 5. Data Model (DataStore)

| Key | Type | Default |
|---|---|---|
| `layout_games` | String | `"comfortable"` |
| `layout_tools` | String | `"comfortable"` |
| `favorite_games` | Set\<String\> | `[]` |
| `favorite_tools` | Set\<String\> | `[]` |
| `theme_mode` | String | `"system"` |

## 6. Security

| Area | Strategy |
|---|---|
| **Network** | `cleartextTrafficPermitted="false"`. HTTPS pinning for future APIs. No network in MVP. |
| **Storage** | DataStore (encrypted on API 28+). Future Room DB file-level encrypted. |
| **API keys** | Keystore-backed; never in source code or plain assets. |
| **Permissions** | Camera only at point-of-use via `registerForActivityResult`. Zero permissions for MVP. |
| **R8** | Obfuscation + shrinking enabled for release. |
| **Dependencies** | Version catalog (`libs.versions.toml`). Automated updates. |

## 7. Testing Strategy

| Layer | Tool | Scope |
|---|---|---|
| Unit | JUnit 5 + MockK | ToolRegistry, models |
| Compose UI | Compose Testing + Robolectric | Tiles, layout switching, bottom sheet, nav |
| Integration | Hilt Testing + fakes | DI wiring, tool registration verification |

CI: GitHub Actions running `./gradlew test lint` on every PR.

## 8. MVP Scope

**Boilerplate:**
- Gradle multi-module project with version catalog
- Hilt DI wiring
- Material 3 theme (dark/light with manual toggle + system follow)
- Compose Navigation with bottom nav + back stack
- Layout switching bottom sheet per grid screen
- FAB tool switcher on tool screens
- `network-security-config.xml`, R8 config

**First game: Yu-Gi-Oh**
- Game tile on home screen
- Tool grid showing dice roller, life counter, rulings searcher

**Tools (placeholder screens only):**
- Dice Roller (universal)
- Life Point Counter (Yu-Gi-Oh specific)
- Rulings Searcher (Yu-Gi-Oh specific)

**Favorites tab** — empty state
**Settings tab** — theme toggle + about section

## 9. Tool Matrix

| Tool | Scope | Games |
|---|---|---|
| Dice Roller | Universal | All (`*`) |
| Life Counter | Per-game | Yu-Gi-Oh (future: Magic, Pokemon, etc.) |
| Rulings Searcher | Yu-Gi-Oh only | yugioh |

## 10. MVP File Tree

```
:core:tools-api/
  Tool.kt, Game.kt, ToolRegistry.kt

:core:ui/
  theme/Color.kt, Type.kt, Theme.kt
  components/GameTile.kt, ToolTile.kt
  components/LayoutBottomSheet.kt
  components/TopBar.kt

:core:data/
  UserPreferences.kt
  (PreferencesDataStore extension in same file)

:app/
  TZApplication.kt
  MainActivity.kt
  navigation/Screen.kt, TZNavHost.kt, BottomNavBar.kt
  ui/games/GamesScreen.kt
  ui/tools/ToolsScreen.kt, ToolSwitcherSheet.kt
  ui/favorites/FavoritesScreen.kt
  ui/settings/SettingsScreen.kt
  games/yugioh/YuGiOhGame.kt, YuGiOhLifeCounter.kt, YuGiOhRulingsSearcher.kt
  tools/dice/DiceTool.kt
  di/ToolsModule.kt, GamesModule.kt, AppModule.kt
```
