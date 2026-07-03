package com.turnzero.tools.rules

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun RulesPlaySequenceTab(
    steps: List<RuleStep>,
    expandedIndex: Int?,
    onToggle: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    RulesStepList(
        steps = steps,
        expandedIndex = expandedIndex,
        onToggle = onToggle,
        modifier = modifier,
    )
}
