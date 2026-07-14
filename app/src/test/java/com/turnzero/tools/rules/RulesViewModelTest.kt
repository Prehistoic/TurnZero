package com.turnzero.tools.rules

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.lifecycle.SavedStateHandle
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RulesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val dataStore = mockk<DataStore<Preferences>>(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { dataStore.data } returns flowOf(emptyPreferences())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val sampleConfig = RulesConfig(
        rulesetId = "v6-gx",
        rulesetName = "v6.0 - GX",
        setup = listOf(
            RuleStep("Shuffle", "Shuffle deck", listOf(ContentBlock.Paragraph("Detail..."))),
            RuleStep("Draw", "Draw 5", listOf(ContentBlock.Paragraph("Detail..."))),
        ),
        winCons = listOf(
            WinCondition("Win", listOf(ContentBlock.Paragraph("Reduce LP"))),
        ),
        playSequence = listOf(
            RuleStep("Draw Phase", "Draw 1", listOf(ContentBlock.Paragraph("Detail..."))),
        ),
        keywords = listOf(
            Keyword("Quick Effect", listOf(ContentBlock.Paragraph("Spell Speed 2"))),
        ),
        pdfAssets = listOf(
            PdfAsset("Rulebook", "rulebooks/v6-gx.pdf"),
        ),
    )

    private val configWithoutKeywords = sampleConfig.copy(
        rulesetId = "v5-dm",
        rulesetName = "v5.0 - DM",
        keywords = null,
    )

    private val configs = listOf(configWithoutKeywords, sampleConfig)

    private fun createViewModel(gameId: String = "yugioh"): RulesViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("gameId" to gameId))
        val provider = FakeRulesProvider().apply {
            register("yugioh", configs)
        }
        return RulesViewModel(provider, dataStore, savedStateHandle)
    }

    @Test
    fun `init loads configs from provider and sets isLoading false`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        val state = vm.uiState.value
        assertEquals("yugioh", state.gameId)
        assertEquals(2, state.configs.size)
        assertFalse(state.isLoading)
    }

    @Test
    fun `init with no configs leaves isLoading true`() = runTest {
        val vm = createViewModel("unknown")
        advanceUntilIdle()
        val state = vm.uiState.value
        assertTrue(state.configs.isEmpty())
        assertTrue(state.isLoading)
    }

    @Test
    fun `init uses defaultRulesetIndex from SavedStateHandle`() = runTest {
        val sh = SavedStateHandle(mapOf("gameId" to "yugioh", "defaultRulesetIndex" to 1))
        val provider = FakeRulesProvider().apply { register("yugioh", configs) }
        val vm = RulesViewModel(provider, dataStore, sh)
        advanceUntilIdle()
        assertEquals(1, vm.uiState.value.selectedRulesetIndex)
        assertEquals(1, vm.uiState.value.defaultRulesetIndex)
    }

    @Test
    fun `selectRuleset resets tab to 0 and closes pdf`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.selectTab(2)
        vm.togglePdf()
        vm.toggleStep(0)
        vm.setSearchQuery("test")
        vm.selectRuleset(1)
        val state = vm.uiState.value
        assertEquals(1, state.selectedRulesetIndex)
        assertEquals(0, state.selectedTabIndex)
        assertFalse(state.showPdf)
        assertNull(state.expandedStepIndex)
        assertEquals("", state.keywordSearchQuery)
    }

    @Test
    fun `selectTab updates selectedTabIndex and clears expanded states`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.toggleStep(0)
        vm.selectTab(2)
        assertEquals(2, vm.uiState.value.selectedTabIndex)
        assertNull(vm.uiState.value.expandedStepIndex)
    }

    @Test
    fun `selectTab with same index does nothing`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.toggleStep(0)
        vm.selectTab(vm.uiState.value.selectedTabIndex)
        assertEquals(0, vm.uiState.value.expandedStepIndex)
    }

    @Test
    fun `onPageChanged syncs selectedTabIndex and clears expanded states`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.toggleKeyword(0)
        vm.onPageChanged(1)
        assertEquals(1, vm.uiState.value.selectedTabIndex)
        assertNull(vm.uiState.value.expandedKeywordIndex)
    }

    @Test
    fun `toggleStep expands step at index`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.toggleStep(0)
        assertEquals(0, vm.uiState.value.expandedStepIndex)
    }

    @Test
    fun `toggleStep collapses when same index tapped`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.toggleStep(0)
        vm.toggleStep(0)
        assertNull(vm.uiState.value.expandedStepIndex)
    }

    @Test
    fun `toggleKeyword expands keyword at index`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.toggleKeyword(0)
        assertEquals(0, vm.uiState.value.expandedKeywordIndex)
    }

    @Test
    fun `setSearchQuery updates query and clears expanded keyword`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.toggleKeyword(0)
        vm.setSearchQuery("eff")
        assertEquals("eff", vm.uiState.value.keywordSearchQuery)
        assertNull(vm.uiState.value.expandedKeywordIndex)
    }

    @Test
    fun `filterCategory updates category and clears expanded keyword`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.toggleKeyword(0)
        vm.filterCategory("Summons")
        assertEquals("Summons", vm.uiState.value.selectedKeywordCategory)
        assertNull(vm.uiState.value.expandedKeywordIndex)
    }

    @Test
    fun `filterCategory with same category toggles off`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.filterCategory("Summons")
        vm.filterCategory("Summons")
        assertEquals("Summons", vm.uiState.value.selectedKeywordCategory)
    }

    @Test
    fun `togglePdf toggles showPdf`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        assertFalse(vm.uiState.value.showPdf)
        vm.togglePdf()
        assertTrue(vm.uiState.value.showPdf)
        vm.togglePdf()
        assertFalse(vm.uiState.value.showPdf)
    }

    @Test
    fun `toggleOptionsSheet toggles showOptionsSheet`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        assertFalse(vm.uiState.value.showOptionsSheet)
        vm.toggleOptionsSheet()
        assertTrue(vm.uiState.value.showOptionsSheet)
        vm.toggleOptionsSheet()
        assertFalse(vm.uiState.value.showOptionsSheet)
    }

    @Test
    fun `setDefaultRuleset updates default index and sets current ruleset to same`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.setDefaultRuleset(1)
        val state = vm.uiState.value
        assertEquals(1, state.defaultRulesetIndex)
        assertEquals(1, state.selectedRulesetIndex)
        assertEquals(0, state.selectedTabIndex)
        assertNull(state.expandedStepIndex)
    }

    @Test
    fun `setDefaultRuleset does not change other uiState properties`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.selectRuleset(1)
        vm.togglePdf()
        vm.setDefaultRuleset(0)
        val state = vm.uiState.value
        assertEquals(0, state.defaultRulesetIndex)
        assertTrue(state.showPdf)
    }

    @Test
    fun `visibleTabs includes keywords when config has keywords`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        // sampleConfig (index 1) has keywords, configWithoutKeywords (index 0) is default
        vm.selectRuleset(1)
        assertEquals(4, vm.uiState.value.visibleTabs.size)
    }

    @Test
    fun `visibleTabs excludes keywords after switching to ruleset without keywords`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        assertEquals(3, vm.uiState.value.visibleTabs.size)
    }

    @Test
    fun `visibleTabs always has setup turn and victory tabs`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        val tabs = vm.uiState.value.visibleTabs.map { it.key }
        assertTrue(tabs.contains("setup"))
        assertTrue(tabs.contains("turn"))
        assertTrue(tabs.contains("victory"))
        assertFalse(tabs.contains("keywords"))
    }

    @Test
    fun `filteredKeywords returns empty when config has no keywords`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        assertEquals(0, vm.uiState.value.filteredKeywords.size)
    }

    @Test
    fun `allKeywordCategories returns distinct sorted categories`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        val cats = vm.uiState.value.allKeywordCategories
        assertEquals(0, cats.size)
    }
}

private class FakeRulesProvider : RulesProvider {
    private val registry = mutableMapOf<String, List<RulesConfig>>()

    fun register(gameId: String, configs: List<RulesConfig>) {
        registry[gameId] = configs
    }

    override fun getConfigs(gameId: String): List<RulesConfig> {
        return registry[gameId] ?: emptyList()
    }
}
