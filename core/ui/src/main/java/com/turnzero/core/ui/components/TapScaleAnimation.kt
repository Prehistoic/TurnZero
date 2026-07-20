package com.turnzero.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.*
import kotlinx.coroutines.delay

@Composable
fun rememberTapScale(
    triggerScale: Int,
    onAction: () -> Unit,
): State<Float> {
    var targetScale by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(triggerScale) {
        if (triggerScale > 0) {
            targetScale = 1.08f
            delay(100)
            targetScale = 1f
            delay(80)
            onAction()
        }
    }

    return animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f),
        label = "tapScale",
    )
}
