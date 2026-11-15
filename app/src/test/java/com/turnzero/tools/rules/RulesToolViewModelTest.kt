package com.turnzero.tools.rules

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.lifecycle.SavedStateHandle
import com.turnzero.TZViewModelTest
import com.turnzero.core.data.UserPreferences
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RulesToolViewModelTest : TZViewModelTest() {

    private val dataStore = mockk<DataStore<Preferences>>(relaxed = true)
    private val storedPreferences = MutableStateFlow<Preferences>(emptyPreferences())

    @Before
    fun setup() {
        every { dataStore.data } returns storedPreferences
        coEvery { dataStore.updateData(any()) } coAnswers {
            val transform = arg<suspend (Preferences) -> Preferences>(0)
            transform(storedPreferences.value).also { storedPreferences.value = it }
        }
    }

    private fun createViewModel(
        rules: GameRules? = sampleRules,
        gameId: String = GAME_ID,
    ): RulesToolViewModel = RulesToolViewModel(
        repository = FakeRulesRepository(rules),
        dataStore = dataStore,
        savedStateHandle = SavedStateHandle(mapOf("gameId" to gameId)),
    )

    @Test
    fun `init without rules leaves empty state`() = runTest {
        val vm = createViewModel(rules = null)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertNull(state.rules)
        assertFalse(state.isLoading)
        assertNull(state.subTool)
        assertFalse(state.showSpecificityDialog)
        assertNull(state.specificitySummary)
    }

    @Test
    fun `init with rules and no specificities shows no dialog and no summary`() = runTest {
        val rules = sampleRules.copy(
            manifest = sampleRules.manifest.copy(specificities = emptyList())
        )
        val vm = createViewModel(rules)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.hasSpecificities)
        assertFalse(state.showSpecificityDialog)
        assertNull(state.draftSelection)
        assertNull(state.specificitySummary)
    }

    @Test
    fun `init with specificities and not configured shows dialog with defaults`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(defaultSelection(sampleRules.manifest), state.selection)
        assertEquals(state.selection, state.draftSelection)
        assertTrue(state.showSpecificityDialog)
    }

    @Test
    fun `init with specificities and configured restores persisted selection`() = runTest {
        storedPreferences.value = mutablePreferencesOf(
            UserPreferences.rulesSpecStringKey(GAME_ID, "fmt") to "commander",
            UserPreferences.rulesConfiguredKey(GAME_ID) to true,
        )
        val vm = createViewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("commander", state.selection.single["fmt"])
        assertEquals(2, state.selection.number["players"] ?: -1)
        assertEquals(emptySet<String>(), state.selection.multi["ext"])
        assertFalse(state.showSpecificityDialog)
        assertNull(state.draftSelection)
    }

    @Test
    fun `confirmSpecificities persists selection and closes dialog`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.selectSpecificityOption("fmt", "commander")
        vm.setSpecificityNumber("players", 4)
        vm.confirmSpecificities()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("commander", state.selection.single["fmt"])
        assertEquals(4, state.selection.number["players"] ?: -1)
        assertNull(state.draftSelection)
        assertFalse(state.showSpecificityDialog)
        assertEquals(
            "commander",
            storedPreferences.value[UserPreferences.rulesSpecStringKey(GAME_ID, "fmt")]
        )
        assertEquals(
            4,
            storedPreferences.value[UserPreferences.rulesSpecIntKey(GAME_ID, "players")] ?: -1
        )
        assertEquals(
            true,
            storedPreferences.value[UserPreferences.rulesConfiguredKey(GAME_ID)]
        )
        coVerify { dataStore.updateData(any()) }
    }

    @Test
    fun `dismissSpecificityDialog reverts draft without persisting`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.selectSpecificityOption("fmt", "commander")
        vm.dismissSpecificityDialog()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertNull(state.draftSelection)
        assertFalse(state.showSpecificityDialog)
        assertEquals("standard", state.selection.single["fmt"])
        assertNull(storedPreferences.value[UserPreferences.rulesConfiguredKey(GAME_ID)])
        assertNull(storedPreferences.value[UserPreferences.rulesSpecStringKey(GAME_ID, "fmt")])
        coVerify(exactly = 0) { dataStore.updateData(any()) }
    }

    @Test
    fun `selectSpecificityOption applies requires closure to draft`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.selectSpecificityOption("ext", "b")

        val draft = vm.uiState.value.draftSelection!!
        assertEquals(setOf("a", "b"), draft.multi["ext"])
        assertEquals(emptySet<String>(), vm.uiState.value.selection.multi["ext"])
    }

    @Test
    fun `openSubTool persists sub tool and fresh view model restores it`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.openSubTool(RuleSubTool.SETUP)
        advanceUntilIdle()

        assertEquals(RuleSubTool.SETUP, vm.uiState.value.subTool)
        assertEquals("setup", storedPreferences.value[UserPreferences.rulesSubToolKey(GAME_ID)])

        val restored = createViewModel()
        advanceUntilIdle()
        assertEquals(RuleSubTool.SETUP, restored.uiState.value.subTool)
    }

    @Test
    fun `closeSubTool clears persisted sub tool`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.openSubTool(RuleSubTool.SETUP)
        advanceUntilIdle()
        vm.closeSubTool()
        advanceUntilIdle()

        assertNull(vm.uiState.value.subTool)
        assertNull(storedPreferences.value[UserPreferences.rulesSubToolKey(GAME_ID)])

        val restored = createViewModel()
        advanceUntilIdle()
        assertNull(restored.uiState.value.subTool)
    }

    @Test
    fun `jumpToPages opens rulebook at referenced page`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.openSubTool(RuleSubTool.SETUP)
        vm.jumpToPages()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(RuleSubTool.RULEBOOK, state.subTool)
        assertEquals("official", state.selectedRulebookId)
        assertEquals(3, state.pdfInitialPage)
        assertEquals(
            "rulebook",
            storedPreferences.value[UserPreferences.rulesSubToolKey(GAME_ID)]
        )
    }

    @Test
    fun `dismissPdfSelector returns to the main panel and clears the persisted sub-tool`() = runTest {
        val rules = sampleRules.copy(
            manifest = sampleRules.manifest.copy(
                rulebooks = listOf(
                    RulebookDef(id = "official", name = "Official Rulebook", asset = "rulebooks/official.pdf"),
                    RulebookDef(id = "quick", name = "Quick Start", asset = "rulebooks/quick.pdf"),
                )
            )
        )
        val vm = createViewModel(rules)
        advanceUntilIdle()

        vm.openSubTool(RuleSubTool.RULEBOOK)
        assertTrue(vm.uiState.value.showPdfSelector)

        vm.dismissPdfSelector()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.showPdfSelector)
        assertNull(state.subTool)
        assertEquals(0, state.pdfInitialPage)
        assertNull(storedPreferences.value[UserPreferences.rulesSubToolKey(GAME_ID)])
    }

    @Test
    fun `jumpToPages falls back when no page ref matches`() = runTest {
        val rules = sampleRules.copy(
            setup = sampleRules.setup.copy(
                pages = listOf(
                    PageRef(
                        rulebook = "commander",
                        pages = listOf(2),
                        `when` = Condition.Predicate("fmt", SpecValue.Eq("commander")),
                    )
                )
            )
        )
        val vm = createViewModel(rules)
        advanceUntilIdle()

        vm.openSubTool(RuleSubTool.SETUP)
        vm.jumpToPages()

        val state = vm.uiState.value
        assertEquals(RuleSubTool.RULEBOOK, state.subTool)
        assertEquals("official", state.selectedRulebookId)
        assertEquals(0, state.pdfInitialPage)
        assertFalse(state.showPdfSelector)
    }

    @Test
    fun `nextStep advances within phase then across phase boundary`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.openSubTool(RuleSubTool.TURN)
        vm.openPhase(0)
        vm.openStep(0)

        vm.nextStep()
        assertEquals(0, vm.uiState.value.turnPhaseIndex ?: -1)
        assertEquals(1, vm.uiState.value.turnStepIndex ?: -1)

        vm.nextStep()
        assertEquals(1, vm.uiState.value.turnPhaseIndex ?: -1)
        assertEquals(0, vm.uiState.value.turnStepIndex ?: -1)

        vm.nextStep()
        assertEquals(1, vm.uiState.value.turnPhaseIndex ?: -1)
        assertEquals(0, vm.uiState.value.turnStepIndex ?: -1)
    }

    @Test
    fun `previousStep goes back across phase boundary`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.openSubTool(RuleSubTool.TURN)
        vm.openPhase(1)
        vm.openStep(0)

        vm.previousStep()
        assertEquals(0, vm.uiState.value.turnPhaseIndex ?: -1)
        assertEquals(1, vm.uiState.value.turnStepIndex ?: -1)

        vm.previousStep()
        assertEquals(0, vm.uiState.value.turnPhaseIndex ?: -1)
        assertEquals(0, vm.uiState.value.turnStepIndex ?: -1)

        vm.previousStep()
        assertEquals(0, vm.uiState.value.turnPhaseIndex ?: -1)
        assertEquals(0, vm.uiState.value.turnStepIndex ?: -1)
    }

    @Test
    fun `turnPhases drops phases with no visible steps`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        val phases = vm.uiState.value.turnPhases
        assertEquals(listOf("Draw", "Main"), phases.map { it.name })
        assertEquals(listOf("draw-1", "draw-2"), phases[0].steps.map { it.title })
    }

    @Test
    fun `filteredKeywords filters by query and category`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals(
            listOf("First Strike", "Flying", "Hexproof"),
            vm.uiState.value.filteredKeywords.map { it.name }
        )

        vm.setKeywordQuery("fl")
        assertEquals(listOf("Flying"), vm.uiState.value.filteredKeywords.map { it.name })

        vm.setKeywordQuery("")
        vm.filterKeywordCategory("Combat")
        assertEquals(
            listOf("First Strike", "Flying"),
            vm.uiState.value.filteredKeywords.map { it.name }
        )

        vm.setKeywordQuery("st")
        assertEquals(listOf("First Strike"), vm.uiState.value.filteredKeywords.map { it.name })

        vm.toggleKeyword(0)
        vm.setKeywordQuery("fly")
        assertNull(vm.uiState.value.expandedKeywordIndex)
        vm.toggleKeyword(0)
        vm.filterKeywordCategory(null)
        assertNull(vm.uiState.value.expandedKeywordIndex)

        assertEquals(listOf("Combat", "Protection"), vm.uiState.value.keywordCategories)
    }

    @Test
    fun `showLayoutSheet opens the sheet and dismissLayoutSheet closes it`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.showLayoutSheet)

        vm.showLayoutSheet()
        assertTrue(vm.uiState.value.showLayoutSheet)

        vm.dismissLayoutSheet()
        assertFalse(vm.uiState.value.showLayoutSheet)
    }

    @Test
    fun `specificitySummary formats single number and multi parts`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals("Standard · 2P", vm.uiState.value.specificitySummary)

        vm.selectSpecificityOption("fmt", "commander")
        vm.setSpecificityNumber("players", 4)
        vm.selectSpecificityOption("ext", "b")
        vm.confirmSpecificities()
        advanceUntilIdle()

        assertEquals("Commander · 4P · +2 ext", vm.uiState.value.specificitySummary)
    }
}

private const val GAME_ID = "game"

private class FakeRulesRepository(private val rules: GameRules?) : RulesRepository {
    override suspend fun getRules(gameId: String): GameRules? = rules
}

private val sampleRules = GameRules(
    manifest = RulesManifest(
        specificities = listOf(
            SpecificityDef(
                id = "fmt",
                name = "Format",
                type = SpecificityType.SINGLE,
                options = listOf(
                    OptionDef(id = "standard", name = "Standard"),
                    OptionDef(id = "commander", name = "Commander"),
                ),
            ),
            SpecificityDef(
                id = "players",
                name = "Players",
                type = SpecificityType.NUMBER,
                min = 2,
                max = 4,
            ),
            SpecificityDef(
                id = "ext",
                name = "Extensions",
                type = SpecificityType.MULTI,
                options = listOf(
                    OptionDef(id = "a", name = "Extension A"),
                    OptionDef(id = "b", name = "Extension B", requires = listOf("a")),
                ),
            ),
        ),
        rulebooks = listOf(
            RulebookDef(
                id = "official",
                name = "Official Rulebook",
                asset = "rulebooks/official.pdf",
            ),
            RulebookDef(
                id = "commander",
                name = "Commander Rulebook",
                asset = "rulebooks/commander.pdf",
                `when` = Condition.Predicate("fmt", SpecValue.Eq("commander")),
            ),
        ),
    ),
    setup = SetupContent(
        pages = listOf(PageRef(rulebook = "official", pages = listOf(4, 5))),
        steps = listOf(
            RuleItem(title = "Shuffle"),
            RuleItem(
                title = "Commander setup",
                `when` = Condition.Predicate("fmt", SpecValue.Eq("commander")),
            ),
        ),
    ),
    turn = TurnContent(
        phases = listOf(
            Phase(
                name = "Draw",
                steps = listOf(
                    RuleItem(title = "draw-1"),
                    RuleItem(title = "draw-2"),
                ),
            ),
            Phase(
                name = "Main",
                steps = listOf(RuleItem(title = "main-1")),
            ),
            Phase(
                name = "Commander",
                steps = listOf(
                    RuleItem(
                        title = "cmd-1",
                        `when` = Condition.Predicate("fmt", SpecValue.Eq("commander")),
                    )
                ),
            ),
        ),
    ),
    win = WinContent(
        winConditions = listOf(RuleItem(title = "Reduce life to zero")),
    ),
    keywords = KeywordsContent(
        keywords = listOf(
            KeywordItem(name = "Hexproof", category = "Protection"),
            KeywordItem(name = "Flying", category = "Combat"),
            KeywordItem(name = "First Strike", category = "Combat"),
        ),
    ),
)
