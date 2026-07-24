package com.turnzero.tools.lifecounter

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

    LaunchedEffect(player.lastChange) {
        if (player.lastChange != 0) {
            changeDisplayValue.intValue = player.lastChange
            lastChangeIsVisible.value = true
            delay(INDICATOR_HOLD_DELAY.toLong())
            lastChangeIsVisible.value = false
            delay(FADE_DURATION.toLong() + 50)
            onResetChangeIndicator()
        } else {
            lastChangeIsVisible.value = false
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
        modifier = modifier then scaleModifier,
        color = playerColor.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
    ) {
        HorizontalContent(
            player = player,
            playerColor = playerColor,
            lastChangeIsVisible = lastChangeIsVisible.value,
            changeDisplayValue = changeDisplayValue.intValue,
            onReset = onReset,
            onLeftPress = { isLeftPressed = it },
            onRightPress = { isRightPressed = it },
            compact = verticalMode,
        )
    }
}

@Composable
private fun HorizontalContent(
    player: LpPlayer,
    playerColor: Color,
    lastChangeIsVisible: Boolean,
    changeDisplayValue: Int,
    onReset: () -> Unit,
    onLeftPress: (Boolean) -> Unit,
    onRightPress: (Boolean) -> Unit,
    compact: Boolean = false,
) {
    val indicatorStyle = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge
    val lpStyle = if (compact) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.displayLarge
    val iconSize = if (compact) 24.dp else 32.dp
    val heartSize = if (compact) 28.dp else 36.dp
    val indicatorHeight = if (compact) 24.dp else 28.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(if (compact) Modifier.rotate(90f) else Modifier),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center),
        ) {
            Box(
                modifier = Modifier.height(indicatorHeight),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = lastChangeIsVisible,
                    exit = fadeOut(animationSpec = tween()),
                ) {
                    val sign = if (changeDisplayValue > 0) "+" else ""
                    Text(
                        text = "$sign$changeDisplayValue",
                        style = indicatorStyle,
                        color = if (changeDisplayValue >= 0) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error,
                    )
                }
            }

            Text(
                text = "${player.currentLp}",
                style = lpStyle,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = playerColor,
            )

            IconButton(
                onClick = onReset,
                modifier = Modifier.size(heartSize),
            ) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = "Reset LP",
                    tint = playerColor,
                    modifier = Modifier.size(if (compact) 16.dp else 20.dp),
                )
            }
        }

        // Left touch zone
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.35f)
                .align(Alignment.CenterStart)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            when (awaitPointerEvent().type) {
                                PointerEventType.Press -> onLeftPress(true)
                                PointerEventType.Release -> onLeftPress(false)
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
                modifier = Modifier.size(iconSize),
            )
        }

        // Right touch zone
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.35f)
                .align(Alignment.CenterEnd)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            when (awaitPointerEvent().type) {
                                PointerEventType.Press -> onRightPress(true)
                                PointerEventType.Release -> onRightPress(false)
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
                modifier = Modifier.size(iconSize),
            )
        }
    }
}
