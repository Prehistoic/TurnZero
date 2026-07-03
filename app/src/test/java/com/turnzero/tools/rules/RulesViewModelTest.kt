package com.turnzero.tools.rules

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val sampleConfig = RulesConfig(
        rulesetId = "tcg",
        rulesetName = "Advanced (TCG)",
        setup = listOf(
            RuleStep("Shuffle", "Shuffle deck", "Detail..."),
            RuleStep("Draw", "Draw 5", "Detail..."),
        ),
        winCons = listOf(
            WinCondition("Win", "Reduce LP"),
        ),
        playSequence = listOf(
            RuleStep("Draw Phase", "Draw 1", "Detail..."),
        ),
        keywords = listOf(
            Keyword("Quick Effect", "Spell Speed 2"),
        ),
    )

    private val configWithoutKeywords = sampleConfig.copy(
        rulesetId = "speed",
        rulesetName = "Speed Duel",
        keywords = null,
    )

    private val configs = listOf(sampleConfig, configWithoutKeywords)

    private fun createViewModel(gameId: String = "yugioh"): RulesViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("gameId" to gameId))
        val provider = FakeRulesProvider().apply {
            register("yugioh", configs)
        }
        return RulesViewModel(provider, savedStateHandle)
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
    fun `selectRuleset does not change selectedTabIndex and resets expanded states`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.selectTab(2)
        vm.toggleStep(0)
        vm.selectRuleset(1)
        val state = vm.uiState.value
        assertEquals(1, state.selectedRulesetIndex)
        assertEquals(2, state.selectedTabIndex)
        assertNull(state.expandedStepIndex)
        assertNull(state.expandedKeywordIndex)
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
        assertNull(vm.uiState.value.expandedKeywordIndex)
    }

    @Test
    fun `onPageChanged syncs selectedTabIndex and clears expanded states`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.toggleKeyword(0)
        vm.onPageChanged(1)
        assertEquals(1, vm.uiState.value.selectedTabIndex)
        assertNull(vm.uiState.value.expandedStepIndex)
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
    fun `toggleStep switches expansion to different index`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.toggleStep(0)
        vm.toggleStep(1)
        assertEquals(1, vm.uiState.value.expandedStepIndex)
    }

    @Test
    fun `toggleKeyword expands keyword at index`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.toggleKeyword(0)
        assertEquals(0, vm.uiState.value.expandedKeywordIndex)
    }

    @Test
    fun `toggleKeyword collapses when same index tapped`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.toggleKeyword(0)
        vm.toggleKeyword(0)
        assertNull(vm.uiState.value.expandedKeywordIndex)
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
    fun `visibleTabs has keywords when config has keywords`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        assertEquals(4, vm.uiState.value.visibleTabs.size)
    }

    @Test
    fun `visibleTabs excludes keywords after switching to ruleset without keywords`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.selectRuleset(1)
        assertEquals(3, vm.uiState.value.visibleTabs.size)
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
