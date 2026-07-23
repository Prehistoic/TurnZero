package com.turnzero.tools.lifecounter

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun LpCounterScreen(
    uiState: LpCounterUiState,
    onIncrement: (Int) -> Unit,
    onDecrement: (Int) -> Unit,
    onRelease: (Int) -> Unit,
    onResetPlayer: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState.playerCount) {
        2 -> TwoPlayerLayout(uiState, onIncrement, onDecrement, onRelease, onResetPlayer, modifier)
        3 -> ThreePlayerLayout(uiState, onIncrement, onDecrement, onRelease, onResetPlayer, modifier)
        4 -> FourPlayerLayout(uiState, onIncrement, onDecrement, onRelease, onResetPlayer, modifier)
    }
}

@Composable
private fun TwoPlayerLayout(
    uiState: LpCounterUiState,
    onIncrement: (Int) -> Unit,
    onDecrement: (Int) -> Unit,
    onRelease: (Int) -> Unit,
    onResetPlayer: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val players = uiState.players
    Column(modifier = modifier.fillMaxSize()) {
        HorizontalPlayerBox(
            player = players.getOrNull(1) ?: players[0],
            isUpsideDown = true,
            onIncrement = { onIncrement(players.getOrNull(1)?.number ?: 2) },
            onDecrement = { onDecrement(players.getOrNull(1)?.number ?: 2) },
            onRelease = { onRelease(players.getOrNull(1)?.number ?: 2) },
            onReset = { onResetPlayer(players.getOrNull(1)?.number ?: 2) },
            modifier = Modifier.weight(1f),
        )
        HorizontalDivider()
        HorizontalPlayerBox(
            player = players[0],
            isUpsideDown = false,
            onIncrement = { onIncrement(1) },
            onDecrement = { onDecrement(1) },
            onRelease = { onRelease(1) },
            onReset = { onResetPlayer(1) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ThreePlayerLayout(
    uiState: LpCounterUiState,
    onIncrement: (Int) -> Unit,
    onDecrement: (Int) -> Unit,
    onRelease: (Int) -> Unit,
    onResetPlayer: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val players = uiState.players
    Column(modifier = modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxHeight(0.5f)) {
            VerticalPlayerBox(
                player = players[1],
                isUpsideDown = false,
                onIncrement = { onIncrement(2) },
                onDecrement = { onDecrement(2) },
                onRelease = { onRelease(2) },
                onReset = { onResetPlayer(2) },
                modifier = Modifier.weight(1f),
            )
            VerticalPlayerBox(
                player = players[2],
                isUpsideDown = true,
                onIncrement = { onIncrement(3) },
                onDecrement = { onDecrement(3) },
                onRelease = { onRelease(3) },
                onReset = { onResetPlayer(3) },
                modifier = Modifier.weight(1f),
            )
        }
        HorizontalDivider()
        HorizontalPlayerBox(
            player = players[0],
            isUpsideDown = false,
            onIncrement = { onIncrement(1) },
            onDecrement = { onDecrement(1) },
            onRelease = { onRelease(1) },
            onReset = { onResetPlayer(1) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun FourPlayerLayout(
    uiState: LpCounterUiState,
    onIncrement: (Int) -> Unit,
    onDecrement: (Int) -> Unit,
    onRelease: (Int) -> Unit,
    onResetPlayer: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val players = uiState.players
    Column(modifier = modifier.fillMaxSize()) {
        Row(modifier = Modifier.weight(1f)) {
            VerticalPlayerBox(
                player = players[0],
                isUpsideDown = false,
                onIncrement = { onIncrement(1) },
                onDecrement = { onDecrement(1) },
                onRelease = { onRelease(1) },
                onReset = { onResetPlayer(1) },
                modifier = Modifier.weight(1f),
            )
            VerticalPlayerBox(
                player = players[1],
                isUpsideDown = true,
                onIncrement = { onIncrement(2) },
                onDecrement = { onDecrement(2) },
                onRelease = { onRelease(2) },
                onReset = { onResetPlayer(2) },
                modifier = Modifier.weight(1f),
            )
        }
        HorizontalDivider()
        Row(modifier = Modifier.weight(1f)) {
            VerticalPlayerBox(
                player = players[2],
                isUpsideDown = false,
                onIncrement = { onIncrement(3) },
                onDecrement = { onDecrement(3) },
                onRelease = { onRelease(3) },
                onReset = { onResetPlayer(3) },
                modifier = Modifier.weight(1f),
            )
            VerticalPlayerBox(
                player = players[3],
                isUpsideDown = true,
                onIncrement = { onIncrement(4) },
                onDecrement = { onDecrement(4) },
                onRelease = { onRelease(4) },
                onReset = { onResetPlayer(4) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}
