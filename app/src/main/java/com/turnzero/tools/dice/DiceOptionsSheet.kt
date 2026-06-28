package com.turnzero.tools.dice

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val faceCounts = listOf(2, 4, 6, 8, 10, 12, 20, 100)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiceOptionsSheet(
    uiState: DiceUiState,
    onChangeDieCount: (Int, DiceColor, Int) -> Unit,
    onSetSelectedColor: (DiceColor) -> Unit,
    onLoadPreset: (DicePreset) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Presets
            if (uiState.presets.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    uiState.presets.forEach { preset ->
                        FilterChip(
                            selected = false,
                            onClick = { onLoadPreset(preset) },
                            label = { Text(preset.name, fontSize = 12.sp) },
                            modifier = Modifier.padding(horizontal = 4.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
            }

            // Dice pool (grouped by face+color)
            val grouped = uiState.dice.groupBy { it.faceCount to it.color }
            if (grouped.isEmpty()) {
                Text(
                    text = "No dice",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            grouped.forEach { (key, dice) ->
                val (faceCount, color) = key
                val isWhite = color == DiceColor.WHITE
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        onClick = { onChangeDieCount(faceCount, color, -1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Remove", modifier = Modifier.size(18.dp))
                    }
                    Text(
                        text = "d$faceCount",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    if (dice.size > 1) {
                        Text(
                            text = "× ${dice.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(color.hex))
                            .then(
                                if (isWhite) Modifier.border(1.dp, Color.Black, CircleShape)
                                else Modifier
                            )
                    )
                    IconButton(
                        onClick = { onChangeDieCount(faceCount, color, 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            // Add dice rows (2 rows of 4)
            for (row in faceCounts.chunked(4)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    row.forEach { faceCount ->
                        val isWhite = uiState.selectedColor == DiceColor.WHITE
                        val isBlack = uiState.selectedColor == DiceColor.BLACK
                        val colorHex = Color(uiState.selectedColor.hex)
                        val tokenBorder = when {
                            isWhite -> Color.Black
                            isBlack -> Color.White
                            else -> colorHex
                        }
                        val tokenBg = when {
                            isWhite -> Color.White
                            isBlack -> Color.Black
                            else -> colorHex.copy(alpha = 0.1f)
                        }
                        val tokenText = when {
                            isWhite -> Color.Black
                            isBlack -> Color.White
                            else -> colorHex
                        }
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(52.dp)
                                .border(2.dp, tokenBorder, RoundedCornerShape(8.dp))
                                .background(tokenBg, RoundedCornerShape(8.dp))
                                .clickable { onChangeDieCount(faceCount, uiState.selectedColor, 1) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "d$faceCount",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = tokenText,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    repeat(4 - row.size) {
                        Spacer(modifier = Modifier.size(60.dp))
                    }
                }
            }

            // Color selector
            Row(
                modifier = Modifier.padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                DiceColor.entries.forEach { color ->
                    val isSelected = color == uiState.selectedColor
                    val isWhite = color == DiceColor.WHITE
                    val isBlack = color == DiceColor.BLACK
                    val needsBorder = isSelected || isWhite || isBlack
                    val dotBorder = when {
                        isSelected -> MaterialTheme.colorScheme.primary
                        isWhite -> MaterialTheme.colorScheme.outline
                        isBlack -> Color.White
                        else -> Color.Transparent
                    }
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(color.hex))
                            .border(
                                width = if (needsBorder) 3.dp else 0.dp,
                                color = dotBorder,
                                shape = CircleShape
                            )
                            .clickable { onSetSelectedColor(color) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
