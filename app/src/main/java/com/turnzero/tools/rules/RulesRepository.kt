package com.turnzero.tools.rules

import android.util.Log
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/** Loads the rule content for a game from its JSON asset folder. */
interface RulesRepository {
    /** Parsed rules for [gameId], or `null` when the folder is missing or malformed. */
    suspend fun getRules(gameId: String): GameRules?
}

@Singleton
class AssetRulesRepository @Inject constructor(
    private val source: RulesAssetSource,
) : RulesRepository {

    private val json = Json { ignoreUnknownKeys = true }

    private val cache = ConcurrentHashMap<String, GameRules>()

    override suspend fun getRules(gameId: String): GameRules? = withContext(Dispatchers.IO) {
        cache[gameId]?.let { return@withContext it }

        load(gameId)?.also { cache[gameId] = it }
    }

    /** Only non-null results are cached, so a broken or missing folder is retried on the next call. */
    private fun load(gameId: String): GameRules? {
        val base = "rules/$gameId"
        val manifest = source.read("$base/manifest.json")
        if (manifest == null) {
            Log.w(TAG, "Missing rules manifest '$base/manifest.json'")
            return null
        }

        val setup = source.read("$base/setup.json")
        val turn = source.read("$base/turn.json")
        val win = source.read("$base/win.json")
        val keywords = source.read("$base/keywords.json")
        if (setup == null || turn == null || win == null || keywords == null) {
            Log.w(TAG, "Incomplete rules folder '$base'")
            return null
        }

        return try {
            GameRules(
                manifest = json.decodeFromString<RulesManifest>(manifest),
                setup = json.decodeFromString<SetupContent>(setup),
                turn = json.decodeFromString<TurnContent>(turn),
                win = json.decodeFromString<WinContent>(win),
                keywords = json.decodeFromString<KeywordsContent>(keywords),
            )
        } catch (e: SerializationException) {
            Log.w(TAG, "Malformed rules JSON in '$base'", e)
            null
        }
    }

    private companion object {
        const val TAG = "TZ/Rules"
    }
}
