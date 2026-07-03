package com.turnzero.tools.rules

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun RulesScreen(
    uiState: RulesUiState,
    onSelectRuleset: (Int) -> Unit,
    onSelectTab: (Int) -> Unit,
    onPageChanged: (Int) -> Unit,
    onToggleStep: (Int) -> Unit,
    onToggleKeyword: (Int) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
}
