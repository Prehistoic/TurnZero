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
        _uiState.update {
            it.copy(
                selectedRulesetIndex = index,
                selectedTabIndex = 0,
                expandedStepIndex = null,
                expandedKeywordIndex = null,
                keywordSearchQuery = "",
                selectedKeywordCategory = null,
                showPdf = false,
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
        _uiState.update { it.copy(showPdf = !it.showPdf) }
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
        _uiState.update {
            it.copy(
                defaultRulesetIndex = index,
                selectedRulesetIndex = index,
                selectedTabIndex = 0,
                expandedStepIndex = null,
                expandedKeywordIndex = null,
                keywordSearchQuery = "",
                selectedKeywordCategory = null,
            )
        }
    }
}
