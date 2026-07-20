package com.turnzero

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.crossfade
import com.turnzero.games.digimon.rules.DigimonRules
import com.turnzero.games.fab.rules.FaBRules
import com.turnzero.games.mtg.rules.MagicRules
import com.turnzero.games.onepiece.rules.OnePieceRules
import com.turnzero.games.pokemon.rules.PokemonRules
import com.turnzero.games.swu.rules.SwuRules
import com.turnzero.games.yugioh.rules.YuGiOhRules
import com.turnzero.tools.rules.InMemoryRulesProvider
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TZApplication : Application(), SingletonImageLoader.Factory {

    @Inject
    lateinit var rulesProvider: InMemoryRulesProvider

    override fun newImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("coil_cache"))
                    .maxSizePercent(0.05)
                    .build()
            }
            .crossfade(true)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        rulesProvider.register("yugioh", YuGiOhRules.configs)
        rulesProvider.register("swu", SwuRules.configs)
        rulesProvider.register("fab", FaBRules.configs)
        rulesProvider.register("onepiece", OnePieceRules.configs)
        rulesProvider.register("digimon", DigimonRules.configs)
        rulesProvider.register("pokemon", PokemonRules.configs)
        rulesProvider.register("mtg", MagicRules.configs)
    }
}
