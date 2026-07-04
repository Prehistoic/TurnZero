package com.turnzero.games.yugioh

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

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

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Column 1: back, EFF, +, -, ÷2
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CalcCell(
                    onClick = onBack,
                    modifier = Modifier
                        .weight(1.5f)
                        .fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                CalcCell(
                    onClick = onAction.onClear,
                    label = "EFF",
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )
                CalcCell(
                    onClick = { onAction.onSetOperation(Operation.ADD) },
                    label = "+",
                    selected = calcState.operation == Operation.ADD,
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )
                CalcCell(
                    onClick = { onAction.onSetOperation(Operation.SUBTRACT) },
                    label = "\u2212",
                    selected = calcState.operation == Operation.SUBTRACT,
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )
                CalcCell(
                    onClick = onAction.onDivideBy2,
                    label = "\u00f72",
                    selected = calcState.operation == Operation.DIVIDE,
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )
            }

            // Columns 2-4: header + digits
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1.5f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = playerLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                CalcCell(onClick = { onAction.onDigit(7) }, label = "7", modifier = Modifier.weight(1f).fillMaxWidth())
                CalcCell(onClick = { onAction.onDigit(4) }, label = "4", modifier = Modifier.weight(1f).fillMaxWidth())
                CalcCell(onClick = { onAction.onDigit(1) }, label = "1", modifier = Modifier.weight(1f).fillMaxWidth())
                CalcCell(onClick = { onAction.onDigit(0) }, label = "0", modifier = Modifier.weight(1f).fillMaxWidth())
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1.5f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "LP",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                CalcCell(onClick = { onAction.onDigit(8) }, label = "8", modifier = Modifier.weight(1f).fillMaxWidth())
                CalcCell(onClick = { onAction.onDigit(5) }, label = "5", modifier = Modifier.weight(1f).fillMaxWidth())
                CalcCell(onClick = { onAction.onDigit(2) }, label = "2", modifier = Modifier.weight(1f).fillMaxWidth())
                CalcCell(onClick = { onAction.onAddZeros(2) }, label = "00", modifier = Modifier.weight(1f).fillMaxWidth())
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1.5f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
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
                        val opColor = when (calcState.operation) {
                            Operation.ADD -> MaterialTheme.colorScheme.primary
                            Operation.SUBTRACT -> MaterialTheme.colorScheme.error
                            Operation.DIVIDE -> MaterialTheme.colorScheme.tertiary
                        }
                        if (calcState.digits.isNotEmpty()) {
                            Text(
                                text = "$opSymbol ",
                                style = MaterialTheme.typography.labelSmall,
                                color = opColor
                            )
                            Text(
                                text = calcState.digits,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (!calcState.lockedIn && calcState.trailingZeroCount > 0) {
                                Text(
                                    text = "0".repeat(calcState.trailingZeroCount),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                }
                CalcCell(onClick = { onAction.onDigit(9) }, label = "9", modifier = Modifier.weight(1f).fillMaxWidth())
                CalcCell(onClick = { onAction.onDigit(6) }, label = "6", modifier = Modifier.weight(1f).fillMaxWidth())
                CalcCell(onClick = { onAction.onDigit(3) }, label = "3", modifier = Modifier.weight(1f).fillMaxWidth())
                CalcCell(onClick = { onAction.onAddZeros(3) }, label = "000", modifier = Modifier.weight(1f).fillMaxWidth())
            }

            // Column 5: current LP display + empty rows + =
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1.5f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentLp.toString(),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        fontSize = 24.sp
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.weight(1f))
                CalcCell(
                    onClick = onAction.onApply,
                    label = "\u003d",
                    containerColor = Color(0xFF1B4332),
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxWidth()
                )
            }
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
