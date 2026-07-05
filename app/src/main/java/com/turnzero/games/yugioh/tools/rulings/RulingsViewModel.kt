package com.turnzero.games.yugioh.tools.rulings

import android.util.Log
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

private const val TAG = "TZ/RulingsVM"

data class RulingsUiState(
    val formats: List<String> = emptyList(),
    val enabledFormats: List<String> = emptyList(),
    val disabledFormats: List<String> = emptyList(),
    val selectedFormat: String = "Goat",
    val defaultFormat: String = "Goat",
    val generalRules: List<FormatRule> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<CardSearchResult> = emptyList(),
    val selectedCard: CardRulingsDetail? = null,
    val isSearching: Boolean = false,
    val showOcr: Boolean = false,
    val showOcrCardNotFound: String? = null,
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
            val rawEnabled = dataStore.data.first()[UserPreferences.RULINGS_ENABLED_FORMATS]
                ?: FormatRulesData.defaultEnabledKeys
            val storedEnabled = if ("Modern" in rawEnabled) {
                (rawEnabled - "Modern") + "Base"
            } else {
                rawEnabled
            }
            val enabled = FormatRulesData.sortByChronological(allFormats.filter { it in storedEnabled })
            val disabled = FormatRulesData.sortByChronological(allFormats.filter { it !in storedEnabled })

            val storedDefault = dataStore.data.first()[UserPreferences.RULINGS_DEFAULT_FORMAT]
            val defaultFormat = if (storedDefault != null && storedDefault in enabled) {
                storedDefault
            } else {
                if ("Goat" in enabled) "Goat" else enabled.firstOrNull() ?: "Goat"
            }

            val rules = FormatRulesData.getMeta(defaultFormat)?.rules ?: emptyList()
            _uiState.update {
                it.copy(
                    formats = allFormats,
                    enabledFormats = enabled,
                    disabledFormats = disabled,
                    selectedFormat = defaultFormat,
                    defaultFormat = defaultFormat,
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
        val currentCardId = _uiState.value.selectedCard?.id
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
        if (currentCardId != null) {
            viewModelScope.launch {
                val detail = repository.getCardRulings(currentCardId, format)
                _uiState.update { it.copy(selectedCard = detail) }
            }
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
            _uiState.update { it.copy(selectedCard = detail, searchQuery = "", searchResults = emptyList(), isSearching = false) }
        }
    }

    fun clearSelectedCard() {
        _uiState.update { it.copy(selectedCard = null) }
    }

    fun showOcr() { _uiState.update { it.copy(showOcr = true) } }
    fun hideOcr() { _uiState.update { it.copy(showOcr = false) } }

    fun showOptionsSheet() { _uiState.update { it.copy(showOptionsSheet = true) } }
    fun hideOptionsSheet() { _uiState.update { it.copy(showOptionsSheet = false) } }

    fun setDefaultFormat(formatKey: String) {
        _uiState.update { it.copy(defaultFormat = formatKey) }
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[UserPreferences.RULINGS_DEFAULT_FORMAT] = formatKey
            }
        }
        selectFormat(formatKey)
    }

    fun toggleFormatEnabled(formatKey: String) {
        val currentEnabled = _uiState.value.enabledFormats.toMutableSet()
        if (formatKey in currentEnabled) currentEnabled.remove(formatKey)
        else currentEnabled.add(formatKey)
        val sorted = FormatRulesData.sortByChronological(currentEnabled.toList())
        val allFormats = _uiState.value.formats
        val disabled = FormatRulesData.sortByChronological(allFormats.filter { it !in currentEnabled })

        var newSelected = _uiState.value.selectedFormat
        if (newSelected !in currentEnabled) {
            newSelected = sorted.firstOrNull() ?: newSelected
        }

        var newDefault = _uiState.value.defaultFormat
        if (newDefault !in currentEnabled) {
            newDefault = sorted.firstOrNull() ?: newDefault
        }

        _uiState.update {
            it.copy(
                enabledFormats = sorted,
                disabledFormats = disabled,
                selectedFormat = newSelected,
                defaultFormat = newDefault,
            )
        }
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[UserPreferences.RULINGS_ENABLED_FORMATS] = currentEnabled
                prefs[UserPreferences.RULINGS_DEFAULT_FORMAT] = newDefault
            }
        }
    }

    fun onOcrTextRecognized(text: String) {
        hideOcr()
        viewModelScope.launch {
            Log.d(TAG, "OCR raw text (${text.length} chars): ${text.take(200)}")
            val cleanLines = text.lines()
                .filter { it.isNotBlank() }
                .map { it.replace(Regex("[^\\p{L}0-9\\s\\-']"), "").trim() }
                .filter { it.length > 3 }
            Log.d(TAG, "OCR clean lines: $cleanLines")
            for (line in cleanLines) {
                Log.d(TAG, "OCR searching for: '$line'")
                val result = repository.searchCards(line, _uiState.value.selectedFormat, 1)
                if (result.isNotEmpty()) {
                    Log.d(TAG, "OCR match found: ${result[0].name} (id=${result[0].id})")
                    val detail = repository.getCardRulings(result[0].id, _uiState.value.selectedFormat)
                    _uiState.update { it.copy(selectedCard = detail, searchQuery = "", searchResults = emptyList(), isSearching = false) }
                    return@launch
                }
            }
            val fallbackQuery = text.take(50).trim()
            Log.w(TAG, "OCR no match found, fallback query: '$fallbackQuery'")
            _uiState.update { it.copy(showOcrCardNotFound = fallbackQuery) }
        }
    }

    fun dismissOcrCardNotFound() {
        _uiState.update { it.copy(showOcrCardNotFound = null) }
    }

    fun toggleGeneralRule(index: Int) {
        _uiState.update {
            it.copy(expandedRuleIndex = if (it.expandedRuleIndex == index) null else index)
        }
    }
}
