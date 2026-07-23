package com.turnzero.tools.lifecounter

import androidx.lifecycle.SavedStateHandle
import com.turnzero.TZViewModelTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeLpCounterRepo : LpCounterRepo {
    private var _config = LpCounterConfig()

    var savedConfig: LpCounterConfig? = null

    override suspend fun getConfig(): LpCounterConfig = _config
    override suspend fun saveConfig(config: LpCounterConfig) { savedConfig = config; _config = config }
}

@OptIn(ExperimentalCoroutinesApi::class)
class LpCounterViewModelTest : TZViewModelTest() {

    private lateinit var repo: FakeLpCounterRepo
    private lateinit var savedStateHandle: SavedStateHandle

    @Before
    fun setup() {
        repo = FakeLpCounterRepo()
        savedStateHandle = SavedStateHandle()
    }

    @Test
    fun `initial state has 2 players with game-specific defaults when initialized`() = runTest {
        val vm = LpCounterViewModel(repo, savedStateHandle)
        vm.initialize("mtg")
        val state = vm.uiState.value
        assertEquals(2, state.players.size)
        assertEquals(20, state.players[0].currentLp)
        assertEquals(20, state.players[1].currentLp)
        assertEquals(20, state.globalStartingLp)
    }

    @Test
    fun `initialize uses swu default of 30`() = runTest {
        val vm = LpCounterViewModel(repo, savedStateHandle)
        vm.initialize("swu")
        val state = vm.uiState.value
        assertEquals(30, state.globalStartingLp)
        assertEquals(30, state.players[0].currentLp)
    }

    @Test
    fun `initialize falls back to 20 for unknown game`() = runTest {
        val vm = LpCounterViewModel(repo, savedStateHandle)
        vm.initialize("unknown")
        val state = vm.uiState.value
        assertEquals(20, state.globalStartingLp)
    }

    @Test
    fun `incrementPlayer adds 1 to currentLp and records history`() = runTest {
        val vm = LpCounterViewModel(repo, savedStateHandle)
        vm.initialize("mtg")
        vm.incrementPlayer(1)
        val state = vm.uiState.value
        assertEquals(21, state.players[0].currentLp)
        assertEquals(1, state.changeHistory.size)
        assertEquals(20, state.changeHistory[0].previousLp)
        assertEquals(1, state.changeHistory[0].delta)
        assertEquals(21, state.changeHistory[0].newTotal)
        assertEquals(1, state.changeHistory[0].playerNumber)
    }

    @Test
    fun `decrementPlayer subtracts 1 from currentLp and records history`() = runTest {
        val vm = LpCounterViewModel(repo, savedStateHandle)
        vm.initialize("mtg")
        vm.decrementPlayer(1)
        val state = vm.uiState.value
        assertEquals(19, state.players[0].currentLp)
        assertEquals(-1, state.changeHistory[0].delta)
        assertEquals(19, state.changeHistory[0].newTotal)
    }

    @Test
    fun `lastChange accumulates while holding button`() = runTest {
        val vm = LpCounterViewModel(repo, savedStateHandle)
        vm.initialize("mtg")
        vm.incrementPlayer(1)
        vm.incrementPlayer(1)
        vm.incrementPlayer(1)
        assertEquals(3, vm.uiState.value.players[0].lastChange)
    }

    @Test
    fun `releasePlayer resets lastChange to zero`() = runTest {
        val vm = LpCounterViewModel(repo, savedStateHandle)
        vm.initialize("mtg")
        vm.incrementPlayer(1)
        vm.incrementPlayer(1)
        vm.releasePlayer(1)
        assertEquals(0, vm.uiState.value.players[0].lastChange)
    }

    @Test
    fun `resetPlayer restores starting LP and records full delta`() = runTest {
        val vm = LpCounterViewModel(repo, savedStateHandle)
        vm.initialize("mtg")
        vm.decrementPlayer(1) // 20 -> 19
        vm.decrementPlayer(1) // 19 -> 18
        vm.decrementPlayer(1) // 18 -> 17
        vm.releasePlayer(1)
        vm.resetPlayer(1)
        val state = vm.uiState.value
        assertEquals(20, state.players[0].currentLp)
        assertEquals(0, state.players[0].lastChange)
        val resetEntry = state.changeHistory.last()
        assertEquals(17, resetEntry.previousLp)
        assertEquals(3, resetEntry.delta)
        assertEquals(20, resetEntry.newTotal)
    }

    @Test
    fun `resetAll resets all players to starting LP`() = runTest {
        val vm = LpCounterViewModel(repo, savedStateHandle)
        vm.initialize("mtg")
        vm.decrementPlayer(1) // 20 -> 19
        vm.decrementPlayer(2) // 20 -> 19
        vm.releasePlayer(1)
        vm.releasePlayer(2)
        vm.resetAll()
        assertEquals(20, vm.uiState.value.players[0].currentLp)
        assertEquals(20, vm.uiState.value.players[1].currentLp)
    }

    @Test
    fun `setPlayerCount to 3 recreates players with global starting LP`() = runTest {
        val vm = LpCounterViewModel(repo, savedStateHandle)
        vm.initialize("mtg")
        vm.setPlayerCount(3)
        val state = vm.uiState.value
        assertEquals(3, state.players.size)
        assertEquals(20, state.players[0].currentLp)
        assertEquals(20, state.players[1].currentLp)
        assertEquals(20, state.players[2].currentLp)
    }

    @Test
    fun `setPlayerCount to 4 recreates 4 players`() = runTest {
        val vm = LpCounterViewModel(repo, savedStateHandle)
        vm.initialize("mtg")
        vm.setPlayerCount(4)
        assertEquals(4, vm.uiState.value.players.size)
    }

    @Test
    fun `setGlobalStartingLp updates all players starting and current LP`() = runTest {
        val vm = LpCounterViewModel(repo, savedStateHandle)
        vm.initialize("mtg")
        vm.setGlobalStartingLp(40)
        val state = vm.uiState.value
        assertEquals(40, state.globalStartingLp)
        assertEquals(40, state.players[0].startingLp)
        assertEquals(40, state.players[0].currentLp)
        assertEquals(40, state.players[1].startingLp)
        assertEquals(40, state.players[1].currentLp)
    }

    @Test
    fun `setPlayerStartingLp overrides individual player`() = runTest {
        val vm = LpCounterViewModel(repo, savedStateHandle)
        vm.initialize("mtg")
        vm.setPlayerStartingLp(2, 30)
        val state = vm.uiState.value
        assertEquals(20, state.players[0].startingLp)
        assertEquals(30, state.players[1].startingLp)
        assertEquals(30, state.players[1].currentLp)
    }

    @Test
    fun `showHistory toggles visibility`() = runTest {
        val vm = LpCounterViewModel(repo, savedStateHandle)
        vm.initialize("mtg")
        vm.showHistory(true)
        assertTrue(vm.uiState.value.showHistory)
        vm.showHistory(false)
        assertFalse(vm.uiState.value.showHistory)
    }

    @Test
    fun `clearHistory empties history`() = runTest {
        val vm = LpCounterViewModel(repo, savedStateHandle)
        vm.initialize("mtg")
        vm.incrementPlayer(1)
        vm.incrementPlayer(2)
        assertEquals(2, vm.uiState.value.changeHistory.size)
        vm.clearHistory()
        assertEquals(0, vm.uiState.value.changeHistory.size)
    }

    @Test
    fun `history is capped at 200 entries`() = runTest {
        val vm = LpCounterViewModel(repo, savedStateHandle)
        vm.initialize("mtg")
        repeat(250) { vm.incrementPlayer(1) }
        val history = vm.uiState.value.changeHistory
        assertTrue(history.size <= 200)
        assertEquals(250 + 20, vm.uiState.value.players[0].currentLp)
    }

    @Test
    fun `setPlayerCount persists config`() = runTest {
        val vm = LpCounterViewModel(repo, savedStateHandle)
        vm.initialize("mtg")
        vm.setPlayerCount(4)
        assertEquals(4, repo.savedConfig?.playerCount)
    }

    @Test
    fun `setGlobalStartingLp persists config`() = runTest {
        val vm = LpCounterViewModel(repo, savedStateHandle)
        vm.initialize("mtg")
        vm.setGlobalStartingLp(30)
        assertEquals(30, repo.savedConfig?.globalStartingLp)
    }

    @Test
    fun `setPlayerStartingLp persists per-player override`() = runTest {
        val vm = LpCounterViewModel(repo, savedStateHandle)
        vm.initialize("mtg")
        vm.setPlayerStartingLp(3, 30)
        assertEquals(mapOf(3 to 30), repo.savedConfig?.perPlayerStartingLp)
    }
}
