package com.turnzero.tools.rules

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
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
    val config = uiState.currentConfig ?: return

    val visibleTabs = uiState.visibleTabs
    val pagerState = rememberPagerState(
        initialPage = uiState.selectedTabIndex.coerceIn(0, (visibleTabs.size - 1).coerceAtLeast(0)),
        pageCount = { visibleTabs.size },
    )

    LaunchedEffect(uiState.selectedTabIndex) {
        if (uiState.selectedTabIndex in visibleTabs.indices) {
            pagerState.animateScrollToPage(uiState.selectedTabIndex)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                if (page != uiState.selectedTabIndex) {
                    onPageChanged(page)
                }
            }
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (uiState.configs.size > 1) {
            RulesetChipBar(
                configs = uiState.configs,
                selectedIndex = uiState.selectedRulesetIndex,
                onSelect = onSelectRuleset,
            )
        }

        TabRow(selectedTabIndex = uiState.selectedTabIndex.coerceIn(visibleTabs.indices)) {
            visibleTabs.forEachIndexed { index, tab ->
                Tab(
                    selected = uiState.selectedTabIndex == index,
                    onClick = { onSelectTab(index) },
                    text = { Text(tab.title) },
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            val tab = visibleTabs.getOrNull(page) ?: return@HorizontalPager
            when (tab.key) {
                "setup" -> RulesSetupTab(
                    steps = config.setup,
                    expandedIndex = uiState.expandedStepIndex,
                    onToggle = onToggleStep,
                )
                "wincons" -> RulesWinConsTab(
                    winCons = config.winCons,
                )
                "sequence" -> RulesPlaySequenceTab(
                    steps = config.playSequence,
                    expandedIndex = uiState.expandedStepIndex,
                    onToggle = onToggleStep,
                )
                "keywords" -> RulesKeywordsTab(
                    keywords = uiState.filteredKeywords,
                    searchQuery = uiState.keywordSearchQuery,
                    expandedIndex = uiState.expandedKeywordIndex,
                    onToggle = onToggleKeyword,
                    onSearchQueryChange = onSearchQueryChange,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RulesetChipBar(
    configs: List<RulesConfig>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        modifier = modifier,
        edgePadding = 16.dp,
        divider = {},
    ) {
        configs.forEachIndexed { index, config ->
            FilterChip(
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                label = { Text(config.rulesetName) },
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }
}
