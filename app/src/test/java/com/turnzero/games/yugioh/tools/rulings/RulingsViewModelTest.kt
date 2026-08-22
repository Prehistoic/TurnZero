package com.turnzero.games.yugioh.tools.rulings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesOf
import com.turnzero.core.data.UserPreferences
import com.turnzero.games.yugioh.tools.rulings.data.CardRulingsDetail
import com.turnzero.games.yugioh.tools.rulings.data.CardSearchResult
import com.turnzero.games.yugioh.tools.rulings.data.RulingsRepository
import com.turnzero.TZViewModelTest
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RulingsViewModelTest : TZViewModelTest() {

    private val repository: RulingsRepository = mockk()
    private val dataStore: DataStore<Preferences> = mockk(relaxed = true)

    @Before
    fun setup() {
        coEvery { dataStore.data } returns flowOf(emptyPreferences())
    }

    @Test
    fun `init loads formats and enables all formats by default`() = runTest {
        coEvery { repository.getAllFormats() } returns listOf("Goat", "Edison", "Stein")
        val vm = RulingsViewModel(repository, dataStore)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.first()
        assertEquals("Goat", state.selectedFormat)
        assertEquals(listOf("Goat", "Stein", "Edison"), state.enabledFormats)
        assertFalse(state.isLoading)
    }

    @Test
    fun `default format is first enabled format in canonical order when nothing stored`() = runTest {
        coEvery { repository.getAllFormats() } returns listOf("Edison", "Goat")
        val vm = RulingsViewModel(repository, dataStore)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.first()
        assertEquals("Goat", state.defaultFormat)
        assertEquals("Goat", state.selectedFormat)
    }

    @Test
    fun `default format falls back to first enabled format when stored default is disabled`() = runTest {
        every { dataStore.data } returns flowOf(
            preferencesOf(
                UserPreferences.RULINGS_ENABLED_FORMATS to setOf("Stein"),
                UserPreferences.RULINGS_DEFAULT_FORMAT to "Goat",
            )
        )
        coEvery { repository.getAllFormats() } returns listOf("Goat", "Stein")
        val vm = RulingsViewModel(repository, dataStore)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.first()
        assertEquals("Stein", state.defaultFormat)
        assertEquals("Stein", state.selectedFormat)
        assertEquals(listOf("Stein"), state.enabledFormats)
    }

    @Test
    fun `init falls back to all formats enabled when stored enabled set is empty`() = runTest {
        every { dataStore.data } returns flowOf(
            preferencesOf(UserPreferences.RULINGS_ENABLED_FORMATS to emptySet<String>())
        )
        coEvery { repository.getAllFormats() } returns listOf("Goat", "Edison")
        val vm = RulingsViewModel(repository, dataStore)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.first()
        assertEquals(listOf("Goat", "Edison"), state.enabledFormats)
        assertEquals("Goat", state.defaultFormat)
    }

    @Test
    fun `dropdownFormats returns enabled formats with date labels`() = runTest {
        coEvery { repository.getAllFormats() } returns listOf("Goat", "Edison", "HAT")
        val vm = RulingsViewModel(repository, dataStore)
        testDispatcher.scheduler.advanceUntilIdle()

        val dropdown = vm.dropdownFormats
        assertEquals(3, dropdown.size)
        assertTrue(dropdown.all { it.second.contains(" - ") })
    }

    @Test
    fun `selectFormat resets search and card`() = runTest {
        coEvery { repository.getAllFormats() } returns listOf("Goat", "Edison")
        val vm = RulingsViewModel(repository, dataStore)
        testDispatcher.scheduler.advanceUntilIdle()
        vm.setSearchQuery("Pot")
        vm.selectFormat("Edison")

        val state = vm.uiState.first()
        assertEquals("Edison", state.selectedFormat)
        assertEquals("", state.searchQuery)
        assertNull(state.selectedCard)
    }

    @Test
    fun `toggleFormatEnabled round-trips format between disabled and enabled`() = runTest {
        coEvery { repository.getAllFormats() } returns listOf("Goat", "Stein")
        val vm = RulingsViewModel(repository, dataStore)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.toggleFormatEnabled("Stein")
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(vm.uiState.first().enabledFormats.contains("Stein"))

        vm.toggleFormatEnabled("Stein")
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.uiState.first().enabledFormats.contains("Stein"))
    }

    @Test
    fun `toggleFormatEnabled disables enabled format`() = runTest {
        coEvery { repository.getAllFormats() } returns listOf("Goat", "Edison")
        val vm = RulingsViewModel(repository, dataStore)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.toggleFormatEnabled("Goat")
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.uiState.first().enabledFormats.contains("Goat"))
    }

    @Test
    fun `setSearchQuery triggers debounced search`() = runTest {
        coEvery { repository.getAllFormats() } returns listOf("Goat")
        coEvery { repository.searchCards("Pot", "Goat", 20) } returns listOf(
            CardSearchResult(1, "Pot of Greed", "Spell Card", null)
        )
        val vm = RulingsViewModel(repository, dataStore)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.setSearchQuery("Pot")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, vm.uiState.first().searchResults.size)
    }

    @Test
    fun `selectCard loads card rulings`() = runTest {
        coEvery { repository.getAllFormats() } returns listOf("Goat")
        coEvery { repository.getCardRulings(1, "Goat") } returns CardRulingsDetail(
            1, "Pot of Greed", "Spell Card", "Draw 2.", null, "Rulings"
        )
        val vm = RulingsViewModel(repository, dataStore)
        testDispatcher.scheduler.advanceUntilIdle()
        vm.selectCard(1)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(vm.uiState.first().selectedCard)
    }

    @Test
    fun `toggleGeneralRule toggles expanded index`() = runTest {
        coEvery { repository.getAllFormats() } returns listOf("Goat")
        val vm = RulingsViewModel(repository, dataStore)
        testDispatcher.scheduler.advanceUntilIdle()
        vm.toggleGeneralRule(0)
        assertEquals(0, vm.uiState.first().expandedRuleIndex)
        vm.toggleGeneralRule(0)
        assertNull(vm.uiState.first().expandedRuleIndex)
    }

    @Test
    fun `showOptionsSheet and hideOptionsSheet toggle`() = runTest {
        coEvery { repository.getAllFormats() } returns listOf("Goat")
        val vm = RulingsViewModel(repository, dataStore)
        testDispatcher.scheduler.advanceUntilIdle()
        vm.showOptionsSheet()
        assertTrue(vm.uiState.first().showOptionsSheet)
        vm.hideOptionsSheet()
        assertFalse(vm.uiState.first().showOptionsSheet)
    }

    @Test
    fun `setDefaultFormat updates defaultFormat and persists`() = runTest {
        coEvery { repository.getAllFormats() } returns listOf("Goat", "Edison", "Stein")
        val vm = RulingsViewModel(repository, dataStore)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.setDefaultFormat("Edison")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.first()
        assertEquals("Edison", state.defaultFormat)
    }

    @Test
    fun `allFormatsWithStatus returns formats in chronological order`() = runTest {
        coEvery { repository.getAllFormats() } returns listOf("HAT", "Goat", "Edison")
        val vm = RulingsViewModel(repository, dataStore)
        testDispatcher.scheduler.advanceUntilIdle()

        val statuses = vm.uiState.first().allFormatsWithStatus
        assertEquals(3, statuses.size)
        assertEquals("Goat", statuses[0].first)
        assertEquals("Edison", statuses[1].first)
        assertEquals("HAT", statuses[2].first)
    }

    @Test
    fun `toggleFormatEnabled cannot disable the last enabled format`() = runTest {
        coEvery { repository.getAllFormats() } returns listOf("Goat", "HAT")
        val vm = RulingsViewModel(repository, dataStore)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.toggleFormatEnabled("Goat")
        testDispatcher.scheduler.advanceUntilIdle()
        vm.toggleFormatEnabled("HAT")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.first()
        assertEquals(listOf("HAT"), state.enabledFormats)
        assertEquals("HAT", state.selectedFormat)
        assertEquals("HAT", state.defaultFormat)
    }
}
