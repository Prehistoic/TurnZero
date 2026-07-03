package com.turnzero.tools.rules

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(
    uiState: RulesUiState,
    onSelectTab: (Int) -> Unit,
    onPageChanged: (Int) -> Unit,
    onToggleStep: (Int) -> Unit,
    onToggleKeyword: (Int) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onFilterCategory: (String?) -> Unit,
    onTogglePdf: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val config = uiState.currentConfig ?: return

    if (uiState.showPdf && config.pdfUrl != null) {
        RulesPdfSheet(
            url = config.pdfUrl,
            onDismiss = onTogglePdf,
        )
        return
    }

    val visibleTabs = uiState.visibleTabs
    val pagerState = rememberPagerState(
        initialPage = uiState.selectedTabIndex.coerceIn(0, (visibleTabs.size - 1).coerceAtLeast(0)),
        pageCount = { visibleTabs.size },
    )

    val currentPageForTab by remember { derivedStateOf { pagerState.currentPage } }

    LaunchedEffect(uiState.selectedTabIndex) {
        if (uiState.selectedTabIndex in visibleTabs.indices) {
            pagerState.animateScrollToPage(uiState.selectedTabIndex)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                onPageChanged(page)
            }
    }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = currentPageForTab.coerceIn(visibleTabs.indices)) {
            visibleTabs.forEachIndexed { index, tab ->
                Tab(
                    selected = currentPageForTab == index,
                    onClick = { onSelectTab(index) },
                    text = {
                        Text(
                            text = tab.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 0,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            val tab = visibleTabs.getOrNull(page) ?: return@HorizontalPager
            when (tab.key) {
                "setup" -> RulesSetupTab(
                    steps = config.setup,
                    expandedIndex = uiState.expandedStepIndex,
                    onToggle = onToggleStep,
                )
                "turn" -> RulesTurnTab(
                    steps = config.playSequence,
                    expandedIndex = uiState.expandedStepIndex,
                    onToggle = onToggleStep,
                )
                "victory" -> RulesVictoryTab(
                    winCons = config.winCons,
                )
                "keywords" -> RulesKeywordsTab(
                    keywords = uiState.filteredKeywords,
                    searchQuery = uiState.keywordSearchQuery,
                    selectedCategory = uiState.selectedKeywordCategory,
                    allCategories = uiState.allKeywordCategories,
                    expandedIndex = uiState.expandedKeywordIndex,
                    onToggle = onToggleKeyword,
                    onSearchQueryChange = onSearchQueryChange,
                    onFilterCategory = onFilterCategory,
                )
            }
        }
    }
}
