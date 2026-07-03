package com.turnzero.tools.coin

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun CoinAnimation(
    coinCount: Int,
    results: List<CoinFace>,
    isFlipping: Boolean,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier,
    coinSize: Dp,
    onFlip: () -> Unit = {},
    triggerScale: Int = 0
) {
    val hasFlipped = results.isNotEmpty()

    var displayedFaces by remember { mutableStateOf(List(coinCount) { CoinFace.HEAD }) }

    LaunchedEffect(isFlipping, results) {
        if (isFlipping && results.isNotEmpty()) {
            val fastEnd = System.currentTimeMillis() + 600
            while (System.currentTimeMillis() < fastEnd) {
                displayedFaces = List(coinCount) { if (Random.nextBoolean()) CoinFace.HEAD else CoinFace.TAIL }
                delay(80)
            }
            delay(100)
            displayedFaces = List(coinCount) { if (Random.nextBoolean()) CoinFace.HEAD else CoinFace.TAIL }
            delay(150)
            displayedFaces = results.mapIndexed { i, _ ->
                if (Random.nextBoolean()) results[i] else (if (Random.nextBoolean()) CoinFace.HEAD else CoinFace.TAIL)
            }
            delay(250)
            displayedFaces = results
            onAnimationComplete()
        } else if (!isFlipping && results.isNotEmpty() && displayedFaces != results) {
            displayedFaces = results
        }
    }

    var targetScale by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(triggerScale) {
        if (triggerScale > 0) {
            targetScale = 1.08f
            delay(100)
            targetScale = 1f
            delay(80)
            onFlip()
        }
    }

    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f),
        label = "tapScale"
    )

    val density = LocalDensity.current

    val rows = (1..coinCount).toList().chunked(4)

    Column(
        modifier = modifier.scale(animatedScale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        rows.forEachIndexed { rowIndex, rowItems ->
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                rowItems.forEachIndexed { colIndex, _ ->
                    val index = rowIndex * 4 + colIndex
                    val face = if (isFlipping || hasFlipped) displayedFaces.getOrElse(index) { CoinFace.HEAD } else CoinFace.HEAD

                    CoinCard(
                        face = face,
                        isAnimating = isFlipping,
                        modifier = Modifier.padding(4.dp),
                        coinSize = coinSize
                    )
                }
            }
        }
    }
}

@Composable
fun CoinCard(
    face: CoinFace,
    isAnimating: Boolean,
    modifier: Modifier = Modifier,
    coinSize: Dp
) {
    val isHead = face == CoinFace.HEAD

    val headGradient = Brush.radialGradient(
        colors = listOf(Color(0xFFFFD700), Color(0xFFDAA520), Color(0xFFB8860B))
    )
    val tailGradient = Brush.radialGradient(
        colors = listOf(Color(0xFFE0E0E0), Color(0xFFC0C0C0), Color(0xFFA0A0A0))
    )

    val gradient = if (isHead) headGradient else tailGradient
    val emoji = if (isHead) "\uD83D\uDC64" else "\u2B50"

    val rimColor = if (isHead) Color(0xFFD4AF37) else Color(0xFFB0B0B0)

    var targetRotX by remember { mutableFloatStateOf(0f) }
    var targetRotY by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            targetRotX = Random.nextFloat() * 720f
            targetRotY = Random.nextFloat() * 720f
        } else {
            targetRotX = 0f
            targetRotY = 0f
        }
    }

    val localDensity = LocalDensity.current
    val cameraDist = 8f * localDensity.density

    val rotX by animateFloatAsState(
        targetValue = targetRotX,
        animationSpec = tween(durationMillis = 800),
        label = "rotX"
    )
    val rotY by animateFloatAsState(
        targetValue = targetRotY,
        animationSpec = tween(durationMillis = 1000),
        label = "rotY"
    )

    val borderWidth = (coinSize.value / 32f).dp

    Box(
        modifier = modifier
            .size(coinSize)
            .graphicsLayer {
                rotationX = rotX
                rotationY = rotY
                cameraDistance = cameraDist
            }
            .border(borderWidth, rimColor, CircleShape)
            .background(gradient, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = (coinSize.value * 0.38f).sp,
            textAlign = TextAlign.Center
        )
    }
}
