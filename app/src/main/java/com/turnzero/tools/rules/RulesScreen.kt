package com.turnzero.tools.rules

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
    onSelectPdfAsset: (String) -> Unit,
    onDismissPdfSelector: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val config = uiState.currentConfig ?: return

    if (uiState.showPdfSelector && config.pdfAssets != null) {
        AlertDialog(
            onDismissRequest = { onDismissPdfSelector() },
            title = { Text("Choose Rules PDF") },
            text = {
                Column {
                    config.pdfAssets.forEach { pdf ->
                        TextButton(
                            onClick = { onSelectPdfAsset(pdf.assetPath) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(pdf.name, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { onDismissPdfSelector() }) { Text("Cancel") }
            },
        )
        return
    }

    if (uiState.showPdf && uiState.selectedPdfAssetPath != null) {
        RulesPdfSheet(
            assetPath = uiState.selectedPdfAssetPath!!,
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
        if (uiState.selectedTabIndex in visibleTabs.indices && pagerState.currentPage != uiState.selectedTabIndex) {
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
        ScrollableTabRow(
            selectedTabIndex = currentPageForTab.coerceIn(visibleTabs.indices),
            edgePadding = 16.dp,
        ) {
            visibleTabs.forEachIndexed { index, tab ->
                Tab(
                    selected = currentPageForTab == index,
                    onClick = { onSelectTab(index) },
                    text = {
                        Text(
                            text = tab.title,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Visible,
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
                    cards = config.victoryRules,
                )
                "deck" -> RulesVictoryTab(
                    cards = config.deckRules ?: emptyList(),
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
                "field" -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                ) {
                    ContentBlockRenderer(
                        blocks = config.fieldContent ?: emptyList(),
                    )
                }
            }
        }
    }
}
