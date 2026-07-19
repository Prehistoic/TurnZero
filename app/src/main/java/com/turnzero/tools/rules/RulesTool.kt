package com.turnzero.tools.rules

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turnzero.core.tools.api.Tool

class RulesTool : Tool {
    override val id = "rules"
    override val name = "Rules"
    override val icon: ImageVector get() = Icons.AutoMirrored.Filled.MenuBook
    override val supportedGames = setOf("*")

    @Suppress("ComposableNaming")
    @Composable
    override fun TopBarActions(): @Composable RowScope.() -> Unit {
        val viewModel: RulesViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val hasPdf = uiState.hasPdf
        val configs = uiState.visibleConfigs
        val selectedId = uiState.selectedRulesetId

        return {
            if (configs.size > 1) {
                var expanded by remember { mutableStateOf(false) }
                Box {
                    Row(
                        modifier = Modifier
                            .clickable { expanded = true }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = configs.firstOrNull { it.rulesetId == selectedId }?.rulesetName ?: "",
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Icon(
                            Icons.Filled.ArrowDropDown,
                            contentDescription = "Select format",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        configs.forEach { config ->
                            DropdownMenuItem(
                                text = { Text(config.rulesetName) },
                                onClick = {
                                    expanded = false
                                    viewModel.selectRuleset(config.rulesetId)
                                },
                            )
                        }
                    }
                }
            }
            if (hasPdf) {
                IconButton(onClick = { viewModel.togglePdf() }) {
                    Icon(
                        Icons.Filled.PictureAsPdf,
                        contentDescription = "Official Rules PDF",
                    )
                }
            }
        }
    }

    @Composable
    override fun Screen(onBack: () -> Unit) {
        val viewModel: RulesViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        RulesScreen(
            uiState = uiState,
            onSelectTab = viewModel::selectTab,
            onPageChanged = viewModel::onPageChanged,
            onToggleStep = viewModel::toggleStep,
            onToggleKeyword = viewModel::toggleKeyword,
            onSearchQueryChange = viewModel::setSearchQuery,
            onFilterCategory = viewModel::filterCategory,
            onTogglePdf = viewModel::togglePdf,
            onSelectPdfAsset = viewModel::selectPdfAsset,
            onDismissPdfSelector = viewModel::dismissPdfSelector,
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun OptionsSheet(onDismiss: () -> Unit) {
        val viewModel: RulesViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val configs = uiState.configs
        val enabledRulesets = uiState.enabledRulesets
        var expanded by remember { mutableStateOf(false) }

        ModalBottomSheet(onDismissRequest = onDismiss) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .padding(bottom = 32.dp),
            ) {
                Text(
                    text = "Visible Formats",
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.height(8.dp))
                configs.forEach { config ->
                    val isEnabled = enabledRulesets.isEmpty() || config.rulesetId in enabledRulesets
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.toggleRulesetEnabled(config.rulesetId, !isEnabled)
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = isEnabled,
                            onCheckedChange = { viewModel.toggleRulesetEnabled(config.rulesetId, it) },
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = config.rulesetName,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Default Format",
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.height(12.dp))
                Box {
                    OutlinedButton(onClick = { expanded = true }) {
                        Text(configs.firstOrNull { it.rulesetId == uiState.defaultRulesetId }?.rulesetName ?: "")
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        configs.forEach { config ->
                            DropdownMenuItem(
                                text = { Text(config.rulesetName) },
                                onClick = {
                                    viewModel.setDefaultRuleset(config.rulesetId)
                                    expanded = false
                                },
                            )
                        }
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
