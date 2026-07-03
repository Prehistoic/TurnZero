package com.turnzero.tools.coin

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStoreCoinRepoTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `getCoinCount returns 1 when no data stored`() = runTest {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { tempFolder.newFile("test.preferences_pb") }
        )
        val repo = DataStoreCoinRepo(dataStore)
        val count = repo.getCoinCount("yugioh").first()
        assertEquals(1, count)
    }

    @Test
    fun `saveCoinCount and getCoinCount roundtrip`() = runTest {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { tempFolder.newFile("test.preferences_pb") }
        )
        val repo = DataStoreCoinRepo(dataStore)
        repo.saveCoinCount("yugioh", 5)
        val count = repo.getCoinCount("yugioh").first()
        assertEquals(5, count)
    }

    @Test
    fun `saveHistory and getHistory roundtrip`() = runTest {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { tempFolder.newFile("test.preferences_pb") }
        )
        val repo = DataStoreCoinRepo(dataStore)
        val flip = CoinFlip(
            id = "f1",
            timestamp = 1000L,
            coinCount = 2,
            results = listOf(CoinFace.HEAD, CoinFace.TAIL),
            headsCount = 1,
            tailsCount = 1
        )
        repo.saveHistory(listOf(flip))
        val history = repo.getHistory().first()
        assertEquals(1, history.size)
        assertEquals(1, history[0].headsCount)
        assertEquals(1, history[0].tailsCount)
    }

    @Test
    fun `saveHistory caps at 10 entries newest first`() = runTest {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { tempFolder.newFile("test.preferences_pb") }
        )
        val repo = DataStoreCoinRepo(dataStore)
        val flips = (1..12).map { i ->
            CoinFlip(
                id = "f$i",
                timestamp = i.toLong(),
                coinCount = 1,
                results = listOf(CoinFace.HEAD),
                headsCount = 1,
                tailsCount = 0
            )
        }
        repo.saveHistory(flips)
        val history = repo.getHistory().first()
        assertEquals(10, history.size)
        assertEquals("f1", history[0].id)
    }

    @Test
    fun `coin counts are isolated per gameId`() = runTest {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { tempFolder.newFile("test.preferences_pb") }
        )
        val repo = DataStoreCoinRepo(dataStore)
        repo.saveCoinCount("yugioh", 2)
        repo.saveCoinCount("mtg", 4)
        assertEquals(2, repo.getCoinCount("yugioh").first())
        assertEquals(4, repo.getCoinCount("mtg").first())
    }
}
