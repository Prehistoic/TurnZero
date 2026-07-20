package com.turnzero.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

abstract class JsonDataStoreRepo(
    protected val dataStore: DataStore<Preferences>,
) {
    protected suspend inline fun <reified T> readJson(key: Preferences.Key<String>, default: T): T =
        dataStore.readJson(key, default)

    protected suspend inline fun <reified T> writeJson(key: Preferences.Key<String>, value: T) =
        dataStore.writeJson(key, value)

    protected suspend inline fun <reified T : Enum<T>> readEnum(key: Preferences.Key<String>, default: T): T =
        dataStore.readEnum(key, default)

    protected suspend fun <T : Enum<T>> writeEnum(key: Preferences.Key<String>, value: T) =
        dataStore.writeEnum(key, value)

    protected suspend fun readInt(key: Preferences.Key<String>, default: Int = 0): Int =
        dataStore.readInt(key, default)

    protected suspend fun writeInt(key: Preferences.Key<String>, value: Int) =
        dataStore.writeInt(key, value)
}
