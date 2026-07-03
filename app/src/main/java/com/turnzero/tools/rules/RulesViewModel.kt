package com.turnzero.tools.rules

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RulesViewModel @Inject constructor(
    private val rulesProvider: RulesProvider,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val gameId: String = savedStateHandle.get<String>("gameId") ?: ""

    private val _uiState = MutableStateFlow(RulesUiState(gameId = gameId))
    val uiState: StateFlow<RulesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val configs = rulesProvider.getConfigs(gameId)
            _uiState.update { it.copy(configs = configs, isLoading = configs.isEmpty()) }
        }
    }

    fun selectRuleset(index: Int) {
        _uiState.update {
            it.copy(
                selectedRulesetIndex = index,
                expandedStepIndex = null,
                expandedKeywordIndex = null,
                keywordSearchQuery = "",
                selectedKeywordCategory = null,
            )
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTabIndex = index) }
    }

    fun onPageChanged(index: Int) {
        _uiState.update { it.copy(selectedTabIndex = index) }
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
}
