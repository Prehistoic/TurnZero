package com.turnzero.tools.rules

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.turnzero.tools.rules.MarkdownContent

@Composable
fun RulesKeywordsTab(
    keywords: List<Keyword>,
    searchQuery: String,
    selectedCategory: String?,
    allCategories: List<String>,
    expandedIndex: Int?,
    onToggle: (Int) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onFilterCategory: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            placeholder = { Text("Search keywords...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            singleLine = true,
        )

        if (selectedCategory != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AssistChip(
                    onClick = { onFilterCategory(null) },
                    label = { Text(selectedCategory) },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear filter",
                            modifier = Modifier.size(16.dp),
                        )
                    },
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${keywords.size} result${if (keywords.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(8.dp))
        }

        if (keywords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (searchQuery.isNotBlank() || selectedCategory != null) "No matching keywords" else "No keywords available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (selectedCategory == null && allCategories.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            allCategories.forEach { category ->
                                SuggestionChip(
                                    onClick = { onFilterCategory(category) },
                                    label = { Text(category) },
                                )
                            }
                        }
                    }
                }
                itemsIndexed(keywords) { index, keyword ->
                    val isExpanded = expandedIndex == index
                    KeywordCard(
                        keyword = keyword,
                        isExpanded = isExpanded,
                        onClick = { onToggle(index) },
                        onCategoryClick = if (selectedCategory == null) {
                            { category -> onFilterCategory(category) }
                        } else null,
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun KeywordCard(
    keyword: Keyword,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onCategoryClick: ((String) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        border = if (isExpanded) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        },
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = keyword.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                if (keyword.category != null) {
                    SuggestionChip(
                        onClick = {
                            onCategoryClick?.invoke(keyword.category)
                        },
                        label = {
                            Text(
                                text = keyword.category,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }

            if (isExpanded) {
                Spacer(Modifier.height(8.dp))
                MarkdownContent(
                    content = keyword.definition,
                )
            }
        }
    }
}
