package com.turnzero.tools.rules

data class RulesConfig(
    val rulesetId: String,
    val rulesetName: String,
    val setup: List<RuleStep>,
    val winCons: List<WinCondition>,
    val playSequence: List<RuleStep>,
    val keywords: List<Keyword>? = null,
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
    val isLoading: Boolean = true,
) {
    val currentConfig: RulesConfig?
        get() = configs.getOrNull(selectedRulesetIndex)

    val visibleTabs: List<TabInfo>
        get() {
            val config = currentConfig ?: return emptyList()
            return buildList {
                add(TabInfo("setup", "Setup"))
                add(TabInfo("wincons", "WinCons"))
                add(TabInfo("sequence", "Sequence"))
                if (!config.keywords.isNullOrEmpty()) {
                    add(TabInfo("keywords", "Keywords"))
                }
            }
        }

    val filteredKeywords: List<Keyword>
        get() {
            val keywords = currentConfig?.keywords ?: return emptyList()
            val sorted = keywords.sortedBy { it.name.lowercase() }
            if (keywordSearchQuery.isBlank()) return sorted
            return sorted.filter { it.name.contains(keywordSearchQuery, ignoreCase = true) }
        }
}

data class TabInfo(
    val key: String,
    val title: String,
)
