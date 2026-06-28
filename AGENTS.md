# TurnZero — Agent Instructions

## Project Overview

TurnZero is an Android companion app for tabletop and trading card games. It provides a toolkit architecture where Games (e.g., Yu-Gi-Oh!) have associated Tools (e.g., Dice Roller, Life Counter, Rulings Searcher).

- **Package:** `com.turnzero`
- **Min SDK:** 28 (Android 9) — **Target/Compile SDK:** 36
- **Language:** Kotlin — **UI:** Jetpack Compose + Material 3
- **Build:** Gradle 9.2.0 (Kotlin DSL) + AGP 9.0.1
- **Kotlin:** 2.2.10 (built into AGP 9.x — do NOT apply `org.jetbrains.kotlin.android` plugin)

## Module Structure

```
:app              → Application entry, navigation, screens, game/tool implementations
:core:tools-api   → Interfaces (Tool, Game) and ToolRegistry
:core:ui          → Shared Compose components (tiles, top bar, bottom sheets, theme)
:core:data        → DataStore preferences layer
```

**Dependency graph:**
```
:app → :core:tools-api, :core:ui, :core:data
:core:ui → :core:tools-api
```

## Build Commands

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK (R8 minification enabled)
./gradlew testDebugUnitTest      # Run unit tests
./gradlew connectedDebugAndroidTest  # Run instrumented tests
./gradlew lint                   # Run Android Lint
./gradlew clean assembleDebug    # Clean build
```

## Code Conventions

### Naming
- **App-level components:** `TZ` prefix → `TZApplication`, `TZTheme`, `TZTopBar`, `TZBottomNavBar`
- **Composable functions:** PascalCase → `GamesScreen()`, `GameTile()`, `LayoutBottomSheet()`
- **Non-composable functions:** camelCase → `getGames()`, `createRoute()`
- **Constants/Keys:** SCREAMING_SNAKE_CASE → `LAYOUT_GAMES`, `FAVORITE_TOOLS`
- **Enum values:** SCREAMING_SNAKE_CASE → `COMPACT`, `COMFORTABLE`, `LIST`

### File Organization
- One public type per file. Small related types (enums, data classes) may co-locate with their primary consumer.
- Package structure follows feature boundaries: `ui/games/`, `ui/tools/`, `games/yugioh/`, `tools/dice/`
- Core modules use `com.turnzero.core.<module>` packages.

### Style
- Wildcard imports are acceptable for Compose packages (`androidx.compose.foundation.layout.*`, `androidx.compose.runtime.*`, `androidx.compose.material3.*`).
- Use `data object` for sealed class members.
- Use `@OptIn(ExperimentalMaterial3Api::class)` where needed — do not suppress globally.
- All public composables must accept `modifier: Modifier = Modifier` as a parameter.

## Architecture Guidelines

### Current State
The project uses a **Service Locator** pattern (`ServiceLocator.kt`) for dependency provision. State is managed with `remember { mutableStateOf() }` inside composables.

### Target State (for new features)
When adding new features or refactoring existing ones, follow these patterns:

- **ViewModels** for screen-level state and business logic. Use `hiltViewModel()` with `@HiltAndroidApp` and `@HiltViewModel` annotations. Inject dependencies via constructor.
- **State holders** (`data class` with immutable fields) exposed as `StateFlow` from ViewModels. Collect in composables with `collectAsStateWithLifecycle()`.
- **Unidirectional Data Flow (UDF):** Events flow up (lambdas), state flows down (State objects).
- **Repository pattern** for data access. Repositories are injected into ViewModels.
- **Side effects** in `LaunchedEffect` or `SideEffect` blocks — never in the composable body directly.

### Navigation
- Routes are defined as a `sealed class Screen` in `navigation/Screen.kt`.
- Use type-safe navigation with `NavType` for path parameters.
- Bottom nav destinations use `popUpTo` + `launchSingleTop` + `restoreState`.

### Adding a New Game or Tool
1. Implement the `Game` interface in `games/<name>/`
2. Implement `Tool` interfaces in `games/<name>/` or `tools/<name>/`
3. Register instances in `ServiceLocator.tools` and `ServiceLocator.games`
4. The `ToolRegistry` handles discovery automatically

## Testing

### Requirements
- **Every new feature must include tests.** No exceptions.
- Unit tests go in `src/test/java/` — use JUnit 4 + MockK.
- Instrumented tests go in `src/androidTest/java/` — use AndroidX Test + Compose Test.
- ViewModels: test state transitions, error handling, and side effects.
- Repositories: test data transformations and caching logic.
- Compose UI: test critical user flows with `createComposeRule()`.

### Test Naming
```
class <ClassUnderTest>Test {
    @Test
    fun `<method>_<scenario>_<expected result>`() { ... }
}
```

### Example
```kotlin
class ToolRegistryTest {
    @Test
    fun `getToolsForGame returns only tools matching game`() {
        val registry = ToolRegistry(setOf(TestGame), setOf(TestTool, OtherTool))
        val tools = registry.getToolsForGame("test-game")
        assertEquals(1, tools.size)
    }
}
```

## Logging

- Use `android.util.Log` with a consistent tag per module: `Log.d("TZ/<Feature>", "...")`.
- **Never log sensitive data:** tokens, passwords, user identifiers, API keys, or personal information.
- Use `Log.w` for recoverable errors, `Log.e` for failures that affect the user.
- Wrap verbose/debug logs in `if (BuildConfig.DEBUG)` to avoid leaking internals in release builds.
- For structured logging needs, consider Timber in the future — do not introduce new logging libraries without discussion.

## Security

- **No secrets in source code.** API keys, tokens, and credentials must come from `local.properties` (gitignored) or Android Keystore.
- **ProGuard/R8** is enabled for release builds. Keep rules in `app/proguard-rules.pro`. Only add `-keep` rules that are strictly necessary — avoid blanket package keeps.
- **Network security:** When adding network calls, use HTTPS only. Pin certificates for critical endpoints. Configure `network_security_config.xml` if needed.
- **DataStore:** Do not store sensitive data in DataStore Preferences. Use EncryptedSharedPreferences or Android Keystore for secrets.
- **Dependencies:** Keep dependencies minimal. When adding a new dependency, verify it is actively maintained and from a trusted source.
- **Permissions:** Request only the minimum permissions needed. Document the reason for each permission in the manifest.

## Git Workflow

### Branch Strategy
```
main            ← stable, always deployable
  └── feature/* ← new features, branched from main
  └── fix/*     ← bug fixes, branched from main
  └── refactor/*← refactoring, branched from main
```

- **Never commit directly to `main`.** Always use a feature/fix/refactor branch.
- Branch naming: `feature/add-dice-history`, `fix/life-counter-overflow`, `refactor/extract-viewmodel`
- Keep branches short-lived. Merge back to `main` when the feature is complete and tested.

### Commit Messages
Follow **Conventional Commits**:

```
<type>: <imperative description>

[optional body]
```

**Types:** `feat`, `fix`, `refactor`, `chore`, `test`, `docs`, `style`, `perf`, `ci`, `build`

**Rules:**
- Imperative mood: "add tool registry" not "added tool registry"
- Lowercase, no period at the end
- Keep the first line under 72 characters
- Add a body for non-trivial changes explaining *why*, not *what*

**Examples:**
```
feat: add dice roll history screen with persistent storage
fix: prevent life counter from going below zero
refactor: extract GamesViewModel from GamesScreen composable
test: add unit tests for ToolRegistry filtering logic
chore: update Compose BOM to 2025.01.00
```

### Pull Requests
- Squash-merge into `main` to keep history clean.
- PR title follows the same Conventional Commits format.
- Ensure all checks pass (build, lint, tests) before merging.
- **PR description must include the relevant CHANGELOG entry** for the changes being merged.

### Changelog

The project maintains a `CHANGELOG.md` at the repository root following [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) format.

**Rules:**
- Every PR that changes functionality MUST update `CHANGELOG.md` under the `## [Unreleased]` section.
- Use the categories: `Added`, `Changed`, `Deprecated`, `Removed`, `Fixed`, `Security`.
- Each entry is a single bullet point describing the change from the user's perspective.
- Before creating a release, move the `[Unreleased]` entries to a new version section with the date.
- The version header must match the tag format exactly (e.g., `## [0.2.0] - 2026-06-28`).
- The release workflow automatically extracts the changelog section for the tagged version as the GitHub Release body.

**Example entry:**
```markdown
## [Unreleased]

### Added
- About section in Settings with version info and update checker

### Fixed
- Tool tiles now have uniform height in grid layout
```

## Dependency Management

- All dependency versions are centralized in `gradle/libs.versions.toml`.
- Use version references (`version.ref`) for shared versions across multiple artifacts.
- **Do not add inline version strings** in `build.gradle.kts` files.
- When updating a dependency, verify compatibility with the current AGP and Kotlin versions.
- AGP 9.x has built-in Kotlin support — do NOT apply `org.jetbrains.kotlin.android` in any module. Only `org.jetbrains.kotlin.plugin.compose` is needed for Compose modules.

## Common Pitfalls

- **Do not use `runBlocking` on the main thread.** Use `LaunchedEffect` or `collectAsStateWithLifecycle()` for DataStore reads in composables.
- **Do not hold `Context` references** in composables or state holders — use `LocalContext.current` or inject via constructor.
- **Do not bypass the module boundaries.** `:core:ui` must not depend on `:app`. `:core:data` must not depend on `:core:ui`.
- **Do not add wildcard ProGuard keeps.** Each `-keep` rule must target a specific class or pattern with a comment explaining why.
