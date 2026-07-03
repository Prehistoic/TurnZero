package com.turnzero.tools.rules

data class RulesConfig(
    val rulesetId: String,
    val rulesetName: String,
    val setup: List<RuleStep>,
    val winCons: List<WinCondition>,
    val playSequence: List<RuleStep>,
    val keywords: List<Keyword>? = null,
    val pdfUrl: String? = null,
)

data class RuleStep(
    val title: String,
    val summary: String,
    val detail: String,
)

data class WinCondition(
    val title: String,
    val description: String,
)

data class Keyword(
    val name: String,
    val definition: String,
    val category: String? = null,
)

interface RulesProvider {
    fun getConfigs(gameId: String): List<RulesConfig>
}

data class RulesUiState(
    val gameId: String = "",
    val configs: List<RulesConfig> = emptyList(),
    val selectedRulesetIndex: Int = 0,
    val selectedTabIndex: Int = 0,
    val expandedStepIndex: Int? = null,
    val expandedKeywordIndex: Int? = null,
    val keywordSearchQuery: String = "",
    val selectedKeywordCategory: String? = null,
    val showPdf: Boolean = false,
    val isLoading: Boolean = true,
) {
    val currentConfig: RulesConfig?
        get() = configs.getOrNull(selectedRulesetIndex)

    val visibleTabs: List<TabInfo>
        get() {
            val config = currentConfig ?: return emptyList()
            return buildList {
                add(TabInfo("setup", "Setup"))
                add(TabInfo("turn", "Turn"))
                add(TabInfo("victory", "Victory"))
                if (!config.keywords.isNullOrEmpty()) {
                    add(TabInfo("keywords", "Keywords"))
                }
            }
        }

    val filteredKeywords: List<Keyword>
        get() {
            val keywords = currentConfig?.keywords ?: return emptyList()
            val sorted = keywords.sortedBy { it.name.lowercase() }
            val filtered = if (keywordSearchQuery.isBlank()) sorted
            else sorted.filter { it.name.contains(keywordSearchQuery, ignoreCase = true) }
            return if (selectedKeywordCategory != null) {
                filtered.filter { it.category == selectedKeywordCategory }
            } else {
                filtered
            }
        }

    val allKeywordCategories: List<String>
        get() {
            val keywords = currentConfig?.keywords ?: return emptyList()
            return keywords.mapNotNull { it.category }.distinct().sorted()
        }
}

data class TabInfo(
    val key: String,
    val title: String,
)
