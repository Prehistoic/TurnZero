package com.turnzero.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

@PublishedApi internal val json = Json { ignoreUnknownKeys = true }

suspend inline fun <reified T> DataStore<Preferences>.readJson(
    key: Preferences.Key<String>,
    default: T
): T {
    val raw = data.map { it[key] }.first() ?: return default
    return try {
        json.decodeFromString<T>(raw)
    } catch (_: Exception) {
        default
    }
}

suspend inline fun <reified T> DataStore<Preferences>.readJsonOrNull(
    key: Preferences.Key<String>
): T? {
    val raw = data.map { it[key] }.first() ?: return null
    return try {
        json.decodeFromString<T>(raw)
    } catch (_: Exception) {
        null
    }
}

fun <T> DataStore<Preferences>.observeJson(
    key: Preferences.Key<String>,
    default: T,
    serializer: KSerializer<T>
): Flow<T> {
    return data.map { prefs ->
        val raw = prefs[key]
        if (raw != null) {
            try {
                json.decodeFromString(serializer, raw)
            } catch (_: Exception) {
                default
            }
        } else {
            default
        }
    }
}

suspend inline fun <reified T> DataStore<Preferences>.writeJson(
    key: Preferences.Key<String>,
    value: T
) {
    edit { prefs -> prefs[key] = json.encodeToString(value) }
}

suspend fun DataStore<Preferences>.readString(
    key: Preferences.Key<String>,
    default: String = ""
): String {
    return data.map { it[key] }.first() ?: default
}

suspend fun DataStore<Preferences>.writeString(
    key: Preferences.Key<String>,
    value: String
) {
    edit { prefs -> prefs[key] = value }
}

fun DataStore<Preferences>.observeString(
    key: Preferences.Key<String>,
    default: String = ""
): Flow<String> {
    return data.map { it[key] ?: default }
}

suspend inline fun <reified T : Enum<T>> DataStore<Preferences>.readEnum(
    key: Preferences.Key<String>,
    default: T
): T {
    val raw = data.map { it[key] }.first() ?: return default
    return try {
        enumValueOf<T>(raw)
    } catch (_: Exception) {
        default
    }
}

suspend fun <T : Enum<T>> DataStore<Preferences>.writeEnum(
    key: Preferences.Key<String>,
    value: T
) {
    edit { prefs -> prefs[key] = value.name }
}

suspend fun DataStore<Preferences>.readInt(
    key: Preferences.Key<String>,
    default: Int = 0
): Int {
    return data.map { it[key]?.toIntOrNull() }.first() ?: default
}

suspend fun DataStore<Preferences>.writeInt(
    key: Preferences.Key<String>,
    value: Int
) {
    edit { prefs -> prefs[key] = value.toString() }
}

fun DataStore<Preferences>.observeInt(
    key: Preferences.Key<String>,
    default: Int = 0
): Flow<Int> {
    return data.map { it[key]?.toIntOrNull() ?: default }
}
