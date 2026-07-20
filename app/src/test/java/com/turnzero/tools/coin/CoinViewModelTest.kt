package com.turnzero.tools.coin

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import com.turnzero.TZViewModelTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CoinViewModelTest : TZViewModelTest() {

    private lateinit var repo: FakeCoinRepo
    private lateinit var savedStateHandle: SavedStateHandle

    @Before
    fun setup() {
        repo = FakeCoinRepo()
        savedStateHandle = SavedStateHandle(mapOf("gameId" to "test-game"))
    }

    @Test
    fun `initial state has 1 coin and no results`() = runTest {
        val vm = CoinViewModel(repo, savedStateHandle)
        advanceUntilIdle()
        val state = vm.uiState.value
        assertEquals(1, state.coinCount)
        assertEquals("test-game", state.gameId)
        assertTrue(state.results.isEmpty())
        assertFalse(state.isFlipping)
        assertFalse(state.showHistory)
    }

    @Test
    fun `setCoinCount changes count within bounds`() = runTest {
        val vm = CoinViewModel(repo, savedStateHandle)
        advanceUntilIdle()
        vm.setCoinCount(5)
        assertEquals(5, vm.uiState.value.coinCount)
    }

    @Test
    fun `setCoinCount does not go below 1`() = runTest {
        val vm = CoinViewModel(repo, savedStateHandle)
        advanceUntilIdle()
        vm.setCoinCount(0)
        assertEquals(1, vm.uiState.value.coinCount)
    }

    @Test
    fun `setCoinCount caps at 8`() = runTest {
        val vm = CoinViewModel(repo, savedStateHandle)
        advanceUntilIdle()
        vm.setCoinCount(10)
        assertEquals(8, vm.uiState.value.coinCount)
    }

    @Test
    fun `flip generates HEAD or TAIL for each coin`() = runTest {
        val vm = CoinViewModel(repo, savedStateHandle)
        advanceUntilIdle()
        vm.setCoinCount(3)
        vm.flip()
        val state = vm.uiState.value
        assertEquals(3, state.results.size)
        state.results.forEach { face ->
            assertTrue(face == CoinFace.HEAD || face == CoinFace.TAIL)
        }
        assertEquals(state.results.count { it == CoinFace.HEAD }, state.headsCount)
        assertEquals(state.results.count { it == CoinFace.TAIL }, state.tailsCount)
        assertTrue(state.isFlipping)
    }

    @Test
    fun `flipAnimationComplete sets isFlipping false and adds to history`() = runTest {
        val vm = CoinViewModel(repo, savedStateHandle)
        advanceUntilIdle()
        vm.setCoinCount(2)
        vm.flip()
        assertTrue(vm.uiState.value.isFlipping)
        vm.flipAnimationComplete()
        assertFalse(vm.uiState.value.isFlipping)
        assertEquals(1, vm.uiState.value.history.size)
        assertEquals(2, vm.uiState.value.history[0].coinCount)
    }

    @Test
    fun `history caps at 10 entries newest first`() = runTest {
        val vm = CoinViewModel(repo, savedStateHandle)
        advanceUntilIdle()
        vm.setCoinCount(1)
        repeat(12) {
            vm.flip()
            vm.flipAnimationComplete()
        }
        assertEquals(10, vm.uiState.value.history.size)
    }

    @Test
    fun `loadPreset replaces coin count`() = runTest {
        val vm = CoinViewModel(repo, savedStateHandle)
        advanceUntilIdle()
        vm.setCoinCount(5)
        assertEquals(5, vm.uiState.value.coinCount)
        val preset = CoinPreset(id = "p1", name = "Test", gameId = "test-game", coinCount = 3)
        vm.loadPreset(preset)
        assertEquals(3, vm.uiState.value.coinCount)
    }

    @Test
    fun `clearHistory empties history`() = runTest {
        val vm = CoinViewModel(repo, savedStateHandle)
        advanceUntilIdle()
        vm.setCoinCount(1)
        vm.flip()
        vm.flipAnimationComplete()
        assertEquals(1, vm.uiState.value.history.size)
        vm.clearHistory()
        assertTrue(vm.uiState.value.history.isEmpty())
    }

    @Test
    fun `toggleHistory toggles showHistory flag`() = runTest {
        val vm = CoinViewModel(repo, savedStateHandle)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.showHistory)
        vm.toggleHistory()
        assertTrue(vm.uiState.value.showHistory)
        vm.toggleHistory()
        assertFalse(vm.uiState.value.showHistory)
    }
}

private class FakeCoinRepo : CoinRepo {
    var coinCounts = mutableMapOf<String, Int>()
    var history: List<CoinFlip> = emptyList()

    override fun getCoinCount(gameId: String): Flow<Int> {
        return flowOf(coinCounts[gameId] ?: 1)
    }

    override suspend fun saveCoinCount(gameId: String, count: Int) {
        coinCounts[gameId] = count
    }

    override fun getHistory(): Flow<List<CoinFlip>> {
        return flowOf(history)
    }

    override suspend fun saveHistory(history: List<CoinFlip>) {
        this.history = history.take(10)
    }
}
