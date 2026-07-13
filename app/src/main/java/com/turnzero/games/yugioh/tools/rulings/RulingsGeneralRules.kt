package com.turnzero.games.yugioh.tools.rulings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turnzero.games.yugioh.tools.rulings.data.FormatRule

private fun String.parseRuleText(primaryColor: Color) = buildAnnotatedString {
    val boldRegex = Regex("""\*\*(.+?)\*\*""")
    val cardRegex = Regex(""""([^"]+)"""")

    data class Token(val start: Int, val end: Int, val type: Int)
    val tokens = mutableListOf<Token>()

    boldRegex.findAll(this@parseRuleText).forEach { m ->
        tokens.add(Token(m.range.first, m.range.last + 1, 0))
    }
    cardRegex.findAll(this@parseRuleText).forEach { m ->
        tokens.add(Token(m.range.first, m.range.last + 1, 1))
    }
    tokens.sortBy { it.start }

    var pos = 0
    for (token in tokens) {
        if (token.start < pos) continue
        append(this@parseRuleText.substring(pos, token.start))
        val content = this@parseRuleText.substring(token.start, token.end)
        when (token.type) {
            0 -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(content.removeSurrounding("**"))
            }
            1 -> withStyle(SpanStyle(color = primaryColor, fontWeight = FontWeight.Medium)) {
                append(content)
            }
        }
        pos = token.end
    }
    append(this@parseRuleText.substring(pos))
}

@Composable
fun RulingsGeneralRules(
    rules: List<FormatRule>,
    expandedRuleIndex: Int?,
    onToggleRule: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (rules.isEmpty()) return

    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val listState = rememberLazyListState()

    LaunchedEffect(expandedRuleIndex) {
        if (expandedRuleIndex != null) {
            snapshotFlow {
                listState.layoutInfo.visibleItemsInfo
                    .firstOrNull { it.index == expandedRuleIndex }
            }.filterNotNull().first()
            listState.animateScrollToItem(expandedRuleIndex)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(rules, key = { _, rule -> "${rule.title}-${rule.description}" }) { index, rule ->
            val isExpanded = expandedRuleIndex == index
            val isSpecial = rule.isSpecial

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSpecial)
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    else
                        MaterialTheme.colorScheme.surfaceContainer,
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleRule(index) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = buildAnnotatedString {
                                val label = if (isSpecial) "SPECIAL" else "RULE #${index + 1}"
                                val labelColor = if (isSpecial) errorColor else primaryColor
                                withStyle(SpanStyle(
                                    color = labelColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                )) {
                                    append(label)
                                }
                                append("  ")
                                withStyle(SpanStyle(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = if (isSpecial) errorColor else Color.Unspecified,
                                )) {
                                    append(rule.title)
                                }
                            },
                        )
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp),
                    )
                }
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically(),
                ) {
                    Column(
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
                    ) {
                        if (isSpecial) {
                            HorizontalDivider(
                                color = errorColor.copy(alpha = 0.5f),
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                            if (rule.description.isNotBlank()) {
                                val parsedDesc = remember(rule.description, primaryColor) {
                                    rule.description.parseRuleText(primaryColor)
                                }
                                Text(
                                text = parsedDesc,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        } else                         if (rule.subSections.isNotEmpty() && rule.description.isNotBlank()) {
                            val parsedDesc = remember(rule.description, primaryColor) {
                                rule.description.parseRuleText(primaryColor)
                            }
                            Text(
                                text = parsedDesc,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        rule.subSections.forEachIndexed { subIdx, subSection ->
                            val isAlternating = rule.title.contains("Damage Step", ignoreCase = true) ||
                                rule.title.contains("7-Timing", ignoreCase = true)
                            val altBg = isAlternating && subIdx % 2 == 1
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSpecial)
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                                else if (altBg)
                                    MaterialTheme.colorScheme.surfaceContainerLowest
                                else
                                    MaterialTheme.colorScheme.surfaceContainerLow,
                                tonalElevation = if (altBg || isSpecial) 0.dp else 1.dp,
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    if (subSection.title.isNotBlank()) {
                                        val parsedTitle = remember(subSection.title, primaryColor) {
                                            subSection.title.parseRuleText(primaryColor)
                                        }
                                        Text(
                                            text = parsedTitle,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                    if (subSection.body.isNotBlank()) {
                                        val parsedBody = remember(subSection.body, primaryColor) {
                                            subSection.body.parseRuleText(primaryColor)
                                        }
                                        Text(
                                            text = parsedBody,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        if (rule.subSections.isEmpty() && !isSpecial) {
                            val parsedDesc = remember(rule.description, primaryColor) {
                                rule.description.parseRuleText(primaryColor)
                            }
                            Text(
                                text = parsedDesc,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
