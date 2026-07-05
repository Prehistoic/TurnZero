package com.turnzero.tools.dice

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun DiceAnimation(
    dice: List<Die>,
    results: List<Int>?,
    isRolling: Boolean,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier,
    dieSize: Dp,
    onRoll: () -> Unit = {},
    triggerScale: Int = 0
) {
    val hasRolled = results != null

    var displayedNumbers by remember(dice.size) { mutableStateOf(List(dice.size) { 1 }) }

    LaunchedEffect(isRolling, results) {
        if (isRolling && results != null) {
            val intervalMs = 80L
            val totalFrames = 12
            var previousNumbers = List(dice.size) { -1 }

            repeat(totalFrames) {
                val current = dice.mapIndexed { i, die ->
                    randomNoRepeat(die.faceCount, previousNumbers[i])
                }
                displayedNumbers = current
                previousNumbers = current
                delay(intervalMs)
            }

            displayedNumbers = results
            onAnimationComplete()
        } else if (!isRolling && results != null && displayedNumbers != results) {
            displayedNumbers = results
        }
    }

    // Tap scale animation
    var targetScale by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(triggerScale) {
        if (triggerScale > 0) {
            targetScale = 1.08f
            delay(100)
            targetScale = 1f
            delay(80)
            onRoll()
        }
    }

    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f),
        label = "tapScale"
    )

    val rows = remember(dice) { dice.chunked(4) }

    Column(
        modifier = modifier.scale(animatedScale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        rows.forEachIndexed { rowIndex, rowDice ->
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                rowDice.forEachIndexed { colIndex, die ->
                    val index = rowIndex * 4 + colIndex
                    val number = if (isRolling || hasRolled) displayedNumbers.getOrElse(index) { 1 } else 0

                    DieCard(
                        die = die,
                        displayNumber = if (hasRolled || isRolling) number else null,
                        modifier = Modifier.padding(4.dp),
                        dieSize = dieSize
                    )
                }
            }
        }
    }
}

@Composable
fun DieCard(
    die: Die,
    displayNumber: Int?,
    modifier: Modifier = Modifier,
    dieSize: Dp
) {
    val dieColor = Color(die.color.hex)
    val isWhite = die.color == DiceColor.WHITE
    val isBlack = die.color == DiceColor.BLACK
    val faceLabel = "d${die.faceCount}"

    val borderColor = when {
        isWhite -> Color.Black
        isBlack -> Color.White
        else -> dieColor
    }
    val bgColor = when {
        isWhite -> Color.White
        isBlack -> Color.Black
        else -> dieColor.copy(alpha = 0.12f)
    }

    val borderWidth = (dieSize.value / 32f).dp
    val innerPadding = (dieSize.value / 16f).dp
    val labelFontSize = (dieSize.value * 0.14f).sp
    val numberFontSize = (dieSize.value * 0.38f).sp
    val gap = (dieSize.value / 32f).dp

    Column(
        modifier = modifier
            .size(dieSize)
            .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(innerPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = faceLabel,
            style = MaterialTheme.typography.labelSmall,
            color = borderColor,
            fontSize = labelFontSize
        )
        Spacer(modifier = Modifier.height(gap))
        Text(
            text = if (displayNumber != null) displayNumber.toString() else faceLabel,
            fontWeight = FontWeight.Bold,
            color = borderColor,
            fontSize = numberFontSize,
            textAlign = TextAlign.Center
        )
    }
}

private fun randomNoRepeat(faceCount: Int, previous: Int): Int {
    if (faceCount <= 1) return 1
    var next = Random.nextInt(1, faceCount + 1)
    while (next == previous) {
        next = Random.nextInt(1, faceCount + 1)
    }
    return next
}
