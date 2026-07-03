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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turnzero.core.tools.api.Tool

class RulesTool : Tool {
    override val id = "rules"
    override val name = "Rules"
    override val icon: ImageVector get() = Icons.AutoMirrored.Filled.MenuBook
    override val supportedGames = setOf("*")

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun TopBarActions(): @Composable RowScope.() -> Unit {
        val viewModel: RulesViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val hasPdf = uiState.currentConfig?.pdfUrl != null
        val configs = uiState.configs
        val selectedIndex = uiState.selectedRulesetIndex

        return {
            if (configs.size > 1) {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                ) {
                    Row(
                        modifier = Modifier
                            .menuAnchor()
                            .clickable { expanded = true }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = configs.getOrNull(selectedIndex)?.rulesetName ?: "",
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        configs.forEachIndexed { index, config ->
                            DropdownMenuItem(
                                text = { Text(config.rulesetName) },
                                onClick = {
                                    viewModel.selectRuleset(index)
                                    expanded = false
                                },
                            )
                        }
                    }
                }
            }
            if (hasPdf) {
                IconButton(onClick = { viewModel.togglePdf() }) {
                    Icon(
                        Icons.Default.PictureAsPdf,
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
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun OptionsSheet(onDismiss: () -> Unit) {
        ModalBottomSheet(onDismissRequest = onDismiss) {
            Text(
                text = "No options available for this tool.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(32.dp),
            )
        }
    }
}
