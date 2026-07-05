package com.turnzero.games.yugioh.tools.rulings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turnzero.core.data.UserPreferences
import com.turnzero.games.yugioh.tools.rulings.data.CardRulingsDetail
import com.turnzero.games.yugioh.tools.rulings.data.CardSearchResult
import com.turnzero.games.yugioh.tools.rulings.data.FormatRule
import com.turnzero.games.yugioh.tools.rulings.data.FormatRulesData
import com.turnzero.games.yugioh.tools.rulings.data.RulingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RulingsUiState(
    val formats: List<String> = emptyList(),
    val enabledFormats: List<String> = emptyList(),
    val disabledFormats: List<String> = emptyList(),
    val selectedFormat: String = "Goat",
    val generalRules: List<FormatRule> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<CardSearchResult> = emptyList(),
    val selectedCard: CardRulingsDetail? = null,
    val isSearching: Boolean = false,
    val showOcr: Boolean = false,
    val showOptionsSheet: Boolean = false,
    val expandedRuleIndex: Int? = null,
    val isLoading: Boolean = true,
)

@HiltViewModel
class RulingsViewModel @Inject constructor(
    private val repository: RulingsRepository,
    private val dataStore: DataStore<Preferences>,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RulingsUiState())
    val uiState: StateFlow<RulingsUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            val allFormats = repository.getAllFormats()
            val storedEnabled = dataStore.data.first()[UserPreferences.RULINGS_ENABLED_FORMATS]
                ?: FormatRulesData.defaultEnabledKeys
            val enabled = FormatRulesData.sortByChronological(allFormats.filter { it in storedEnabled })
            val disabled = FormatRulesData.sortByChronological(allFormats.filter { it !in storedEnabled })
            val initialFormat = if ("Goat" in enabled) "Goat" else enabled.firstOrNull() ?: "Goat"
            val rules = FormatRulesData.getMeta(initialFormat)?.rules ?: emptyList()
            _uiState.update {
                it.copy(
                    formats = allFormats,
                    enabledFormats = enabled,
                    disabledFormats = disabled,
                    selectedFormat = initialFormat,
                    generalRules = rules,
                    isLoading = allFormats.isEmpty(),
                )
            }
        }
    }

    val dropdownFormats: List<Pair<String, String>>
        get() = _uiState.value.enabledFormats.map { it to FormatRulesData.getLabelWithDate(it) }

    val allFormatsWithStatus: List<Pair<String, Boolean>>
        get() {
            val enabledSet = _uiState.value.enabledFormats.toSet()
            return FormatRulesData.sortByChronological(_uiState.value.formats)
                .map { it to (it in enabledSet) }
        }

    fun selectFormat(format: String) {
        searchJob?.cancel()
        val rules = FormatRulesData.getMeta(format)?.rules ?: emptyList()
        _uiState.update {
            it.copy(
                selectedFormat = format,
                generalRules = rules,
                searchQuery = "",
                searchResults = emptyList(),
                selectedCard = null,
            )
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }
        _uiState.update { it.copy(isSearching = true) }
        searchJob = viewModelScope.launch {
            delay(300)
            val format = _uiState.value.selectedFormat
            val results = repository.searchCards(query, format, 20)
            _uiState.update { it.copy(searchResults = results, isSearching = true) }
        }
    }

    fun selectCard(cardId: Int) {
        viewModelScope.launch {
            val detail = repository.getCardRulings(cardId, _uiState.value.selectedFormat)
            _uiState.update { it.copy(selectedCard = detail) }
        }
    }

    fun clearSelectedCard() {
        _uiState.update { it.copy(selectedCard = null) }
    }

    fun showOcr() { _uiState.update { it.copy(showOcr = true) } }
    fun hideOcr() { _uiState.update { it.copy(showOcr = false) } }

    fun showOptionsSheet() { _uiState.update { it.copy(showOptionsSheet = true) } }
    fun hideOptionsSheet() { _uiState.update { it.copy(showOptionsSheet = false) } }

    fun toggleFormatEnabled(formatKey: String) {
        val currentEnabled = _uiState.value.enabledFormats.toMutableSet()
        if (formatKey in currentEnabled) currentEnabled.remove(formatKey)
        else currentEnabled.add(formatKey)
        val sorted = FormatRulesData.sortByChronological(currentEnabled.toList())
        val allFormats = _uiState.value.formats
        val disabled = FormatRulesData.sortByChronological(allFormats.filter { it !in currentEnabled })
        val newSelected = if (_uiState.value.selectedFormat !in currentEnabled)
            sorted.firstOrNull() ?: _uiState.value.selectedFormat
        else _uiState.value.selectedFormat
        _uiState.update {
            it.copy(enabledFormats = sorted, disabledFormats = disabled, selectedFormat = newSelected)
        }
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[UserPreferences.RULINGS_ENABLED_FORMATS] = currentEnabled
            }
        }
    }

    fun onOcrTextRecognized(text: String) {
        hideOcr()
        viewModelScope.launch {
            val cleanText = text.lines()
                .filter { it.isNotBlank() }
                .map { it.replace(Regex("[^a-zA-Z0-9\\s\\-']"), "").trim() }
                .filter { it.length > 3 }
                .firstOrNull() ?: text.take(50).trim()
            val result = repository.searchCards(cleanText, _uiState.value.selectedFormat, 1)
            if (result.isNotEmpty()) {
                val detail = repository.getCardRulings(result[0].id, _uiState.value.selectedFormat)
                _uiState.update { it.copy(selectedCard = detail) }
            }
        }
    }

    fun toggleGeneralRule(index: Int) {
        _uiState.update {
            it.copy(expandedRuleIndex = if (it.expandedRuleIndex == index) null else index)
        }
    }
}
