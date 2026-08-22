package com.turnzero.tools.rules

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.lifecycle.SavedStateHandle
import com.turnzero.core.data.UserPreferences
import com.turnzero.core.ui.components.ContentBlock
import com.turnzero.TZViewModelTest
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RulesViewModelTest : TZViewModelTest() {

    private val dataStore = mockk<DataStore<Preferences>>(relaxed = true)

    @Before
    fun setup() {
        every { dataStore.data } returns flowOf(emptyPreferences())
    }

    private val sampleConfig = RulesConfig(
        rulesetId = "v6-gx",
        rulesetName = "v6.0 - GX",
        setup = listOf(
            RuleStep("Shuffle", "Shuffle deck", listOf(ContentBlock.Paragraph("Detail..."))),
            RuleStep("Draw", "Draw 5", listOf(ContentBlock.Paragraph("Detail..."))),
        ),
        victoryRules = listOf(
            InfoCard("Win", listOf(ContentBlock.Paragraph("Reduce LP"))),
        ),
        playSequence = listOf(
            RuleStep("Draw Phase", "Draw 1", listOf(ContentBlock.Paragraph("Detail..."))),
        ),
        keywords = listOf(
            Keyword("Quick Effect", listOf(ContentBlock.Paragraph("Spell Speed 2"))),
        ),
        pdfAssets = listOf(
            PdfAsset("Rulebook", "rulebooks/yugioh-v6-rulebook.pdf"),
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
    fun `init uses defaultRulesetId from DataStore when available`() = runTest {
        every { dataStore.data } returns flowOf(
            mutablePreferencesOf(UserPreferences.RULES_DEFAULT_RULESET to "v6-gx")
        )
        val vm = createViewModel()
        advanceUntilIdle()
        assertEquals("v6-gx", vm.uiState.value.selectedRulesetId)
        assertEquals("v6-gx", vm.uiState.value.defaultRulesetId)
    }

    @Test
    fun `selectRuleset preserves tab index and clears search`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.selectTab(2)
        vm.toggleStep(0)
        vm.setSearchQuery("test")
        vm.selectRuleset("v6-gx")
        val state = vm.uiState.value
        assertEquals("v6-gx", state.selectedRulesetId)
        assertEquals(2, state.selectedTabIndex)
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
    fun `setDefaultRuleset updates default id and sets current ruleset to same`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.setDefaultRuleset("v6-gx")
        val state = vm.uiState.value
        assertEquals("v6-gx", state.defaultRulesetId)
        assertEquals("v6-gx", state.selectedRulesetId)
    }

    @Test
    fun `setDefaultRuleset does not change other uiState properties`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.selectRuleset("v6-gx")
        vm.togglePdf()
        vm.setDefaultRuleset("v5-dm")
        val state = vm.uiState.value
        assertEquals("v5-dm", state.defaultRulesetId)
        assertTrue(state.showPdf)
    }

    @Test
    fun `visibleTabs includes keywords when config has keywords`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.selectRuleset("v6-gx")
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

    @Test
    fun `init enables all rulesets by default`() = runTest {
        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.first()
        assertEquals(setOf("v5-dm", "v6-gx"), state.enabledRulesets)
    }

    @Test
    fun `toggleRulesetEnabled cannot disable the last enabled ruleset`() = runTest {
        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.toggleRulesetEnabled("v5-dm", false)
        testDispatcher.scheduler.advanceUntilIdle()
        vm.toggleRulesetEnabled("v6-gx", false)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.first()
        assertEquals(setOf("v6-gx"), state.enabledRulesets)
        assertEquals("v6-gx", state.selectedRulesetId)
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
