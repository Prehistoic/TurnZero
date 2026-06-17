# TurnZero MVP Shell — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax.

**Goal:** Create a fully navigable Android app shell with Yu-Gi-Oh game tiles, dice/rulings/life-counter tool tiles, bottom nav bar, layout switching, FAB tool switcher, and Material 3 theming — no business logic, placeholder tool screens only.

**Architecture:** Hybrid modular — `:core:tools-api` (interfaces), `:core:ui` (composables/theme), `:core:data` (preferences), and `:app` (screens, games, tools, DI, nav). Tools register via Hilt multibindings into `ToolRegistry`.

**Tech Stack:** Kotlin 2.0, Jetpack Compose + Material 3, Compose Navigation, Hilt DI, DataStore Preferences, API 28+

**Dependencies to use (from libs.versions.toml):**
- agp = "8.5.2"
- kotlin = "2.0.10"
- compose-bom = "2024.09.00"
- hilt = "2.51.1"
- hilt-navigation = "1.2.0"
- lifecycle = "2.8.4"
- datastore = "1.1.1"
- activity-compose = "1.9.1"
- core-ktx = "1.13.1"
- coroutines = "1.8.1"

---
---

## Task 1: Root Project Scaffolding

**Files to CREATE:** 
- `gradle/libs.versions.toml`
- `settings.gradle.kts`
- Root `build.gradle.kts`
- `gradle.properties`
- `app/build.gradle.kts`
- `core/tools-api/build.gradle.kts`
- `core/ui/build.gradle.kts`
- `core/data/build.gradle.kts`

### Step 1: Create `gradle/libs.versions.toml`

```toml
[versions]
agp = "8.5.2"
kotlin = "2.0.10"
compose-bom = "2024.09.00"
compose-compiler = "1.5.14"
hilt = "2.51.1"
hilt-navigation = "1.2.0"
lifecycle = "2.8.4"
datastore = "1.1.1"
activity-compose = "1.9.1"
core-ktx = "1.13.1"
coroutines = "1.8.1"

[libraries]
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-material-icons = { group = "androidx.compose.material", name = "material-icons-extended" }
navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version = "2.7.7" }
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hilt-navigation" }
lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }
datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }
core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "core-ktx" }
activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activity-compose" }
coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
junit = { group = "junit", name = "junit", version = "4.13.2" }
compose-ui-test = { group = "androidx.compose.ui", name = "ui-test-junit4" }
compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
hilt-testing = { group = "com.google.dagger", name = "hilt-android-testing", version.ref = "hilt" }
mockk = { group = "io.mockk", name = "mockk", version = "1.13.12" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version = "2.0.10-1.0.24" }
```

### Step 2: Create root `build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
}
```

### Step 3: Create `settings.gradle.kts`

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolution {
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "TurnZero"
include(":app")
include(":core:tools-api")
include(":core:ui")
include(":core:data")
```

### Step 4: Create `gradle.properties`

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
```

### Step 5: Create `app/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.turnzero"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.turnzero"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:tools-api"))
    implementation(project(":core:ui"))
    implementation(project(":core:data"))

    implementation(libs.core.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.navigation.compose)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}
```

### Step 6: Create `core/tools-api/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.turnzero.core.tools.api"
    compileSdk = 34

    defaultConfig {
        minSdk = 28
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
}
```

### Step 7: Create `core/ui/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.turnzero.core.ui"
    compileSdk = 34

    defaultConfig {
        minSdk = 28
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:tools-api"))

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.lifecycle.viewmodel.compose)
}
```

### Step 8: Create `core/data/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.turnzero.core.data"
    compileSdk = 34

    defaultConfig {
        minSdk = 28
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.datastore.preferences)
    implementation(libs.coroutines.android)
}
```

### Step 9: Generate Gradle wrapper

Run from project root:
```bash
gradle wrapper --gradle-version 8.7
```

If `gradle` command is not available, download it or use the Gradle wrapper generator.

### Step 10: Verify

Run: `./gradlew projects`
Expected: Lists `:app`, `:core:tools-api`, `:core:ui`, `:core:data`

### Step 11: Commit

```bash
git add gradle/ gradlew gradlew.bat gradle.properties settings.gradle.kts build.gradle.kts gradle/libs.versions.toml app/build.gradle.kts core/
git commit -m "chore: project scaffolding with multi-module setup"
```

---

## Task 2: Core tools-api Interfaces

**Files to CREATE:**
- `core/tools-api/src/main/java/com/turnzero/core/tools/api/Tool.kt`
- `core/tools-api/src/main/java/com/turnzero/core/tools/api/Game.kt`
- `core/tools-api/src/main/java/com/turnzero/core/tools/api/ToolRegistry.kt`

### Implementation

**Tool.kt:**
```kotlin
package com.turnzero.core.tools.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

interface Tool {
    val id: String
    val name: String
    val icon: ImageVector
    val supportedGames: Set<String>

    @Composable
    fun Screen(onBack: () -> Unit)
}
```

**Game.kt:**
```kotlin
package com.turnzero.core.tools.api

interface Game {
    val id: String
    val name: String
    val description: String
    val imageRes: Int
    val toolIds: List<String>
}
```

**ToolRegistry.kt:**
```kotlin
package com.turnzero.core.tools.api

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToolRegistry @Inject constructor(
    private val games: Set<@JvmSuppressWildcards Game>,
    private val tools: Set<@JvmSuppressWildcards Tool>
) {
    fun getGames(): List<Game> = games.toList()

    fun getToolsForGame(gameId: String): List<Tool> =
        tools.filter { tool ->
            tool.supportedGames.contains("*") || tool.supportedGames.contains(gameId)
        }
}
```

### Commit
```bash
git add core/tools-api/
git commit -m "feat: tool registry interfaces (Tool, Game, ToolRegistry)"
```

---

## Task 3: Core Data — UserPreferences

**File to CREATE:** `core/data/src/main/java/com/turnzero/core/data/UserPreferences.kt`

**No tests needed** — this is a simple constants file + extension property.

### Implementation

```kotlin
package com.turnzero.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "turnzero_prefs")

object UserPreferences {
    val LAYOUT_GAMES = stringPreferencesKey("layout_games")
    val LAYOUT_TOOLS = stringPreferencesKey("layout_tools")
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val FAVORITE_GAMES = stringSetPreferencesKey("favorite_games")
    val FAVORITE_TOOLS = stringSetPreferencesKey("favorite_tools")
}
```

### Commit
```bash
git add core/data/
git commit -m "feat: user preferences DataStore keys and extension"
```

---

## Task 4: Core UI Theme

**Files to CREATE:**
- `core/ui/src/main/java/com/turnzero/core/ui/theme/Color.kt`
- `core/ui/src/main/java/com/turnzero/core/ui/theme/Type.kt`
- `core/ui/src/main/java/com/turnzero/core/ui/theme/Theme.kt`

### Implementation

**Color.kt:**
```kotlin
package com.turnzero.core.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val SurfaceDark = Color(0xFF1A1A2E)
val SurfaceContainerDark = Color(0xFF242442)
val OnSurfaceDark = Color(0xFFE6E6F0)

val SurfaceLight = Color(0xFFF5F5FA)
val SurfaceContainerLight = Color(0xFFE8E8F0)
val OnSurfaceLight = Color(0xFF1C1B2E)
```

**Type.kt:**
```kotlin
package com.turnzero.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val TZTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp
    )
)
```

**Theme.kt:**
```kotlin
package com.turnzero.core.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = SurfaceDark,
    surface = SurfaceDark,
    surfaceContainer = SurfaceContainerDark,
    onBackground = OnSurfaceDark,
    onSurface = OnSurfaceDark
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = SurfaceLight,
    surface = SurfaceLight,
    surfaceContainer = SurfaceContainerLight,
    onBackground = OnSurfaceLight,
    onSurface = OnSurfaceLight
)

@Composable
fun TZTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TZTypography,
        content = content
    )
}
```

### Commit
```bash
git add core/ui/
git commit -m "feat: Material 3 theme with dark/light support"
```

---

## Task 5: Core UI Components — Tiles, Layout Sheet, TopBar

**Files to CREATE:**
- `core/ui/src/main/java/com/turnzero/core/ui/components/GameTile.kt`
- `core/ui/src/main/java/com/turnzero/core/ui/components/ToolTile.kt`
- `core/ui/src/main/java/com/turnzero/core/ui/components/LayoutBottomSheet.kt`
- `core/ui/src/main/java/com/turnzero/core/ui/components/TopBar.kt`

### Implementation

**GameTile.kt:**
```kotlin
package com.turnzero.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.turnzero.core.tools.api.Game

enum class LayoutMode { COMPACT, COMFORTABLE, LIST }

@Composable
fun GameTile(
    game: Game,
    layoutMode: LayoutMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (layoutMode) {
        LayoutMode.LIST -> GameTileList(game, onClick, modifier)
        LayoutMode.COMPACT -> GameTileCompact(game, onClick, modifier)
        LayoutMode.COMFORTABLE -> GameTileComfortable(game, onClick, modifier)
    }
}

@Composable
private fun GameTileComfortable(game: Game, onClick: () -> Unit, modifier: Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = game.name.first().toString(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = game.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = game.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun GameTileCompact(game: Game, onClick: () -> Unit, modifier: Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Box(modifier = Modifier.padding(12.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = game.name.first().toString(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = game.name,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun GameTileList(game: Game, onClick: () -> Unit, modifier: Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = game.name.first().toString(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = game.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = game.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

**ToolTile.kt:**
```kotlin
package com.turnzero.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.turnzero.core.tools.api.Tool

@Composable
fun ToolTile(
    tool: Tool,
    layoutMode: LayoutMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (layoutMode) {
        LayoutMode.LIST -> ToolTileList(tool, onClick, modifier)
        LayoutMode.COMPACT -> ToolTileCompact(tool, onClick, modifier)
        LayoutMode.COMFORTABLE -> ToolTileComfortable(tool, onClick, modifier)
    }
}

@Composable
private fun ToolTileComfortable(tool: Tool, onClick: () -> Unit, modifier: Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = tool.icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = tool.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ToolTileCompact(tool: Tool, onClick: () -> Unit, modifier: Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Box(modifier = Modifier.padding(12.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = tool.name,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ToolTileList(tool: Tool, onClick: () -> Unit, modifier: Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = tool.icon,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = tool.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

**LayoutBottomSheet.kt:**
```kotlin
package com.turnzero.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

data class LayoutOption(
    val mode: LayoutMode,
    val label: String,
    val description: String
)

val layoutOptions = listOf(
    LayoutOption(LayoutMode.COMPACT, "Compact Grid", "Small tiles, more per row"),
    LayoutOption(LayoutMode.COMFORTABLE, "Comfortable Grid", "Larger tiles with details"),
    LayoutOption(LayoutMode.LIST, "List", "Full-width rows")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayoutBottomSheet(
    currentLayout: LayoutMode,
    onLayoutSelected: (LayoutMode) -> Unit,
    onDismiss: () -> Unit
) {
    var selected by remember { mutableStateOf(currentLayout) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Display Layout",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            layoutOptions.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selected == option.mode,
                            onClick = { selected = option.mode },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selected == option.mode,
                        onClick = null
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = option.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onLayoutSelected(selected) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Apply")
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
```

**TopBar.kt:**
```kotlin
package com.turnzero.core.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TZTopBar(
    title: String,
    showBackButton: Boolean = false,
    showLayoutButton: Boolean = false,
    onBackClick: () -> Unit = {},
    onLayoutClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = {
            if (showLayoutButton) {
                IconButton(onClick = onLayoutClick) {
                    Icon(
                        imageVector = Icons.Default.GridView,
                        contentDescription = "Change layout"
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
    )
}
```

### Commit
```bash
git add core/ui/
git commit -m "feat: tile composables, layout bottom sheet, and top bar"
```

---

## Task 6: Game & Tool Skeletons

**Files to CREATE:**
- `app/src/main/java/com/turnzero/games/yugioh/YuGiOhGame.kt`
- `app/src/main/java/com/turnzero/games/yugioh/YuGiOhLifeCounter.kt`
- `app/src/main/java/com/turnzero/games/yugioh/YuGiOhRulingsSearcher.kt`
- `app/src/main/java/com/turnzero/tools/dice/DiceTool.kt`

### Implementation

**YuGiOhGame.kt:**
```kotlin
package com.turnzero.games.yugioh

import com.turnzero.core.tools.api.Game

object YuGiOhGame : Game {
    override val id = "yugioh"
    override val name = "Yu-Gi-Oh!"
    override val description = "Trading Card Game"
    override val imageRes = android.R.drawable.ic_menu_gallery
    override val toolIds = listOf("dice", "ygo-life-counter", "ygo-rulings-searcher")
}
```

**YuGiOhLifeCounter.kt:**
```kotlin
package com.turnzero.games.yugioh

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.turnzero.core.tools.api.Tool
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YuGiOhLifeCounter @Inject constructor() : Tool {
    override val id = "ygo-life-counter"
    override val name = "Life Point Counter"
    override val icon = Icons.Default.Favorite
    override val supportedGames = setOf("yugioh")

    @Composable
    override fun Screen(onBack: () -> Unit) {
        Text(
            text = "Life Point Counter — Coming Soon",
            style = MaterialTheme.typography.titleMedium
        )
    }
}
```

**YuGiOhRulingsSearcher.kt:**
```kotlin
package com.turnzero.games.yugioh

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.turnzero.core.tools.api.Tool
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YuGiOhRulingsSearcher @Inject constructor() : Tool {
    override val id = "ygo-rulings-searcher"
    override val name = "Rulings Searcher"
    override val icon = Icons.Default.Search
    override val supportedGames = setOf("yugioh")

    @Composable
    override fun Screen(onBack: () -> Unit) {
        Text(
            text = "Rulings Searcher — Coming Soon",
            style = MaterialTheme.typography.titleMedium
        )
    }
}
```

**DiceTool.kt:**
```kotlin
package com.turnzero.tools.dice

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.turnzero.core.tools.api.Tool
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiceTool @Inject constructor() : Tool {
    override val id = "dice"
    override val name = "Dice Roller"
    override val icon = Icons.Default.Casino
    override val supportedGames = setOf("*")

    @Composable
    override fun Screen(onBack: () -> Unit) {
        Text(
            text = "Dice Roller — Coming Soon",
            style = MaterialTheme.typography.titleMedium
        )
    }
}
```

### Commit
```bash
git add app/
git commit -m "feat: game and tool skeletons (Yu-Gi-Oh, Dice, Life Counter, Rulings)"
```

---

## Task 7: DI Modules

**Files to CREATE:**
- `app/src/main/java/com/turnzero/di/ToolsModule.kt`
- `app/src/main/java/com/turnzero/di/GamesModule.kt`
- `app/src/main/java/com/turnzero/di/AppModule.kt`

### Implementation

**ToolsModule.kt:**
```kotlin
package com.turnzero.di

import com.turnzero.core.tools.api.Tool
import com.turnzero.games.yugioh.YuGiOhLifeCounter
import com.turnzero.games.yugioh.YuGiOhRulingsSearcher
import com.turnzero.tools.dice.DiceTool
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class ToolsModule {

    @Binds
    @IntoSet
    abstract fun bindDiceTool(dice: DiceTool): Tool

    @Binds
    @IntoSet
    abstract fun bindYuGiOhLifeCounter(lp: YuGiOhLifeCounter): Tool

    @Binds
    @IntoSet
    abstract fun bindYuGiOhRulingsSearcher(rs: YuGiOhRulingsSearcher): Tool
}
```

**GamesModule.kt:**
```kotlin
package com.turnzero.di

import com.turnzero.core.tools.api.Game
import com.turnzero.games.yugioh.YuGiOhGame
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
object GamesModule {

    @Provides
    @IntoSet
    fun provideYuGiOhGame(): Game = YuGiOhGame
}
```

**AppModule.kt:**
```kotlin
package com.turnzero.di

import android.content.Context
import com.turnzero.core.data.dataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context) = context.dataStore
}
```

### Commit
```bash
git add app/src/main/java/com/turnzero/di/
git commit -m "feat: Hilt DI modules for tools, games, and DataStore"
```

---

## Task 8: UI Screens

**Files to CREATE:**
- `app/src/main/java/com/turnzero/ui/games/GamesScreen.kt`
- `app/src/main/java/com/turnzero/ui/tools/ToolsScreen.kt`
- `app/src/main/java/com/turnzero/ui/tools/ToolSwitcherSheet.kt`
- `app/src/main/java/com/turnzero/ui/favorites/FavoritesScreen.kt`
- `app/src/main/java/com/turnzero/ui/settings/SettingsScreen.kt`

### Implementation

**GamesScreen.kt:**
```kotlin
package com.turnzero.ui.games

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.turnzero.core.tools.api.ToolRegistry
import com.turnzero.core.ui.components.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(
    toolRegistry: ToolRegistry,
    currentLayout: LayoutMode,
    onLayoutSelected: (LayoutMode) -> Unit,
    onGameClick: (String) -> Unit
) {
    var showLayoutSheet by remember { mutableStateOf(false) }
    val games = remember { toolRegistry.getGames() }

    Scaffold(
        topBar = {
            TZTopBar(
                title = "TurnZero",
                showLayoutButton = true,
                onLayoutClick = { showLayoutSheet = true }
            )
        }
    ) { padding ->
        when (currentLayout) {
            LayoutMode.LIST -> {
                LazyColumn(
                    contentPadding = PaddingValues(padding)
                        .plus(PaddingValues(horizontal = 16.dp, vertical = 8.dp)),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(games, key = { it.id }) { game ->
                        GameTile(
                            game = game,
                            layoutMode = currentLayout,
                            onClick = { onGameClick(game.id) }
                        )
                    }
                }
            }
            else -> {
                val columns = if (currentLayout == LayoutMode.COMPACT) 3 else 2
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    contentPadding = PaddingValues(padding)
                        .plus(PaddingValues(horizontal = 16.dp, vertical = 8.dp)),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(games, key = { it.id }) { game ->
                        GameTile(
                            game = game,
                            layoutMode = currentLayout,
                            onClick = { onGameClick(game.id) }
                        )
                    }
                }
            }
        }
    }

    if (showLayoutSheet) {
        LayoutBottomSheet(
            currentLayout = currentLayout,
            onLayoutSelected = {
                onLayoutSelected(it)
                showLayoutSheet = false
            },
            onDismiss = { showLayoutSheet = false }
        )
    }
}
```

**ToolsScreen.kt:**
```kotlin
package com.turnzero.ui.tools

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.turnzero.core.tools.api.ToolRegistry
import com.turnzero.core.ui.components.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    toolRegistry: ToolRegistry,
    gameId: String,
    currentLayout: LayoutMode,
    onLayoutSelected: (LayoutMode) -> Unit,
    onToolClick: (String) -> Unit,
    onBack: () -> Unit
) {
    var showLayoutSheet by remember { mutableStateOf(false) }
    val tools = remember(gameId) { toolRegistry.getToolsForGame(gameId) }
    val gameName = remember(gameId) {
        toolRegistry.getGames().firstOrNull { it.id == gameId }?.name ?: gameId
    }

    Scaffold(
        topBar = {
            TZTopBar(
                title = gameName,
                showBackButton = true,
                showLayoutButton = true,
                onBackClick = onBack,
                onLayoutClick = { showLayoutSheet = true }
            )
        }
    ) { padding ->
        when (currentLayout) {
            LayoutMode.LIST -> {
                LazyColumn(
                    contentPadding = PaddingValues(padding)
                        .plus(PaddingValues(horizontal = 16.dp, vertical = 8.dp)),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tools, key = { it.id }) { tool ->
                        ToolTile(
                            tool = tool,
                            layoutMode = currentLayout,
                            onClick = { onToolClick(tool.id) }
                        )
                    }
                }
            }
            else -> {
                val columns = if (currentLayout == LayoutMode.COMPACT) 3 else 2
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    contentPadding = PaddingValues(padding)
                        .plus(PaddingValues(horizontal = 16.dp, vertical = 8.dp)),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tools, key = { it.id }) { tool ->
                        ToolTile(
                            tool = tool,
                            layoutMode = currentLayout,
                            onClick = { onToolClick(tool.id) }
                        )
                    }
                }
            }
        }
    }

    if (showLayoutSheet) {
        LayoutBottomSheet(
            currentLayout = currentLayout,
            onLayoutSelected = {
                onLayoutSelected(it)
                showLayoutSheet = false
            },
            onDismiss = { showLayoutSheet = false }
        )
    }
}
```

**ToolSwitcherSheet.kt:**
```kotlin
package com.turnzero.ui.tools

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.turnzero.core.tools.api.Tool

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolSwitcherSheet(
    tools: List<Tool>,
    currentToolId: String,
    onToolSelected: (Tool) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Switch Tool",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            tools.forEach { tool ->
                val isCurrent = tool.id == currentToolId
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable(enabled = !isCurrent) {
                            if (!isCurrent) onToolSelected(tool)
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrent)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = tool.icon,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = if (isCurrent)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(
                            text = tool.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isCurrent)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                        if (isCurrent) {
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = "Current",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun ToolSwitcherFAB(
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onTap,
        modifier = modifier.padding(16.dp),
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ) {
        Icon(
            imageVector = Icons.Default.SwapHoriz,
            contentDescription = "Switch Tool",
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
```

**FavoritesScreen.kt:**
```kotlin
package com.turnzero.ui.favorites

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.turnzero.core.ui.components.TZTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen() {
    Scaffold(
        topBar = {
            TZTopBar(title = "Favorites")
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No favorites yet",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Tap the star icon on any game or tool\nto add it here for quick access",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
```

**SettingsScreen.kt:**
```kotlin
package com.turnzero.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.turnzero.core.ui.components.TZTopBar

enum class ThemeMode { SYSTEM, LIGHT, DARK }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit
) {
    Scaffold(
        topBar = {
            TZTopBar(title = "Settings")
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ThemeOption("Follow System", ThemeMode.SYSTEM, currentTheme, onThemeSelected)
            ThemeOption("Light", ThemeMode.LIGHT, currentTheme, onThemeSelected)
            ThemeOption("Dark", ThemeMode.DARK, currentTheme, onThemeSelected)

            Spacer(Modifier.height(32.dp))

            Text(
                text = "About",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TurnZero v0.1.0",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Your all-in-one boardgame companion",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeOption(
    label: String,
    mode: ThemeMode,
    current: ThemeMode,
    onSelect: (ThemeMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = current == mode,
            onClick = { onSelect(mode) }
        )
        Spacer(Modifier.width(12.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}
```

### Commit
```bash
git add app/src/main/java/com/turnzero/ui/
git commit -m "feat: screen composables (games, tools, favorites, settings) with FAB tool switcher"
```

---

## Task 9: Navigation

**Files to CREATE:**
- `app/src/main/java/com/turnzero/navigation/Screen.kt`
- `app/src/main/java/com/turnzero/navigation/BottomNavBar.kt`
- `app/src/main/java/com/turnzero/navigation/TZNavHost.kt`

### Implementation

**Screen.kt:**
```kotlin
package com.turnzero.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Games
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Games : Screen("games")
    data object Favorites : Screen("favorites")
    data object Settings : Screen("settings")

    data object Tools : Screen("tools/{gameId}") {
        fun createRoute(gameId: String) = "tools/$gameId"
    }

    data object ToolDetail : Screen("tool/{gameId}/{toolId}") {
        fun createRoute(gameId: String, toolId: String) = "tool/$gameId/$toolId"
    }
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        screen = Screen.Games,
        label = "Games",
        selectedIcon = Icons.Filled.Games,
        unselectedIcon = Icons.Outlined.Games
    ),
    BottomNavItem(
        screen = Screen.Favorites,
        label = "Favorites",
        selectedIcon = Icons.Filled.Favorite,
        unselectedIcon = Icons.Outlined.FavoriteBorder
    ),
    BottomNavItem(
        screen = Screen.Settings,
        label = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
)
```

**BottomNavBar.kt:**
```kotlin
package com.turnzero.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun TZBottomNavBar(
    currentRoute: String?,
    onItemClick: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.screen.route
            NavigationBarItem(
                selected = selected,
                onClick = { onItemClick(item.screen) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = { Text(text = item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
```

**TZNavHost.kt:**
```kotlin
package com.turnzero.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.turnzero.core.data.UserPreferences
import com.turnzero.core.tools.api.ToolRegistry
import com.turnzero.core.ui.components.*
import com.turnzero.ui.favorites.FavoritesScreen
import com.turnzero.ui.games.GamesScreen
import com.turnzero.ui.settings.SettingsScreen
import com.turnzero.ui.settings.ThemeMode
import com.turnzero.ui.tools.ToolSwitcherFAB
import com.turnzero.ui.tools.ToolSwitcherSheet
import com.turnzero.ui.tools.ToolsScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Composable
fun TZNavHost(
    toolRegistry: ToolRegistry,
    dataStore: DataStore<Preferences>,
    isDarkTheme: Boolean,
    onThemeChanged: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val scope = rememberCoroutineScope()

    val gamesLayout = remember {
        runBlocking {
            dataStore.data.first()[UserPreferences.LAYOUT_GAMES]?.let {
                try { LayoutMode.valueOf(it) } catch (_: Exception) { LayoutMode.COMFORTABLE }
            } ?: LayoutMode.COMFORTABLE
        }
    }

    var gamesLayoutState by remember { mutableStateOf(gamesLayout) }

    val toolsLayout = remember {
        runBlocking {
            dataStore.data.first()[UserPreferences.LAYOUT_TOOLS]?.let {
                try { LayoutMode.valueOf(it) } catch (_: Exception) { LayoutMode.COMFORTABLE }
            } ?: LayoutMode.COMFORTABLE
        }
    }

    var toolsLayoutState by remember { mutableStateOf(toolsLayout) }

    val themeMode = remember {
        runBlocking {
            dataStore.data.first()[UserPreferences.THEME_MODE] ?: "system"
        }
    }

    var themeModeState by remember {
        mutableStateOf(
            try { ThemeMode.valueOf(themeMode.uppercase()) } catch (_: Exception) { ThemeMode.SYSTEM }
        )
    }

    Scaffold(
        bottomBar = {
            TZBottomNavBar(
                currentRoute = currentRoute,
                onItemClick = { screen ->
                    navController.navigate(screen.route) {
                        popUpTo(Screen.Games.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Games.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Games.route) {
                GamesScreen(
                    toolRegistry = toolRegistry,
                    currentLayout = gamesLayoutState,
                    onLayoutSelected = { mode ->
                        gamesLayoutState = mode
                        scope.launch {
                            dataStore.edit { prefs ->
                                prefs[UserPreferences.LAYOUT_GAMES] = mode.name
                            }
                        }
                    },
                    onGameClick = { gameId ->
                        navController.navigate(Screen.Tools.createRoute(gameId))
                    }
                )
            }

            composable(Screen.Favorites.route) {
                FavoritesScreen()
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    currentTheme = themeModeState,
                    onThemeSelected = { mode ->
                        themeModeState = mode
                        scope.launch {
                            dataStore.edit { prefs ->
                                prefs[UserPreferences.THEME_MODE] = mode.name.lowercase()
                            }
                        }
                        onThemeChanged(
                            when (mode) {
                                ThemeMode.LIGHT -> false
                                ThemeMode.DARK -> true
                                ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
                            }
                        )
                    }
                )
            }

            composable(
                route = Screen.Tools.route,
                arguments = listOf(navArgument("gameId") { type = NavType.StringType })
            ) { backStackEntry ->
                val gameId = backStackEntry.arguments?.getString("gameId") ?: return@composable
                ToolsScreen(
                    toolRegistry = toolRegistry,
                    gameId = gameId,
                    currentLayout = toolsLayoutState,
                    onLayoutSelected = { mode ->
                        toolsLayoutState = mode
                        scope.launch {
                            dataStore.edit { prefs ->
                                prefs[UserPreferences.LAYOUT_TOOLS] = mode.name
                            }
                        }
                    },
                    onToolClick = { toolId ->
                        navController.navigate(Screen.ToolDetail.createRoute(gameId, toolId))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.ToolDetail.route,
                arguments = listOf(
                    navArgument("gameId") { type = NavType.StringType },
                    navArgument("toolId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val gameId = backStackEntry.arguments?.getString("gameId") ?: return@composable
                val toolId = backStackEntry.arguments?.getString("toolId") ?: return@composable
                val allTools = remember(gameId) { toolRegistry.getToolsForGame(gameId) }
                val tool = allTools.firstOrNull { it.id == toolId } ?: return@composable

                var showSwitcher by remember { mutableStateOf(false) }

                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        topBar = {
                            TZTopBar(
                                title = tool.name,
                                showBackButton = true,
                                onBackClick = { navController.popBackStack() }
                            )
                        },
                        floatingActionButton = {
                            ToolSwitcherFAB(
                                onTap = { showSwitcher = true }
                            )
                        }
                    ) { contentPadding ->
                        Box(modifier = Modifier.padding(contentPadding)) {
                            tool.Screen(onBack = { navController.popBackStack() })
                        }
                    }
                }

                if (showSwitcher) {
                    ToolSwitcherSheet(
                        tools = allTools,
                        currentToolId = toolId,
                        onToolSelected = { selected ->
                            showSwitcher = false
                            navController.navigate(
                                Screen.ToolDetail.createRoute(gameId, selected.id)
                            ) {
                                popUpTo(Screen.Tools.createRoute(gameId)) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        onDismiss = { showSwitcher = false }
                    )
                }
            }
        }
    }
}
```

### Commit
```bash
git add app/src/main/java/com/turnzero/navigation/
git commit -m "feat: navigation host, bottom nav bar, and screen routes"
```

---

## Task 10: App Entry Point

**Files to CREATE:**
- `app/src/main/java/com/turnzero/TZApplication.kt`
- `app/src/main/java/com/turnzero/MainActivity.kt`

### Implementation

**TZApplication.kt:**
```kotlin
package com.turnzero

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TZApplication : Application()
```

**MainActivity.kt:**
```kotlin
package com.turnzero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.turnzero.core.tools.api.ToolRegistry
import com.turnzero.core.ui.theme.TZTheme
import com.turnzero.navigation.TZNavHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var toolRegistry: ToolRegistry

    @Inject
    lateinit var dataStore: DataStore<Preferences>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var isDark by remember { mutableStateOf(isSystemInDarkTheme()) }

            TZTheme(darkTheme = isDark) {
                TZNavHost(
                    toolRegistry = toolRegistry,
                    dataStore = dataStore,
                    isDarkTheme = isDark,
                    onThemeChanged = { dark -> isDark = dark }
                )
            }
        }
    }
}
```

### Commit
```bash
git add app/src/main/java/com/turnzero/TZApplication.kt app/src/main/java/com/turnzero/MainActivity.kt
git commit -m "feat: application entry point with Hilt and Compose theme"
```

---

## Task 11: Android Manifest, Resources & Security Config

**Files to CREATE:**
- `app/src/main/AndroidManifest.xml`
- `app/src/main/res/values/themes.xml`
- `app/src/main/res/xml/network_security_config.xml`
- `app/proguard-rules.pro`

### Implementation

**AndroidManifest.xml:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application
        android:name=".TZApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="TurnZero"
        android:networkSecurityConfig="@xml/network_security_config"
        android:supportsRtl="true"
        android:theme="@style/Theme.TurnZero">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.TurnZero">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

**res/values/themes.xml:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.TurnZero" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
```

**res/xml/network_security_config.xml:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
</network-security-config>
```

**proguard-rules.pro:**
```
# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# DataStore
-keep class androidx.datastore.** { *; }
```

### Commit
```bash
git add app/src/main/AndroidManifest.xml app/src/main/res/ app/proguard-rules.pro
git commit -m "feat: manifest, theme XML, security config, and proguard rules"
```

---

## Task 12: Build Verification

Verify the project compiles.

### Steps

1. Run: `./gradlew assembleDebug`
2. Expected: BUILD SUCCESSFUL, APK at `app/build/outputs/apk/debug/app-debug.apk`
3. If any compilation errors, fix them and retry
4. Run: `./gradlew lint`
5. Expected: no errors (warnings acceptable)

### Commit
```bash
git add -A
git commit -m "chore: final wiring - project compiles successfully"
```

---
