package com.turnzero.games.yugioh.tools.rulings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.turnzero.games.yugioh.tools.rulings.data.CardRulingsDetail
import com.turnzero.games.yugioh.tools.rulings.data.CardSearchResult
import com.turnzero.games.yugioh.tools.rulings.data.RulingsRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RulingsViewModelTest {

    private val repository: RulingsRepository = mockk()
    private val dataStore: DataStore<Preferences> = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { dataStore.data } returns flowOf(emptyPreferences())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads formats and defaults to Goat`() = runTest {
        coEvery { repository.getAllFormats() } returns listOf("Goat", "Edison", "Stein")
        val vm = RulingsViewModel(repository, dataStore)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.first()
        assertEquals("Goat", state.selectedFormat)
        assertEquals(listOf("Goat", "Edison"), state.enabledFormats)
        assertFalse(state.isLoading)
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
    fun `toggleFormatEnabled enables disabled format`() = runTest {
        coEvery { repository.getAllFormats() } returns listOf("Goat", "Stein")
        val vm = RulingsViewModel(repository, dataStore)
        testDispatcher.scheduler.advanceUntilIdle()

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
}
