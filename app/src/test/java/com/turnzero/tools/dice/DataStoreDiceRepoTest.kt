package com.turnzero.tools.dice

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreDiceRepoTest {

    @get:Rule
    val tempDir = TemporaryFolder()

    @Test
    fun `getPool returns default white die when no data stored`() = runTest {
        val ds = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { File(tempDir.newFolder(), "test_prefs.preferences_pb") }
        )
        val repo = DataStoreDiceRepo(ds)
        val pool = repo.getPool("testgame")
        assertEquals(1, pool.size)
        assertEquals(6, pool[0].faceCount)
        assertEquals(DiceColor.WHITE, pool[0].color)
    }

    @Test
    fun `savePool and getPool roundtrip`() = runTest {
        val ds = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { File(tempDir.newFolder(), "test_prefs.preferences_pb") }
        )
        val repo = DataStoreDiceRepo(ds)
        val dice = listOf(
            Die(id = "a", faceCount = 6, color = DiceColor.RED),
            Die(id = "b", faceCount = 20, color = DiceColor.BLUE)
        )
        repo.savePool("testgame", dice)
        val loaded = repo.getPool("testgame")
        assertEquals(2, loaded.size)
        assertEquals(6, loaded[0].faceCount)
        assertEquals(DiceColor.RED, loaded[0].color)
        assertEquals(20, loaded[1].faceCount)
    }

    @Test
    fun `saveHistory caps at 10 entries newest first`() = runTest {
        val ds = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { File(tempDir.newFolder(), "test_prefs.preferences_pb") }
        )
        val repo = DataStoreDiceRepo(ds)
        val rolls = (1..15).reversed().map { i ->
            DiceRoll(
                id = "r$i", timestamp = i.toLong(),
                dice = listOf(Die(id = "d", faceCount = 6, color = DiceColor.RED)),
                results = listOf(i), total = i
            )
        }
        repo.saveHistory(rolls)
        val saved = repo.getHistory()
        assertEquals(10, saved.size)
        assertEquals("r15", saved.first().id)
        assertEquals("r6", saved.last().id)
    }

    @Test
    fun `saveSelectedColor and getSelectedColor roundtrip`() = runTest {
        val ds = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { File(tempDir.newFolder(), "test_prefs.preferences_pb") }
        )
        val repo = DataStoreDiceRepo(ds)
        repo.saveSelectedColor(DiceColor.GREEN)
        assertEquals(DiceColor.GREEN, repo.getSelectedColor())
    }

    @Test
    fun `pools are isolated per gameId`() = runTest {
        val ds = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { File(tempDir.newFolder(), "test_prefs.preferences_pb") }
        )
        val repo = DataStoreDiceRepo(ds)
        repo.savePool("gameA", listOf(Die(id = "a", faceCount = 6, color = DiceColor.RED)))
        repo.savePool("gameB", listOf(Die(id = "b", faceCount = 20, color = DiceColor.BLUE)))
        assertEquals(1, repo.getPool("gameA").size)
        assertEquals(6, repo.getPool("gameA")[0].faceCount)
        assertEquals(20, repo.getPool("gameB")[0].faceCount)
    }
}
