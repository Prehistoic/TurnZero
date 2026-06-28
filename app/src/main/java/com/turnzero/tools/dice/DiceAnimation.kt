package com.turnzero.tools.dice

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    dieSize: Dp = 72.dp
) {
    val hasRolled = results != null

    var displayedNumbers by remember { mutableStateOf(List(dice.size) { 1 }) }

    LaunchedEffect(isRolling, results) {
        if (isRolling && results != null) {
            val fastEnd = System.currentTimeMillis() + 600
            while (System.currentTimeMillis() < fastEnd) {
                displayedNumbers = dice.map { Random.nextInt(1, it.faceCount + 1) }
                delay(50)
            }
            delay(100)
            displayedNumbers = dice.mapIndexed { i, _ ->
                if (Random.nextBoolean()) results[i] else Random.nextInt(1, dice[i].faceCount + 1)
            }
            delay(200)
            displayedNumbers = dice.mapIndexed { i, _ ->
                if (Random.nextBoolean()) results[i] else Random.nextInt(1, dice[i].faceCount + 1)
            }
            delay(400)
            displayedNumbers = results
            onAnimationComplete()
        } else if (!isRolling && results != null && displayedNumbers != results) {
            displayedNumbers = results
        }
    }

    val rows = dice.chunked(4)

    Column(
        modifier = modifier,
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
    dieSize: Dp = 72.dp
) {
    val dieColor = Color(die.color.hex)
    val isWhite = die.color == DiceColor.WHITE
    val faceLabel = "d${die.faceCount}"

    val borderColor = if (isWhite) Color.Black else dieColor
    val bgColor = if (isWhite) Color.White else dieColor.copy(alpha = 0.12f)
    val labelColor = if (isWhite) Color.Black else dieColor

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
            color = labelColor,
            fontSize = labelFontSize
        )
        Spacer(modifier = Modifier.height(gap))
        Text(
            text = if (displayNumber != null) displayNumber.toString() else faceLabel,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            fontSize = numberFontSize,
            textAlign = TextAlign.Center
        )
    }
}
