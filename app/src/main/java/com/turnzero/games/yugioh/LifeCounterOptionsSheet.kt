package com.turnzero.games.yugioh

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifeCounterOptionsSheet(
    timerMode: TimerMode,
    timerStartValueMinutes: Int,
    startingLp: Int,
    onSetTimerMode: (TimerMode) -> Unit,
    onSetTimerStartValue: (Int) -> Unit,
    onSetStartingLp: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.6f),
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Timer Mode",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TimerMode.entries.forEach { mode ->
                    val label = when (mode) {
                        TimerMode.COUNT_UP -> "Count Up"
                        TimerMode.COUNT_DOWN -> "Count Down"
                    }
                    FilterChip(
                        selected = timerMode == mode,
                        onClick = { onSetTimerMode(mode) },
                        label = { Text(label, fontSize = 14.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            if (timerMode == TimerMode.COUNT_DOWN) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Duration: ${timerStartValueMinutes} min",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(Modifier.height(4.dp))
                val sliderSteps = listOf(30, 35, 40, 45, 50, 55, 60)
                val sliderPosition = sliderSteps.indexOf(timerStartValueMinutes).coerceAtLeast(0)
                Slider(
                    value = sliderPosition.toFloat(),
                    onValueChange = { pos ->
                        val idx = pos.roundToInt().coerceIn(0, sliderSteps.lastIndex)
                        onSetTimerStartValue(sliderSteps[idx])
                    },
                    valueRange = 0f..(sliderSteps.lastIndex).toFloat(),
                    steps = sliderSteps.lastIndex - 1,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Text(
                text = "Starting LP",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(8000, 4000).forEach { lp ->
                    FilterChip(
                        selected = startingLp == lp,
                        onClick = { onSetStartingLp(lp) },
                        label = { Text(lp.toString(), fontSize = 14.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
