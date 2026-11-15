package com.turnzero.tools.rules

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject

/** Reads raw rule content from app assets. Returns `null` when [path] cannot be read. */
interface RulesAssetSource {
    fun read(path: String): String?
}

class AndroidRulesAssetSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : RulesAssetSource {

    override fun read(path: String): String? = try {
        context.assets.open(path).bufferedReader().use { it.readText() }
    } catch (e: IOException) {
        null
    }
}
