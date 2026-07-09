package com.turnzero.games.yugioh.tools.rulings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turnzero.core.tools.api.Tool
import com.turnzero.games.yugioh.tools.rulings.data.FormatRulesData

class RulingsTool : Tool {
    override val id = "ygo-rulings-searcher"
    override val name = "Rulings"
    override val icon = Icons.Default.Search
    override val supportedGames = setOf("yugioh")

    @Composable
    override fun Screen(onBack: () -> Unit) {
        val viewModel: RulingsViewModel = hiltViewModel()

        RulingsScreen(
            onBack = onBack,
            viewModel = viewModel,
        )
    }

    @Composable
    override fun OptionsSheet(onDismiss: () -> Unit) {
        val viewModel: RulingsViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()

        RulingsOptionsSheet(
            allFormatsWithStatus = viewModel.allFormatsWithStatus,
            defaultFormat = state.defaultFormat,
            onSetDefaultFormat = { viewModel.setDefaultFormat(it) },
            onToggleFormat = { viewModel.toggleFormatEnabled(it) },
            onDismiss = onDismiss,
        )
    }

    @Suppress("ComposableNaming")
    @Composable
    override fun TopBarActions(): @Composable RowScope.() -> Unit {
        val viewModel: RulingsViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()

        return {
            var formatExpanded by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier.clickable { formatExpanded = true },
            ) {
                Text(
                    text = FormatRulesData.getLabelWithDate(state.selectedFormat),
                    style = MaterialTheme.typography.titleMedium,
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select format",
                    modifier = Modifier.size(24.dp),
                )
            }
            DropdownMenu(
                expanded = formatExpanded,
                onDismissRequest = { formatExpanded = false },
            ) {
                viewModel.dropdownFormats.forEach { (formatKey, labelWithDate) ->
                    DropdownMenuItem(
                        text = { Text(labelWithDate) },
                        onClick = {
                            viewModel.selectFormat(formatKey)
                            formatExpanded = false
                        },
                    )
                }
            }
        }
    }
}
