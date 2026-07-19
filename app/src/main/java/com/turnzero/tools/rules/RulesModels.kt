package com.turnzero.tools.rules

import androidx.compose.runtime.Stable

data class PdfAsset(
    val name: String,
    val assetPath: String,
)

data class RulesConfig(
    val rulesetId: String,
    val rulesetName: String,
    val setup: List<RuleStep>,
    val victoryRules: List<InfoCard>,
    val playSequence: List<RuleStep>,
    val keywords: List<Keyword>? = null,
    val pdfAssets: List<PdfAsset>? = null,
    val fieldContent: List<ContentBlock>? = null,
    val deckRules: List<InfoCard>? = null,
)

data class RuleStep(
    val title: String,
    val summary: String,
    val contents: List<ContentBlock>,
)

data class InfoCard(
    val title: String,
    val contents: List<ContentBlock>,
)

data class Keyword(
    val name: String,
    val contents: List<ContentBlock>,
    val category: String? = null,
)

sealed interface ContentBlock {
    data class Paragraph(val text: String) : ContentBlock
    data class SubHeading(val text: String) : ContentBlock
    data class BulletList(val items: List<String>) : ContentBlock
    data class Image(val uri: String) : ContentBlock
}

interface RulesProvider {
    fun getConfigs(gameId: String): List<RulesConfig>
}

@Stable
data class RulesUiState(
    val gameId: String = "",
    val configs: List<RulesConfig> = emptyList(),
    val selectedRulesetIndex: Int = 0,
    val selectedRulesetId: String = "",
    val selectedTabIndex: Int = 0,
    val expandedStepIndex: Int? = null,
    val expandedKeywordIndex: Int? = null,
    val keywordSearchQuery: String = "",
    val selectedKeywordCategory: String? = null,
    val showPdf: Boolean = false,
    val showPdfSelector: Boolean = false,
    val selectedPdfAssetPath: String? = null,
    val previousTabIndex: Int? = null,
    val enabledRulesets: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val showOptionsSheet: Boolean = false,
    val defaultRulesetIndex: Int = 0,
    val defaultRulesetId: String = "",
) {
    val currentConfig: RulesConfig?
        get() = configs.getOrNull(selectedRulesetIndex)

    val visibleConfigs: List<RulesConfig>
        get() {
            if (enabledRulesets.isEmpty()) return configs
            return configs.filter { it.rulesetId in enabledRulesets }
        }

    val hasPdf: Boolean
        get() = !currentConfig?.pdfAssets.isNullOrEmpty()

    val isSinglePdf: Boolean
        get() = currentConfig?.pdfAssets?.size == 1

    val visibleTabs: List<TabInfo>
        get() {
            val config = currentConfig ?: return emptyList()
            return buildList {
                add(TabInfo("setup", "Setup"))
                add(TabInfo("turn", "Turn"))
                add(TabInfo("victory", "Victory"))
                if (!config.deckRules.isNullOrEmpty()) {
                    add(TabInfo("deck", "Deck"))
                }
                if (!config.fieldContent.isNullOrEmpty()) {
                    add(TabInfo("field", "Field"))
                }
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
