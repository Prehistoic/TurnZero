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
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

private val REPEAT_INITIAL_DELAY = 500L
private val REPEAT_INTERVAL = 100L
private val INDICATOR_FADE_DELAY = 2500

@Composable
fun HorizontalPlayerBox(
    player: LpPlayer,
    isUpsideDown: Boolean,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRelease: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lastChangeIsVisible = remember { mutableStateOf(false) }
    val changeValue = remember { mutableIntStateOf(0) }

    LaunchedEffect(player.lastChange) {
        if (player.lastChange != 0) {
            changeValue.intValue = player.lastChange
            lastChangeIsVisible.value = true
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(if (isUpsideDown) -1f else 1f, if (isUpsideDown) -1f else 1f),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp),
        ) {
            AnimatedVisibility(
                visible = lastChangeIsVisible.value,
                exit = fadeOut(animationSpec = tween(delayMillis = INDICATOR_FADE_DELAY)),
            ) {
                val sign = if (changeValue.intValue > 0) "+" else ""
                Text(
                    text = "$sign${changeValue.intValue}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (changeValue.intValue >= 0) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error,
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                RepeatButton(
                    onPress = onDecrement,
                    onRelease = onRelease,
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease LP")
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "${player.currentLp}",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.width(16.dp))

                RepeatButton(
                    onPress = onIncrement,
                    onRelease = onRelease,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase LP")
                }
            }

            IconButton(onClick = onReset) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = "Reset LP",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
fun VerticalPlayerBox(
    player: LpPlayer,
    isUpsideDown: Boolean,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRelease: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .scale(if (isUpsideDown) -1f else 1f, if (isUpsideDown) -1f else 1f),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
        ) {
            RepeatButton(
                onPress = onIncrement,
                onRelease = onRelease,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase LP")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${player.currentLp}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.rotate(90f),
            )

            Spacer(modifier = Modifier.height(8.dp))

            RepeatButton(
                onPress = onDecrement,
                onRelease = onRelease,
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease LP")
            }

            Spacer(modifier = Modifier.height(4.dp))

            IconButton(
                onClick = onReset,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = "Reset LP",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun RepeatButton(
    onPress: () -> Unit,
    onRelease: () -> Unit,
    content: @Composable () -> Unit,
) {
    var isPressed by remember { mutableStateOf(false) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            onPress()
            delay(REPEAT_INITIAL_DELAY)
            while (isPressed) {
                onPress()
                delay(REPEAT_INTERVAL)
            }
        } else {
            onRelease()
        }
    }

    IconButton(
        onClick = { },
        modifier = Modifier
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when (event.type) {
                            PointerEventType.Press -> {
                                isPressed = true
                            }
                            PointerEventType.Release -> {
                                isPressed = false
                            }
                            else -> {}
                        }
                    }
                }
            },
    ) {
        content()
    }
}
