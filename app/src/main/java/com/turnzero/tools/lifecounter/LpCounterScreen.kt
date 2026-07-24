package com.turnzero.tools.lifecounter

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val BOX_SPACING = 8.dp

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

private fun playerColor(index: Int) = PlayerColors[index % PlayerColors.size]

@Composable
private fun PlayerBoxEntry(
    player: LpPlayer,
    isUpsideDown: Boolean,
    verticalMode: Boolean,
    onIncrement: (Int) -> Unit,
    onDecrement: (Int) -> Unit,
    onRelease: (Int) -> Unit,
    onResetPlayer: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    PlayerBox(
        player = player,
        isUpsideDown = isUpsideDown,
        playerColor = playerColor(player.number - 1),
        onIncrement = { onIncrement(player.number) },
        onDecrement = { onDecrement(player.number) },
        onRelease = { onRelease(player.number) },
        onReset = { onResetPlayer(player.number) },
        modifier = modifier,
        verticalMode = verticalMode,
    )
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = BOX_SPACING),
        verticalArrangement = Arrangement.spacedBy(BOX_SPACING),
    ) {
        PlayerBoxEntry(
            player = players.getOrNull(1) ?: players[0],
            isUpsideDown = true,
            verticalMode = false,
            onIncrement = onIncrement,
            onDecrement = onDecrement,
            onRelease = onRelease,
            onResetPlayer = onResetPlayer,
            modifier = Modifier.weight(1f),
        )
        PlayerBoxEntry(
            player = players[0],
            isUpsideDown = false,
            verticalMode = false,
            onIncrement = onIncrement,
            onDecrement = onDecrement,
            onRelease = onRelease,
            onResetPlayer = onResetPlayer,
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = BOX_SPACING),
        verticalArrangement = Arrangement.spacedBy(BOX_SPACING),
    ) {
        Row(
            modifier = Modifier.fillMaxHeight(0.5f),
            horizontalArrangement = Arrangement.spacedBy(BOX_SPACING),
        ) {
            PlayerBoxEntry(
                player = players[1],
                isUpsideDown = false,
                verticalMode = true,
                onIncrement = onIncrement,
                onDecrement = onDecrement,
                onRelease = onRelease,
                onResetPlayer = onResetPlayer,
                modifier = Modifier.weight(1f),
            )
            PlayerBoxEntry(
                player = players[2],
                isUpsideDown = true,
                verticalMode = true,
                onIncrement = onIncrement,
                onDecrement = onDecrement,
                onRelease = onRelease,
                onResetPlayer = onResetPlayer,
                modifier = Modifier.weight(1f),
            )
        }
        PlayerBoxEntry(
            player = players[0],
            isUpsideDown = false,
            verticalMode = false,
            onIncrement = onIncrement,
            onDecrement = onDecrement,
            onRelease = onRelease,
            onResetPlayer = onResetPlayer,
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = BOX_SPACING),
        verticalArrangement = Arrangement.spacedBy(BOX_SPACING),
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(BOX_SPACING),
        ) {
            PlayerBoxEntry(
                player = players[0],
                isUpsideDown = false,
                verticalMode = true,
                onIncrement = onIncrement,
                onDecrement = onDecrement,
                onRelease = onRelease,
                onResetPlayer = onResetPlayer,
                modifier = Modifier.weight(1f),
            )
            PlayerBoxEntry(
                player = players[1],
                isUpsideDown = true,
                verticalMode = true,
                onIncrement = onIncrement,
                onDecrement = onDecrement,
                onRelease = onRelease,
                onResetPlayer = onResetPlayer,
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(BOX_SPACING),
        ) {
            PlayerBoxEntry(
                player = players[2],
                isUpsideDown = false,
                verticalMode = true,
                onIncrement = onIncrement,
                onDecrement = onDecrement,
                onRelease = onRelease,
                onResetPlayer = onResetPlayer,
                modifier = Modifier.weight(1f),
            )
            PlayerBoxEntry(
                player = players[3],
                isUpsideDown = true,
                verticalMode = true,
                onIncrement = onIncrement,
                onDecrement = onDecrement,
                onRelease = onRelease,
                onResetPlayer = onResetPlayer,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
