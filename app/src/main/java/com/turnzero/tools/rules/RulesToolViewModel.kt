package com.turnzero.tools.rules

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turnzero.core.data.UserPreferences
import com.turnzero.core.ui.components.LayoutMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RulesToolViewModel @Inject constructor(
    private val repository: RulesRepository,
    private val dataStore: DataStore<Preferences>,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val gameId: String = savedStateHandle.get<String>("gameId").orEmpty()

    private val _uiState = MutableStateFlow(RulesToolUiState(gameId = gameId))
    val uiState: StateFlow<RulesToolUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val rules = repository.getRules(gameId)
            val prefs = dataStore.data.first()
            val manifest = rules?.manifest
            val defaults = manifest?.let { defaultSelection(it) } ?: Selection()
            val needsDialog = !manifest?.specificities.isNullOrEmpty() &&
                prefs[UserPreferences.rulesConfiguredKey(gameId)] != true
            val selection = when {
                manifest == null -> Selection()
                needsDialog -> defaults
                else -> restoreSelection(manifest, prefs, defaults)
            }
            _uiState.update {
                it.copy(
                    rules = rules,
                    selection = selection,
                    draftSelection = selection.takeIf { needsDialog },
                    showSpecificityDialog = needsDialog,
                    subTool = rules?.let { restoreSubTool(prefs) },
                    layout = restoreLayout(prefs),
                    isLoading = false,
                )
            }
        }
    }

    fun openSubTool(subTool: RuleSubTool) {
        _uiState.update { state ->
            val rulebooks = state.matchingRulebooks
            val reset = state.copy(
                subTool = subTool,
                turnPhaseIndex = null,
                turnStepIndex = null,
                keywordQuery = "",
                keywordCategory = null,
                expandedKeywordIndex = null,
                showPdfSelector = false,
                selectedRulebookId = null,
                pdfInitialPage = 0,
            )
            when {
                subTool != RuleSubTool.RULEBOOK -> reset
                rulebooks.size == 1 -> reset.copy(selectedRulebookId = rulebooks.single().id)
                rulebooks.size > 1 -> reset.copy(showPdfSelector = true)
                else -> reset
            }
        }
        persistSubTool(subTool.id)
    }

    fun closeSubTool() {
        _uiState.update { it.copy(subTool = null) }
        viewModelScope.launch {
            dataStore.edit { prefs -> prefs.remove(UserPreferences.rulesSubToolKey(gameId)) }
        }
    }

    fun openSpecificityEditor() {
        _uiState.update { it.copy(draftSelection = it.draftSelection ?: it.selection) }
    }

    fun openSpecificityDialog() {
        openSpecificityEditor()
        _uiState.update { it.copy(showSpecificityDialog = true) }
    }

    fun dismissSpecificityDialog() {
        _uiState.update { it.copy(draftSelection = null, showSpecificityDialog = false) }
    }

    fun confirmSpecificities() {
        val current = _uiState.value
        val draft = current.draftSelection ?: return
        val manifest = current.rules?.manifest
        _uiState.update {
            it.copy(
                selection = draft,
                draftSelection = null,
                showSpecificityDialog = false,
            ).resetForSelection()
        }
        viewModelScope.launch {
            dataStore.edit { prefs ->
                manifest?.specificities?.forEach { spec ->
                    when (spec.type) {
                        SpecificityType.SINGLE -> draft.single[spec.id]?.let { value ->
                            prefs[UserPreferences.rulesSpecStringKey(gameId, spec.id)] = value
                        }
                        SpecificityType.NUMBER -> draft.number[spec.id]?.let { value ->
                            prefs[UserPreferences.rulesSpecIntKey(gameId, spec.id)] = value
                        }
                        SpecificityType.MULTI -> draft.multi[spec.id]?.let { value ->
                            prefs[UserPreferences.rulesSpecSetKey(gameId, spec.id)] = value
                        }
                    }
                }
                prefs[UserPreferences.rulesConfiguredKey(gameId)] = true
            }
        }
    }

    fun selectSpecificityOption(specId: String, optionId: String) {
        _uiState.update { state ->
            val manifest = state.rules?.manifest ?: return@update state
            val draft = state.draftSelection ?: state.selection
            val spec = manifest.specificities.firstOrNull { it.id == specId } ?: return@update state
            val toggleOff = spec.type == SpecificityType.MULTI &&
                optionId in draft.multi[specId].orEmpty()
            val updated = if (toggleOff) {
                deselectOption(manifest, draft, specId, optionId)
            } else {
                selectOption(manifest, draft, specId, optionId)
            }
            state.copy(draftSelection = updated)
        }
    }

    fun setSpecificityNumber(specId: String, value: Int) {
        _uiState.update { state ->
            val manifest = state.rules?.manifest ?: return@update state
            val draft = state.draftSelection ?: state.selection
            state.copy(draftSelection = withNumber(manifest, draft, specId, value))
        }
    }

    fun setLayout(mode: LayoutMode) {
        _uiState.update { it.copy(layout = mode) }
        viewModelScope.launch {
            dataStore.edit { prefs -> prefs[UserPreferences.RULES_LAYOUT] = mode.name }
        }
    }

    fun showLayoutSheet() {
        _uiState.update { it.copy(showLayoutSheet = true) }
    }

    fun dismissLayoutSheet() {
        _uiState.update { it.copy(showLayoutSheet = false) }
    }

    fun openPhase(index: Int) {
        _uiState.update { state ->
            val steps = state.turnPhases.getOrNull(index)?.steps ?: return@update state
            // A phase with a single step has no meaningful step list, so open its detail directly.
            val stepIndex = if (steps.size == 1) 0 else null
            state.copy(turnPhaseIndex = index, turnStepIndex = stepIndex)
        }
    }

    fun closePhase() {
        _uiState.update { it.copy(turnPhaseIndex = null, turnStepIndex = null) }
    }

    fun openStep(index: Int) {
        _uiState.update { it.copy(turnStepIndex = index) }
    }

    fun closeStep() {
        _uiState.update { it.copy(turnStepIndex = null) }
    }

    /**
     * Pops one drill-down level, mirroring the on-screen hierarchy: step detail → step list (for
     * multi-step phases) → phase list → main panel.
     */
    fun back() {
        val state = _uiState.value
        if (state.subTool == RuleSubTool.TURN) {
            val phase = state.turnPhaseIndex?.let { state.turnPhases.getOrNull(it) }
            if (state.turnStepIndex != null && phase != null && phase.steps.size > 1) {
                closeStep()
                return
            }
            if (state.turnPhaseIndex != null) {
                closePhase()
                return
            }
        }
        closeSubTool()
    }

    fun nextStep() {
        _uiState.update { state ->
            val phases = state.turnPhases
            val phaseIndex = state.turnPhaseIndex ?: return@update state
            val stepIndex = state.turnStepIndex ?: return@update state
            val phase = phases.getOrNull(phaseIndex) ?: return@update state
            when {
                stepIndex + 1 < phase.steps.size ->
                    state.copy(turnStepIndex = stepIndex + 1)
                phaseIndex + 1 < phases.size ->
                    state.copy(turnPhaseIndex = phaseIndex + 1, turnStepIndex = 0)
                else -> state
            }
        }
    }

    fun previousStep() {
        _uiState.update { state ->
            val phases = state.turnPhases
            val phaseIndex = state.turnPhaseIndex ?: return@update state
            val stepIndex = state.turnStepIndex ?: return@update state
            when {
                stepIndex > 0 -> state.copy(turnStepIndex = stepIndex - 1)
                phaseIndex > 0 -> {
                    // turnPhases drops empty phases, so a previous phase always has steps.
                    val previous = phases[phaseIndex - 1]
                    state.copy(
                        turnPhaseIndex = phaseIndex - 1,
                        turnStepIndex = previous.steps.lastIndex,
                    )
                }
                else -> state
            }
        }
    }

    fun setKeywordQuery(query: String) {
        _uiState.update { it.copy(keywordQuery = query, expandedKeywordIndex = null) }
    }

    fun filterKeywordCategory(category: String?) {
        _uiState.update { it.copy(keywordCategory = category, expandedKeywordIndex = null) }
    }

    fun toggleKeyword(index: Int) {
        _uiState.update {
            it.copy(expandedKeywordIndex = if (it.expandedKeywordIndex == index) null else index)
        }
    }

    fun selectRulebook(rulebookId: String) {
        _uiState.update {
            it.copy(
                showPdfSelector = false,
                selectedRulebookId = rulebookId,
                pdfInitialPage = 0,
            )
        }
    }

    /** Cancelling the picker returns to the rules main panel instead of re-asking forever. */
    fun dismissPdfSelector() {
        _uiState.update {
            it.copy(showPdfSelector = false, subTool = null, pdfInitialPage = 0)
        }
        viewModelScope.launch {
            dataStore.edit { prefs -> prefs.remove(UserPreferences.rulesSubToolKey(gameId)) }
        }
    }

    fun jumpToPages() {
        val state = _uiState.value
        val rules = state.rules ?: return
        val pages = when (state.subTool) {
            RuleSubTool.SETUP -> rules.setup.pages
            RuleSubTool.TURN -> rules.turn.pages
            RuleSubTool.WIN -> rules.win.pages
            RuleSubTool.KEYWORDS -> rules.keywords.pages
            RuleSubTool.RULEBOOK, null -> return
        }
        val ref = pages.firstOrNull { matches(it.`when`, state.selection) }
        val rulebook = ref?.let { pageRef ->
            rules.manifest.rulebooks.firstOrNull { it.id == pageRef.rulebook }
        }
        if (ref == null || rulebook == null || ref.pages.isEmpty()) {
            openSubTool(RuleSubTool.RULEBOOK)
            return
        }
        _uiState.update {
            it.copy(
                subTool = RuleSubTool.RULEBOOK,
                showPdfSelector = false,
                selectedRulebookId = ref.rulebook,
                pdfInitialPage = ref.pages.first() - 1,
            )
        }
        persistSubTool(RuleSubTool.RULEBOOK.id)
    }

    private fun persistSubTool(id: String) {
        viewModelScope.launch {
            dataStore.edit { prefs -> prefs[UserPreferences.rulesSubToolKey(gameId)] = id }
        }
    }

    /**
     * Drops every drill-down and filter that the new selection may have invalidated, and resolves
     * the rulebook again, so the open sub-tool looks exactly as it would on a fresh open.
     */
    private fun RulesToolUiState.resetForSelection(): RulesToolUiState {
        val rulebooks = matchingRulebooks
        val keptRulebook = selectedRulebookId?.takeIf { id -> rulebooks.any { it.id == id } }
        return copy(
            turnPhaseIndex = null,
            turnStepIndex = null,
            keywordQuery = "",
            keywordCategory = null,
            expandedKeywordIndex = null,
            selectedRulebookId = keptRulebook ?: rulebooks.singleOrNull()?.id,
            showPdfSelector = keptRulebook == null && rulebooks.size > 1,
            pdfInitialPage = 0,
        )
    }

    private fun restoreLayout(prefs: Preferences): LayoutMode = try {
        LayoutMode.valueOf(prefs[UserPreferences.RULES_LAYOUT] ?: LayoutMode.GRID.name)
    } catch (e: Exception) {
        Log.w(TAG, "Invalid RULES_LAYOUT preference", e)
        LayoutMode.GRID
    }

    private fun restoreSubTool(prefs: Preferences): RuleSubTool? {
        val savedId = prefs[UserPreferences.rulesSubToolKey(gameId)] ?: return null
        return RuleSubTool.entries.firstOrNull { it.id == savedId }
    }

    private fun restoreSelection(
        manifest: RulesManifest,
        prefs: Preferences,
        defaults: Selection,
    ): Selection {
        val single = mutableMapOf<String, String>()
        val number = mutableMapOf<String, Int>()
        val multi = mutableMapOf<String, Set<String>>()
        manifest.specificities.forEach { spec ->
            when (spec.type) {
                SpecificityType.SINGLE -> single[spec.id] =
                    prefs[UserPreferences.rulesSpecStringKey(gameId, spec.id)]
                        ?: defaults.single[spec.id].orEmpty()
                SpecificityType.NUMBER -> number[spec.id] =
                    prefs[UserPreferences.rulesSpecIntKey(gameId, spec.id)]
                        ?: defaults.number[spec.id] ?: 0
                SpecificityType.MULTI -> multi[spec.id] =
                    prefs[UserPreferences.rulesSpecSetKey(gameId, spec.id)]
                        ?: defaults.multi[spec.id].orEmpty()
            }
        }
        return Selection(single = single, number = number, multi = multi)
    }

    private companion object {
        const val TAG = "TZ/Rules"
    }
}
