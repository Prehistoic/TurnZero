# Changelog

All notable changes to TurnZero will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.5.0] - 2026-07-04

### Added
- **Life Counter tool** for Yu-Gi-Oh! with full LP tracking and editing
  - Dual-player LP counters with counting animation on change
  - Configurable timer (count-up or count-down) with vibration alert at 00:00:00
  - Calculator overlay for LP edits: auto-multiply logic (×100/×10/×1), zero-locking
  - Operation preview with dimmed trailing zeros and divide-by-2 support
  - Multi-undo history with colored entries and clear option
  - Options sheet: starting LP (8000/4000), timer mode, duration slider (30-60 min)
  - Forced landscape orientation with screen-on wake lock
  - State persists across navigation (scoped to Activity)
- **Tool interface** extended with `showBottomBar` property for fullscreen tools
- **CompositionLocal** for tool-to-nav communication (tool switcher, options sheet)
- **Bottom sheets** constrained to 60% width in landscape, 70% max height with scrolling
- **Tool switcher** supports scrolling for 5+ tools

### Changed
- Reorganized Yugioh package: `tools/`, `rules/` subdirectories per tool and ruleset
- Landscape tools get zero horizontal padding on top bar and content
- Bottom sheets now use `LazyColumn` for consistent scroll behavior

### Fixed
- Calc screen properly respects safe areas (status bar, navigation bar, display cutout)
- Timer pauses when leaving screen, labels show Chrono/Paused/Running states
- Options changes reset both LP and timer immediately
- Removed bottom navbar for landscape tools (Tools/Options moved to top bar)

## [0.4.1] - 2026-07-04

### Added
- Rulings Searcher tool with PDF viewer, category filter, and multiple rulesets (Standard, Speed Duel)
- Markdown rendering for step details, win condition descriptions, and keyword definitions
- Custom markdown image rendering via Coil 3
- Fullscreen PDF document viewer

### Changed
- Bump Gradle from 9.2.0 to 9.6.1 and AGP from 9.0.1 to 9.2.1
- Target Android SDK 37 (Android 16)
- Ruleset selector moved to TopBar dropdown with OptionsSheet
- Replaced ExposedDropdownMenuBox with stable DropdownMenu (fixes NoSuchMethodError crash)
- Replaced FlowRow with scrolling Row (fixes NoSuchMethodError crash)

### Fixed
- Pager tab sync when switching rulesets
- Intermediate tabs during pager animation
- Tab switch clearing expanded accordion states
- Same-tab accordion re-expansion prevention

## [0.4.0] - 2026-07-03

### Added
- CoinFlip tool with configurable coin count (1-8), 3D spin animation, and flip history (last 10)
- Per-game coin presets (Yu-Gi-Oh! defaults to 1 coin)
- Smart result display that omits zero-count sides and count prefix for single coin flips
- Coin count persists per-game across sessions via DataStore

## [0.3.1] - 2026-06-29

### Changed
- Dice pool capped at 20 dice maximum
- Die sizing now relative to screen width (35%/60%/80%) instead of hardcoded values
- Total display centered between dice and bottom navbar
- History limited to last 10 rolls (session-only, not persisted)

### Fixed
- White dice now render with black border and black text for visibility
- Black dice now render with white border and white text for visibility
- Dice numbers and labels match border color for all dice
- ToolBottomNavBar height now matches TZBottomNavBar exactly
- Fixed total area reserving space so dice never shift when total appears
- Fixed tap-to-roll showing no highlight (indication = null)
- Fixed brief scale animation triggering on tap before roll

### Refactored
- Removed all `runBlocking` calls from main thread (replaced with `LaunchedEffect`)
- Deleted dead code: `Color.kt` (9 unused constants), `ToolSwitcherFAB`, `FAVORITE_GAMES` key, `Game.imageRes`
- Extracted shared components: `TZDivider`, `TZEmptyState`, `navItemColors`
- `FavoritesScreen` now supports LIST layout in addition to GRID
- `LayoutBottomSheet`: fixed stale state memory and disabled Apply button when unchanged
- Added 14 unit tests for `ToolRegistry`
- App icon updated to use TurnZero logo with white background

## [0.3.0] - 2026-06-28

### Added
- Fully-functional Dice Roller tool with configurable dice pool
- Support for standard RPG dice: d2, d4, d6, d8, d10, d12, d20, d100
- Six dice color options per die (Red, Blue, Green, Yellow, White, Black)
- Number-shuffle animation on roll with deceleration and bounce settle
- Roll history panel (session-only, last 10 rolls)
- Game-specific dice presets (dev-defined, e.g., Yu-Gi-Oh! 1d6 and 2d6)
- New tool bottom navbar with Tools (switcher) and Options for all tool screens
- Tool interface extended with OptionsSheet and TopBarActions composables
- Hilt dependency injection activated across the app
- Adaptive die sizing based on screen width and dice count

### Changed
- Dice pool persists per-game across sessions
- Tool detail screens now use a dedicated bottom bar replacing the FAB
- Tool navbar replaces Games/Favorites/Settings bar on tool screens
- History icon moved from FAB to top bar actions

## [0.2.2] - 2026-06-28

### Changed
- Shortened tool names: Dice Roller → Dice, Life Point Counter → LP Counter, Rulings Searcher → Rulings
- Increased tool tile grid icon size from 48dp to 64dp
- Tool names now single-line with bold weight
- Reduced spacing between tool name and subtitle in favorites

## [0.2.1] - 2026-06-28

### Added
- Custom app logo and app icon with adaptive icon safe zone inset
- CHANGELOG.md following Keep a Changelog format
- AGENTS.md changelog update enforcement rules
- Release workflow now auto-extracts changelog sections for GitHub Releases

### Changed
- Top bar logo uses Image instead of Icon to preserve original colors and transparency

### Fixed
- Theme mode now persists correctly across app restarts
- Tool tiles in grid layout now have uniform height regardless of text length

## [0.2.0] - 2026-06-28

### Added
- About section in Settings with version info, changelog link, and update checker
- OSS licenses screen with auto-detected dependency data (powered by AboutLibraries)
- Remote update mechanism (GitHub Releases API polling, APK download, FileProvider install)
- Social media links (Web, Discord, GitHub) in About screen
- Custom app logo with adaptive icon support
- CI/CD workflows (PR build checks, tag-based releases, version bump enforcement)
- Pull request template and issue templates (bug report, feature request)
- Apache 2.0 license
- CHANGELOG.md (keepachangelog format)

### Changed
- Tools screen grid tiles now have uniform height regardless of text length
- About screen uses mihon-style row layout instead of pill buttons
- Update notification uses standard Android Toast

### Fixed
- Update checker no longer displays raw API URL on HTTP errors
- License screen properly handles nullable fields from AboutLibraries API

## [0.1.0] - 2026-06-18

### Added
- Initial project scaffold with multi-module Gradle setup (app, core:tools-api, core:ui, core:data)
- Jetpack Compose + Material 3 UI with dark/light theme support
- Six color theme presets (Purple, Ocean, Forest, Sunset, Crimson, Rose)
- Navigation host with bottom nav bar (Games, Favorites, Settings)
- Games screen with grid/list layout toggle
- Tools screen with grid/list layout toggle and FAB tool switcher
- Favorites screen with favoriting support
- Settings screen with Appearance section (theme mode, color theme, language, layout)
- Search in settings
- Yu-Gi-Oh! game with Life Counter and Rulings Searcher tools (placeholder)
- Dice Roller tool
- DataStore-backed user preferences
- Service Locator dependency provision
- Tool registry with game-to-tool filtering
