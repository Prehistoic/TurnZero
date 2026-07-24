package com.turnzero.tools.lifecounter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

private val REPEAT_INITIAL_DELAY = 500L
private val REPEAT_INTERVAL = 100L
private val INDICATOR_HOLD_DELAY = 2500
private val FADE_DURATION = 300

val PlayerColors = listOf(
    Color(0xFF4CAF50), // P1 - Green
    Color(0xFFF44336), // P2 - Red
    Color(0xFF2196F3), // P3 - Blue
    Color(0xFFFF9800), // P4 - Orange
)

@Composable
fun PlayerBox(
    player: LpPlayer,
    isUpsideDown: Boolean,
    playerColor: Color,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onReset: () -> Unit,
    onResetChangeIndicator: () -> Unit,
    modifier: Modifier = Modifier,
    verticalMode: Boolean = false,
) {
    val lastChangeIsVisible = remember { mutableStateOf(false) }
    val changeDisplayValue = remember { mutableIntStateOf(0) }
    var isLeftPressed by remember { mutableStateOf(false) }
    var isRightPressed by remember { mutableStateOf(false) }

    // Show indicator when lastChange is non-zero, accumulate, and hide after delay
    LaunchedEffect(player.lastChange) {
        if (player.lastChange != 0) {
            changeDisplayValue.intValue = player.lastChange
            lastChangeIsVisible.value = true
            delay(INDICATOR_HOLD_DELAY.toLong())
            lastChangeIsVisible.value = false
            delay(FADE_DURATION.toLong() + 50)
            onResetChangeIndicator()
        }
    }

    LaunchedEffect(isLeftPressed) {
        if (isLeftPressed) {
            onDecrement()
            delay(REPEAT_INITIAL_DELAY)
            while (isLeftPressed) {
                onDecrement()
                delay(REPEAT_INTERVAL)
            }
        }
    }

    LaunchedEffect(isRightPressed) {
        if (isRightPressed) {
            onIncrement()
            delay(REPEAT_INITIAL_DELAY)
            while (isRightPressed) {
                onIncrement()
                delay(REPEAT_INTERVAL)
            }
        }
    }

    val scaleModifier = if (isUpsideDown)
        Modifier.scale(scaleX = -1f, scaleY = -1f)
    else
        Modifier

    Surface(
        modifier = modifier then scaleModifier then
            if (verticalMode) Modifier.rotate(90f) else Modifier,
        color = playerColor.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.Center),
            ) {
                AnimatedVisibility(
                    visible = lastChangeIsVisible.value,
                    exit = fadeOut(animationSpec = tween()),
                ) {
                    val sign = if (changeDisplayValue.intValue > 0) "+" else ""
                    Text(
                        text = "$sign${changeDisplayValue.intValue}",
                        style = if (verticalMode) MaterialTheme.typography.titleMedium
                                else MaterialTheme.typography.titleLarge,
                        color = if (changeDisplayValue.intValue >= 0) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error,
                    )
                }

                Text(
                    text = "${player.currentLp}",
                    style = if (verticalMode) MaterialTheme.typography.headlineLarge
                            else MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = playerColor,
                )

                IconButton(
                    onClick = onReset,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = "Reset LP",
                        tint = playerColor,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            // Left touch zone: decrement
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.35f)
                    .align(Alignment.CenterStart)
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                when (event.type) {
                                    PointerEventType.Press -> isLeftPressed = true
                                    PointerEventType.Release -> isLeftPressed = false
                                    else -> {}
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Remove,
                    contentDescription = "Decrease LP",
                    tint = playerColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(32.dp),
                )
            }

            // Right touch zone: increment
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.35f)
                    .align(Alignment.CenterEnd)
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                when (event.type) {
                                    PointerEventType.Press -> isRightPressed = true
                                    PointerEventType.Release -> isRightPressed = false
                                    else -> {}
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Increase LP",
                    tint = playerColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(32.dp),
                )
            }
        }
    }
}
