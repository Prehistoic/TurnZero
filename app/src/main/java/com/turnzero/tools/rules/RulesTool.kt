package com.turnzero.tools.rules

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turnzero.core.tools.api.Tool
import com.turnzero.core.ui.components.LayoutBottomSheet
import com.turnzero.core.ui.components.TZEmptyState

class RulesTool : Tool {
    override val id = "rules"
    override val name = "Rules"
    override val icon: ImageVector get() = Icons.AutoMirrored.Filled.MenuBook
    override val supportedGames = setOf("*")
    override val providesOwnTopBar = true

    @Composable
    override fun Screen(onBack: () -> Unit) {
        val viewModel: RulesToolViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        val subTool = state.subTool
        val rules = state.rules
        val draft = state.draftSelection

        val actionIcon: ImageVector? = when (subTool) {
            null -> Icons.Filled.GridView
            RuleSubTool.RULEBOOK -> null
            else -> Icons.AutoMirrored.Filled.MenuBook
        }
        val actionContentDescription: String? = when (subTool) {
            null -> "Change layout"
            RuleSubTool.RULEBOOK -> null
            else -> "Open rulebook pages"
        }
        val onActionClick: (() -> Unit)? = when (subTool) {
            null -> viewModel::showLayoutSheet
            RuleSubTool.RULEBOOK -> null
            else -> viewModel::jumpToPages
        }

        Column(modifier = Modifier.fillMaxSize()) {
            RulesToolTopBar(
                title = subTool?.title ?: "Rules",
                onBack = { if (subTool != null) viewModel.closeSubTool() else onBack() },
                pillText = state.specificitySummary,
                onPillClick = viewModel::openSpecificityDialog,
                actionIcon = actionIcon,
                actionContentDescription = actionContentDescription,
                onActionClick = onActionClick,
            )

            if (rules == null) {
                TZEmptyState(message = "No rules available for this game")
            } else {
                when (subTool) {
                    null -> RulesMainPanel(
                        subTools = RuleSubTool.entries,
                        layoutMode = state.layout,
                        onSubToolClick = viewModel::openSubTool,
                    )

                    RuleSubTool.SETUP -> SetupCarousel(steps = state.setupSteps)

                    RuleSubTool.TURN -> TurnStructureScreen(
                        phases = state.turnPhases,
                        phaseIndex = state.turnPhaseIndex,
                        stepIndex = state.turnStepIndex,
                        onOpenPhase = viewModel::openPhase,
                        onOpenStep = viewModel::openStep,
                        onBackToOverview = viewModel::closePhase,
                        onBackToSteps = viewModel::closeStep,
                        onNextStep = viewModel::nextStep,
                        onPreviousStep = viewModel::previousStep,
                    )

                    RuleSubTool.WIN -> WinConditionScreen(conditions = state.winConditions)

                    RuleSubTool.KEYWORDS -> RulesKeywordsScreen(
                        keywords = state.filteredKeywords,
                        searchQuery = state.keywordQuery,
                        selectedCategory = state.keywordCategory,
                        allCategories = state.keywordCategories,
                        expandedIndex = state.expandedKeywordIndex,
                        onToggle = viewModel::toggleKeyword,
                        onSearchQueryChange = viewModel::setKeywordQuery,
                        onFilterCategory = viewModel::filterKeywordCategory,
                    )

                    RuleSubTool.RULEBOOK -> RulebookScreen(
                        rulebooks = state.matchingRulebooks,
                        selectedRulebookId = state.selectedRulebookId,
                        initialPage = state.pdfInitialPage,
                        showSelector = state.showPdfSelector,
                        onSelectRulebook = viewModel::selectRulebook,
                        onDismissSelector = viewModel::dismissPdfSelector,
                    )
                }
            }
        }

        if (rules != null && state.showSpecificityDialog && draft != null) {
            SpecificityDialog(
                manifest = rules.manifest,
                selection = draft,
                onSelectOption = viewModel::selectSpecificityOption,
                onNumberChange = viewModel::setSpecificityNumber,
                onConfirm = viewModel::confirmSpecificities,
            )
        }

        if (state.showLayoutSheet) {
            LayoutBottomSheet(
                currentLayout = state.layout,
                onLayoutSelected = viewModel::setLayout,
                onDismiss = viewModel::dismissLayoutSheet,
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun OptionsSheet(onDismiss: () -> Unit) {
        val viewModel: RulesToolViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        val rules = state.rules ?: return

        LaunchedEffect(Unit) { viewModel.openSpecificityEditor() }

        ModalBottomSheet(onDismissRequest = onDismiss) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .padding(bottom = 32.dp),
            ) {
                Text(
                    text = "Game Settings",
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.height(16.dp))
                SpecificityEditor(
                    manifest = rules.manifest,
                    selection = state.draftSelection ?: state.selection,
                    onSelectOption = viewModel::selectSpecificityOption,
                    onNumberChange = viewModel::setSpecificityNumber,
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        viewModel.confirmSpecificities()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Save")
                }
            }
        }
    }
}
