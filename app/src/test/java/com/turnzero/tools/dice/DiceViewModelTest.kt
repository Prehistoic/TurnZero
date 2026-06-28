package com.turnzero.tools.dice

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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DiceViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeDiceRepo
    private lateinit var savedStateHandle: SavedStateHandle

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeDiceRepo()
        savedStateHandle = SavedStateHandle(mapOf("gameId" to "test-game"))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty dice and default color`() = runTest {
        val vm = DiceViewModel(repo, savedStateHandle)
        advanceUntilIdle()
        val state = vm.uiState.value
        assertTrue(state.dice.isEmpty())
        assertEquals(DiceColor.RED, state.selectedColor)
        assertEquals("test-game", state.gameId)
        assertNull(state.results)
        assertNull(state.total)
        assertFalse(state.isRolling)
    }

    @Test
    fun `changeDieCount adds dice with selected color`() = runTest {
        val vm = DiceViewModel(repo, savedStateHandle)
        advanceUntilIdle()
        vm.changeDieCount(6, DiceColor.RED, 2)
        val state = vm.uiState.value
        assertEquals(2, state.dice.size)
        assertEquals(6, state.dice[0].faceCount)
        assertEquals(DiceColor.RED, state.dice[0].color)
    }

    @Test
    fun `changeDieCount removes dice`() = runTest {
        val vm = DiceViewModel(repo, savedStateHandle)
        advanceUntilIdle()
        vm.changeDieCount(6, DiceColor.RED, 3)
        vm.changeDieCount(6, DiceColor.RED, -1)
        assertEquals(2, vm.uiState.value.dice.size)
    }

    @Test
    fun `changeDieCount does not go below zero`() = runTest {
        val vm = DiceViewModel(repo, savedStateHandle)
        advanceUntilIdle()
        vm.changeDieCount(6, DiceColor.RED, 1)
        vm.changeDieCount(6, DiceColor.RED, -5)
        assertEquals(0, vm.uiState.value.dice.size)
    }

    @Test
    fun `roll generates results within faceCount range`() = runTest {
        val vm = DiceViewModel(repo, savedStateHandle)
        advanceUntilIdle()
        vm.changeDieCount(6, DiceColor.RED, 2)
        vm.changeDieCount(20, DiceColor.BLUE, 1)
        vm.roll()

        val state = vm.uiState.value
        assertTrue(state.isRolling)
        assertNotNull(state.results)
        assertEquals(3, state.results!!.size)
        state.results!!.forEachIndexed { index, result ->
            val faceCount = state.dice[index].faceCount
            assertTrue(result in 1..faceCount)
        }
        assertEquals(state.results!!.sum(), state.total)
    }

    @Test
    fun `animationComplete sets isRolling to false and adds to history`() = runTest {
        val vm = DiceViewModel(repo, savedStateHandle)
        advanceUntilIdle()
        vm.changeDieCount(6, DiceColor.RED, 1)
        vm.roll()
        assertTrue(vm.uiState.value.isRolling)
        vm.animationComplete()
        assertFalse(vm.uiState.value.isRolling)
        assertEquals(1, vm.uiState.value.history.size)
    }

    @Test
    fun `roll does nothing with empty pool`() = runTest {
        val vm = DiceViewModel(repo, savedStateHandle)
        advanceUntilIdle()
        vm.roll()
        assertNull(vm.uiState.value.results)
        assertFalse(vm.uiState.value.isRolling)
    }

    @Test
    fun `loadPreset replaces pool`() = runTest {
        val vm = DiceViewModel(repo, savedStateHandle)
        advanceUntilIdle()
        vm.changeDieCount(6, DiceColor.RED, 3)
        assertEquals(3, vm.uiState.value.dice.size)

        val preset = DicePreset(
            id = "p1", name = "Test", gameId = "test-game",
            dice = listOf(Die(id = "x", faceCount = 20, color = DiceColor.GREEN))
        )
        vm.loadPreset(preset)
        assertEquals(1, vm.uiState.value.dice.size)
        assertEquals(20, vm.uiState.value.dice[0].faceCount)
    }

    @Test
    fun `clearHistory empties history`() = runTest {
        val vm = DiceViewModel(repo, savedStateHandle)
        advanceUntilIdle()
        vm.changeDieCount(6, DiceColor.RED, 1)
        vm.roll()
        vm.animationComplete()
        assertEquals(1, vm.uiState.value.history.size)
        vm.clearHistory()
        assertTrue(vm.uiState.value.history.isEmpty())
    }

    @Test
    fun `setSelectedColor updates color and persists`() = runTest {
        val vm = DiceViewModel(repo, savedStateHandle)
        advanceUntilIdle()
        vm.setSelectedColor(DiceColor.GREEN)
        advanceUntilIdle()
        assertEquals(DiceColor.GREEN, vm.uiState.value.selectedColor)
        assertEquals(DiceColor.GREEN, repo.selectedColor)
    }
}

private class FakeDiceRepo : DiceRepo {
    var pool: List<Die> = emptyList()
    var selectedColor: DiceColor = DiceColor.RED
    var history: List<DiceRoll> = emptyList()

    override suspend fun getPool(gameId: String) = pool
    override suspend fun savePool(gameId: String, dice: List<Die>) { pool = dice }
    override suspend fun getSelectedColor() = selectedColor
    override suspend fun saveSelectedColor(color: DiceColor) { selectedColor = color }
    override suspend fun getHistory() = history
    override suspend fun saveHistory(h: List<DiceRoll>) { history = h }
}
