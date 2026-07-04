package com.turnzero.games.yugioh

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LifeCounterCalcScreen(
    player: Player,
    calcState: CalcInputState,
    currentLp: Int,
    onBack: () -> Unit,
    onAction: LifeCounterCalcActions,
    modifier: Modifier = Modifier
) {
    val playerLabel = when (player) {
        Player.PLAYER_1 -> "Duellist 1"
        Player.PLAYER_2 -> "Duellist 2"
    }
    val opSymbol = when (calcState.operation) {
        Operation.ADD -> "+"
        Operation.SUBTRACT -> "\u2212"
        Operation.DIVIDE -> "\u00f72"
    }

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.5f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CalcCell(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Box(
                modifier = Modifier
                    .weight(3f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Column(verticalArrangement = Arrangement.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(
                                    topStartPercent = 0, topEndPercent = 50,
                                    bottomStartPercent = 0, bottomEndPercent = 50
                                ))
                                .background(MaterialTheme.colorScheme.onSurface)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "LP",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.surface
                                ),
                                fontSize = 13.sp
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = playerLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row {
                        Text(
                            text = currentLp.toString(),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            fontSize = 28.sp
                        )
                        if (calcState.digits.isNotEmpty()) {
                            Spacer(Modifier.width(8.dp))
                            val opColor = when (calcState.operation) {
                                Operation.ADD -> MaterialTheme.colorScheme.primary
                                Operation.SUBTRACT -> MaterialTheme.colorScheme.error
                                Operation.DIVIDE -> MaterialTheme.colorScheme.tertiary
                            }
                            Text(
                                text = "$opSymbol ",
                                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 22.sp),
                                color = opColor
                            )
                            Text(
                                text = calcState.digits,
                                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 22.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (calcState.trailingZeroCount > 0) {
                                Text(
                                    text = "0".repeat(calcState.trailingZeroCount),
                                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 22.sp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CalcCell(
                onClick = onAction.onClear,
                label = "EFF",
                modifier = Modifier.weight(1f)
            )
            CalcCell(onClick = { onAction.onDigit(7) }, label = "7", modifier = Modifier.weight(1f))
            CalcCell(onClick = { onAction.onDigit(8) }, label = "8", modifier = Modifier.weight(1f))
            CalcCell(onClick = { onAction.onDigit(9) }, label = "9", modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.weight(0f))
        }

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CalcCell(
                onClick = { onAction.onSetOperation(Operation.ADD) },
                label = "+",
                selected = calcState.operation == Operation.ADD,
                modifier = Modifier.weight(1f)
            )
            CalcCell(onClick = { onAction.onDigit(4) }, label = "4", modifier = Modifier.weight(1f))
            CalcCell(onClick = { onAction.onDigit(5) }, label = "5", modifier = Modifier.weight(1f))
            CalcCell(onClick = { onAction.onDigit(6) }, label = "6", modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.weight(0f))
        }

        Row(
            modifier = Modifier.weight(2f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Column(
                modifier = Modifier.weight(4f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CalcCell(
                        onClick = { onAction.onSetOperation(Operation.SUBTRACT) },
                        label = "\u2212",
                        selected = calcState.operation == Operation.SUBTRACT,
                        modifier = Modifier.weight(1f)
                    )
                    CalcCell(onClick = { onAction.onDigit(1) }, label = "1", modifier = Modifier.weight(1f))
                    CalcCell(onClick = { onAction.onDigit(2) }, label = "2", modifier = Modifier.weight(1f))
                    CalcCell(onClick = { onAction.onDigit(3) }, label = "3", modifier = Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CalcCell(
                        onClick = onAction.onDivideBy2,
                        label = "\u00f72",
                        modifier = Modifier.weight(1f)
                    )
                    CalcCell(onClick = { onAction.onDigit(0) }, label = "0", modifier = Modifier.weight(1f))
                    CalcCell(onClick = { onAction.onAddZeros(2) }, label = "00", modifier = Modifier.weight(1f))
                    CalcCell(onClick = { onAction.onAddZeros(3) }, label = "000", modifier = Modifier.weight(1f))
                }
            }
            CalcCell(
                onClick = onAction.onApply,
                label = "\u003d",
                containerColor = Color(0xFF1B4332),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun CalcCell(
    onClick: () -> Unit,
    label: String? = null,
    selected: Boolean = false,
    containerColor: Color = Color.Unspecified,
    modifier: Modifier = Modifier,
    content: @Composable (() -> Unit)? = null
) {
    val bg = when {
        containerColor != Color.Unspecified -> containerColor
        selected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val fg = when {
        containerColor != Color.Unspecified -> Color.White
        selected -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (content != null) {
            content()
        } else if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Medium
                ),
                color = fg
            )
        }
    }
}
