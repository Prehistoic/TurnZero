package com.turnzero.games.yugioh.tools.rulings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turnzero.core.ui.components.TZEmptyState

@Composable
fun RulingsScreen(
    onBack: () -> Unit,
    viewModel: RulingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    if (state.showOcr) {
        RulingsOcrScreen(
            selectedFormat = state.selectedFormat,
            onTextRecognized = { viewModel.onOcrTextRecognized(it) },
            onDismiss = { viewModel.hideOcr() },
        )
        return
    }

    if (state.showOcrCardNotFound != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissOcrCardNotFound() },
            title = { Text("No Card Found") },
            text = {
                Text("No card found matching \"${state.showOcrCardNotFound}\". Try again!")
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissOcrCardNotFound() }) {
                    Text("OK")
                }
            },
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
            RulingsSearchBar(
                query = state.searchQuery,
                onQueryChange = { viewModel.setSearchQuery(it) },
                onOcrClick = { viewModel.showOcr() },
                enabled = !state.isLoading,
            )

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
                state.isSearching || state.searchQuery.isNotBlank() -> {
                    if (state.searchResults.isEmpty()) {
                        TZEmptyState(message = "No cards found for \"${state.searchQuery}\"")
                    } else {
                        RulingsSearchResults(
                            results = state.searchResults,
                            onCardClick = { result ->
                                focusManager.clearFocus()
                                viewModel.selectCard(result.id)
                            },
                        )
                    }
                }
                state.selectedCard != null -> {
                val card = state.selectedCard!!
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = {
                        viewModel.clearSelectedCard()
                        viewModel.setSearchQuery("")
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Back to Rules")
                    }
                }
                RulingsCardDetail(card = card)
            }
                else -> {
                RulingsGeneralRules(
                    rules = state.generalRules,
                    expandedRuleIndex = state.expandedRuleIndex,
                    onToggleRule = { viewModel.toggleGeneralRule(it) },
                )
            }
        }
    }
}

@Composable
private fun RulingsSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onOcrClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                enabled = enabled,
                placeholder = { Text("Search cards...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = {
                            onQueryChange("")
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                ),
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilledIconButton(
                onClick = onOcrClick,
                enabled = enabled,
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Scan card with camera", modifier = Modifier.size(20.dp))
            }

        }
    }
}
