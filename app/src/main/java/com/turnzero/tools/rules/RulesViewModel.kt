package com.turnzero.tools.rules

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turnzero.core.data.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RulesViewModel @Inject constructor(
    private val rulesProvider: RulesProvider,
    private val dataStore: DataStore<Preferences>,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val gameId: String = savedStateHandle.get<String>("gameId") ?: ""

    private val _uiState = MutableStateFlow(
        RulesUiState(gameId = gameId)
    )
    val uiState: StateFlow<RulesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val configs = rulesProvider.getConfigs(gameId)
            val savedDefaultIndex = dataStore.data.map { it[UserPreferences.RULES_DEFAULT_RULESET] }
                .first()?.toIntOrNull()
                ?: savedStateHandle.get<Int>("defaultRulesetIndex") ?: 0
            val clampedIndex = savedDefaultIndex.coerceIn(0, (configs.size - 1).coerceAtLeast(0))
            _uiState.update {
                it.copy(
                    configs = configs,
                    isLoading = configs.isEmpty(),
                    selectedRulesetIndex = clampedIndex,
                    defaultRulesetIndex = clampedIndex,
                )
            }
        }
    }

    fun selectRuleset(index: Int) {
        val current = _uiState.value
        val newConfig = current.configs.getOrNull(index)
        val visibleTabs = newConfig?.let { computeVisibleTabCount(it) } ?: 0
        val clampedTab = current.selectedTabIndex.coerceAtMost(visibleTabs - 1).coerceAtLeast(0)
        _uiState.update {
            it.copy(
                selectedRulesetIndex = index,
                selectedTabIndex = clampedTab,
                expandedStepIndex = null,
                expandedKeywordIndex = null,
                keywordSearchQuery = "",
                selectedKeywordCategory = null,
                showPdf = false,
                previousTabIndex = null,
            )
        }
    }

    fun selectTab(index: Int) {
        if (index == _uiState.value.selectedTabIndex) return
        _uiState.update {
            it.copy(
                selectedTabIndex = index,
                expandedStepIndex = null,
                expandedKeywordIndex = null,
            )
        }
    }

    fun onPageChanged(index: Int) {
        _uiState.update {
            it.copy(
                selectedTabIndex = index,
                expandedStepIndex = null,
                expandedKeywordIndex = null,
            )
        }
    }

    fun toggleStep(index: Int) {
        _uiState.update {
            it.copy(
                expandedStepIndex = if (it.expandedStepIndex == index) null else index
            )
        }
    }

    fun toggleKeyword(index: Int) {
        _uiState.update {
            it.copy(
                expandedKeywordIndex = if (it.expandedKeywordIndex == index) null else index
            )
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update {
            it.copy(
                keywordSearchQuery = query,
                expandedKeywordIndex = null,
            )
        }
    }

    fun filterCategory(category: String?) {
        _uiState.update {
            it.copy(
                selectedKeywordCategory = category,
                expandedKeywordIndex = null,
            )
        }
    }

    fun togglePdf() {
        val current = _uiState.value
        if (current.showPdf) {
            _uiState.update {
                it.copy(
                    showPdf = false,
                    selectedPdfAssetPath = null,
                    selectedTabIndex = it.previousTabIndex ?: 0,
                    previousTabIndex = null,
                )
            }
            return
        }
        val pdfs = current.currentConfig?.pdfAssets ?: return
        if (pdfs.size == 1) {
            _uiState.update {
                it.copy(
                    showPdf = true,
                    selectedPdfAssetPath = pdfs.first().assetPath,
                    previousTabIndex = it.selectedTabIndex,
                )
            }
        } else {
            _uiState.update { it.copy(showPdfSelector = true) }
        }
    }

    fun selectPdfAsset(path: String) {
        val current = _uiState.value
        _uiState.update {
            it.copy(
                showPdfSelector = false,
                showPdf = true,
                selectedPdfAssetPath = path,
                previousTabIndex = it.selectedTabIndex,
            )
        }
    }

    fun dismissPdfSelector() {
        _uiState.update { it.copy(showPdfSelector = false) }
    }

    fun toggleOptionsSheet() {
        _uiState.update { it.copy(showOptionsSheet = !it.showOptionsSheet) }
    }

    fun setDefaultRuleset(index: Int) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[UserPreferences.RULES_DEFAULT_RULESET] = index.toString()
            }
        }
        val current = _uiState.value
        val newConfig = current.configs.getOrNull(index)
        val visibleTabs = newConfig?.let { computeVisibleTabCount(it) } ?: 0
        val clampedTab = current.selectedTabIndex.coerceAtMost(visibleTabs - 1).coerceAtLeast(0)
        _uiState.update {
            it.copy(
                defaultRulesetIndex = index,
                selectedRulesetIndex = index,
                selectedTabIndex = clampedTab,
                expandedStepIndex = null,
                expandedKeywordIndex = null,
                keywordSearchQuery = "",
                selectedKeywordCategory = null,
            )
        }
    }

    private fun computeVisibleTabCount(config: RulesConfig): Int {
        var count = 3
        if (!config.deckRules.isNullOrEmpty()) count++
        if (!config.fieldContent.isNullOrEmpty()) count++
        if (!config.keywords.isNullOrEmpty()) count++
        return count
    }
}
