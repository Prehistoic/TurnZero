package com.turnzero.tools.rules

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.ui.graphics.vector.ImageVector
import com.turnzero.core.ui.components.LayoutMode

/** The sections of the rules tool; [id] is the value persisted for the last opened section. */
enum class RuleSubTool(val id: String, val title: String, val icon: ImageVector) {
    SETUP("setup", "Setup", Icons.Filled.Build),
    TURN("turn", "Turn Structure", Icons.Filled.Loop),
    WIN("win", "Win Condition", Icons.Filled.EmojiEvents),
    KEYWORDS("keywords", "Keywords", Icons.Filled.TextFields),
    RULEBOOK("rulebook", "Rulebook", Icons.AutoMirrored.Filled.MenuBook),
}

data class RulesToolUiState(
    val gameId: String = "",
    val rules: GameRules? = null,
    val selection: Selection = Selection(),
    val draftSelection: Selection? = null, // non-null while the specificity editor is open
    val showSpecificityDialog: Boolean = false, // centered-dialog variant (first open + pill)
    val subTool: RuleSubTool? = null, // null = main panel
    val layout: LayoutMode = LayoutMode.GRID,
    val showLayoutSheet: Boolean = false,
    val turnPhaseIndex: Int? = null,
    val turnStepIndex: Int? = null,
    val keywordQuery: String = "",
    val keywordCategory: String? = null,
    val expandedKeywordIndex: Int? = null,
    val showPdfSelector: Boolean = false,
    val selectedRulebookId: String? = null,
    val pdfInitialPage: Int = 0, // 0-based viewer page
    val isLoading: Boolean = true,
) {
    val hasSpecificities: Boolean get() = !rules?.manifest?.specificities.isNullOrEmpty()

    /** Compact top-bar summary of the confirmed [selection], in manifest specificity order. */
    val specificitySummary: String?
        get() {
            val manifest = rules?.manifest ?: return null
            val parts = manifest.specificities.mapNotNull { spec ->
                when (spec.type) {
                    SpecificityType.SINGLE -> selection.single[spec.id]
                        ?.let { id -> spec.options.firstOrNull { it.id == id }?.name }
                    SpecificityType.NUMBER -> selection.number[spec.id]?.let { "${it}P" }
                    SpecificityType.MULTI -> selection.multi[spec.id].orEmpty()
                        .takeIf { it.isNotEmpty() }
                        ?.let { "+${it.size} ext" }
                }
            }
            return parts.takeIf { it.isNotEmpty() }?.joinToString(" · ")
        }

    val setupSteps: List<RuleItem>
        get() = rules?.setup?.steps.orEmpty().filter { matches(it.`when`, selection) }

    val turnPhases: List<Phase>
        get() = rules?.turn?.phases.orEmpty()
            .filter { matches(it.`when`, selection) }
            .mapNotNull { phase ->
                val steps = phase.steps.filter { matches(it.`when`, selection) }
                if (steps.isEmpty()) null else phase.copy(steps = steps)
            }

    val winConditions: List<RuleItem>
        get() = rules?.win?.winConditions.orEmpty().filter { matches(it.`when`, selection) }

    val keywords: List<KeywordItem>
        get() = rules?.keywords?.keywords.orEmpty().filter { matches(it.`when`, selection) }

    val filteredKeywords: List<KeywordItem>
        get() = keywords
            .sortedBy { it.name.lowercase() }
            .filter { it.name.contains(keywordQuery, ignoreCase = true) }
            .filter { keywordCategory == null || it.category == keywordCategory }

    val keywordCategories: List<String>
        get() = keywords.mapNotNull { it.category }.distinct().sorted()

    val matchingRulebooks: List<RulebookDef>
        get() = rules?.manifest?.rulebooks.orEmpty().filter { matches(it.`when`, selection) }
}
