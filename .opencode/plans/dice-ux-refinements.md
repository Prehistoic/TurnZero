# Dice Tool — UX Refinements Implementation Plan

## Max 20 dice, dynamic sizing, tap-to-roll, compact history, navbar consistency

### Change 1: DiceViewModel.kt — Cap at 20 dice

**File:** `app/src/main/java/com/turnzero/tools/dice/DiceViewModel.kt:47-48`

Replace:
```kotlin
            val newDice = (1..delta).map {
```
With:
```kotlin
            val clampedDelta = minOf(delta, 20 - current.size)
            if (clampedDelta <= 0) return
            val newDice = (1..clampedDelta).map {
```

---

### Change 2: DiceAnimation.kt — Dynamic die size

**File:** `app/src/main/java/com/turnzero/tools/dice/DiceAnimation.kt`

Add `dieSize: Dp = 72.dp` parameter to both `DiceAnimation` and `DieCard`:

**DiceAnimation signature** (line 23):
```kotlin
fun DiceAnimation(
    dice: List<Die>,
    results: List<Int>?,
    isRolling: Boolean,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier,
    dieSize: androidx.compose.ui.unit.Dp = 72.dp
)
```

**DieCard signature** (line 83):
```kotlin
fun DieCard(
    die: Die,
    displayNumber: Int?,
    modifier: Modifier = Modifier,
    dieSize: androidx.compose.ui.unit.Dp = 72.dp
)
```

**Add import:** `import androidx.compose.ui.unit.Dp`

**DieCard body** — replace fixed values with proportional ones:
- `.size(72.dp)` → `.size(dieSize)`
- `.border(2.dp, ...)` → `.border(dieSize / 36, ...)`  (gives 2dp at 72dp, 1.1dp at 40dp)
- `.padding(4.dp)` → `.padding(dieSize / 18)`  (gives 4dp at 72dp, 2.2dp at 40dp)
- `fontSize = 10.sp` → `fontSize = (dieSize.value * 0.14f).sp`  (gives 10 at 72dp, 5.6 at 40dp)
- `headlineMedium` number text: remove fixed `style` ref, keep `fontWeight = Bold`, set `fontSize = (dieSize.value * 0.4f).sp` (gives ~29 at 72dp, 16 at 40dp)
- Face label `Spacer(height = 2.dp)` → `Spacer(height = dieSize / 36)`

---

### Change 3: DiceScreen.kt — Remove button, tap to roll, total at bottom

**File:** `app/src/main/java/com/turnzero/tools/dice/DiceScreen.kt`

Replace entire file:

```kotlin
package com.turnzero.tools.dice

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DiceScreen(
    uiState: DiceUiState,
    onChangeDieCount: (Int, DiceColor, Int) -> Unit,
    onSetSelectedColor: (DiceColor) -> Unit,
    onRoll: () -> Unit,
    onAnimationComplete: () -> Unit,
    onLoadPreset: (DicePreset) -> Unit,
    onClearHistory: () -> Unit,
    onToggleHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canRoll = uiState.dice.isNotEmpty() && !uiState.isRolling
    val dieSize = when {
        uiState.dice.size <= 4 -> 80.dp
        uiState.dice.size <= 8 -> 64.dp
        uiState.dice.size <= 12 -> 56.dp
        uiState.dice.size <= 16 -> 48.dp
        else -> 40.dp
    }

    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clickable(enabled = canRoll) { onRoll() },
            contentAlignment = Alignment.Center
        ) {
            DiceAnimation(
                dice = uiState.dice,
                results = uiState.results,
                isRolling = uiState.isRolling,
                onAnimationComplete = onAnimationComplete,
                dieSize = dieSize
            )
        }

        if (uiState.total != null && !uiState.isRolling) {
            Text(
                text = "Total: ${uiState.total}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 16.dp)
            )
        }
    }

    if (uiState.showHistory) {
        DiceHistorySheet(
            history = uiState.history,
            onClear = onClearHistory,
            onDismiss = onToggleHistory
        )
    }
}
```

---

### Change 4: DiceHistorySheet.kt — Single-row compact logs

**File:** `app/src/main/java/com/turnzero/tools/dice/DiceHistorySheet.kt`

Replace the `RollEntry` composable (lines 68-117) with:

```kotlin
@Composable
private fun RollEntry(roll: DiceRoll, index: Int) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeStr = timeFormat.format(Date(roll.timestamp))
    val diceSummary = roll.dice.groupBy { it.faceCount }
        .map { (fc, list) -> "${list.size}d$fc" }
        .joinToString(" + ")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#$index  $diceSummary",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${roll.total}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = timeStr,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
```

Remove unused imports: `Color`, `Surface` (if only used in old RollEntry). Remove `itemsIndexed` import, add `items` or keep `itemsIndexed` since it's still used in the LazyColumn.

---

### Change 5: ToolBottomNavBar.kt — Match TZBottomNavBar colors

**File:** `app/src/main/java/com/turnzero/navigation/ToolBottomNavBar.kt`

Add `colors` parameter to both `NavigationBarItem` calls:

```kotlin
@Composable
fun ToolBottomNavBar(
    onToolsClick: () -> Unit,
    onOptionsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        NavigationBarItem(
            selected = false,
            onClick = onToolsClick,
            icon = {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Tools"
                )
            },
            label = { Text("Tools") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = onOptionsClick,
            icon = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Options"
                )
            },
            label = { Text("Options") },
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
```

---

### Change 6: DiceViewModelTest.kt — Test 20-dice cap

**File:** `app/src/test/java/com/turnzero/tools/dice/DiceViewModelTest.kt`

Add test after the `changeDieCount does not go below zero` test:

```kotlin
    @Test
    fun `changeDieCount caps at 20 dice maximum`() = runTest {
        val vm = DiceViewModel(repo, savedStateHandle)
        advanceUntilIdle()
        // Add 15 d6
        vm.changeDieCount(6, DiceColor.RED, 15)
        assertEquals(15, vm.uiState.value.dice.size)
        // Try to add 10 more — should cap at 20
        vm.changeDieCount(6, DiceColor.RED, 10)
        assertEquals(20, vm.uiState.value.dice.size)
        // Try to add more — should stay at 20
        vm.changeDieCount(20, DiceColor.BLUE, 5)
        assertEquals(20, vm.uiState.value.dice.size)
    }
```

---

### Build verification

```bash
export ANDROID_HOME=/mnt/c/Users/matla/AppData/Local/Android/Sdk
cd /mnt/c/Users/matla/dev/TurnZero
./gradlew assembleDebug testDebugUnitTest lint
```

Expected: BUILD SUCCESSFUL, all tests pass, lint clean.

---

### Commit

```bash
git add -A
git commit -m "feat: max 20 dice, dynamic sizing, tap-to-roll, compact history, navbar style match"
```
