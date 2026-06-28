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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    modifier: Modifier = Modifier
) {
    val hasRolled = results != null

    var displayedNumbers by remember { mutableStateOf(List(dice.size) { 1 }) }

    LaunchedEffect(isRolling, results) {
        if (isRolling && results != null) {
            // Fast shuffle phase: ~600ms
            val fastEnd = System.currentTimeMillis() + 600
            while (System.currentTimeMillis() < fastEnd) {
                displayedNumbers = dice.map { Random.nextInt(1, it.faceCount + 1) }
                delay(50)
            }
            // Deceleration phase: ~400ms
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

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        dice.forEachIndexed { index, die ->
            val number = if (isRolling || hasRolled) displayedNumbers.getOrElse(index) { 1 } else 0
            val settled = !isRolling && results != null

            val scale by animateFloatAsState(
                targetValue = if (settled) 1f else 1f,
                animationSpec = spring(dampingRatio = 0.4f, stiffness = 300f),
                label = "dieScale"
            )

            DieCard(
                die = die,
                displayNumber = if (hasRolled || isRolling) number else null,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
        }
    }
}

@Composable
fun DieCard(
    die: Die,
    displayNumber: Int?,
    modifier: Modifier = Modifier
) {
    val dieColor = Color(die.color.hex)
    val faceLabel = "d${die.faceCount}"

    Column(
        modifier = modifier
            .size(72.dp)
            .border(2.dp, dieColor, RoundedCornerShape(12.dp))
            .background(dieColor.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = faceLabel,
            style = MaterialTheme.typography.labelSmall,
            color = dieColor,
            fontSize = 10.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = if (displayNumber != null) displayNumber.toString() else faceLabel,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = if (displayNumber != null) Color.Unspecified else dieColor.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}
