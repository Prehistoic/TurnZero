package com.turnzero.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer

class HistoryRepository<T : Any>(
    private val dataStore: DataStore<Preferences>,
    private val key: Preferences.Key<String>,
    private val serializer: KSerializer<T>,
    private val maxSize: Int = 10,
) {
    private val listSerializer = ListSerializer(serializer)

    suspend fun get(): List<T> = dataStore.readJson(key, listSerializer, emptyList())

    fun observe(): Flow<List<T>> = dataStore.observeJson(key, emptyList(), listSerializer)

    suspend fun append(item: T): List<T> {
        val current = get()
        val updated = (listOf(item) + current).take(maxSize)
        dataStore.writeJson(key, updated)
        return updated
    }

    suspend fun clear() {
        dataStore.writeJson(key, emptyList<T>())
    }
}
