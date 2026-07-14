package com.turnzero

import android.app.Application
import com.turnzero.games.fab.rules.FaBRules
import com.turnzero.games.swu.rules.SwuRules
import com.turnzero.games.yugioh.rules.YuGiOhRules
import com.turnzero.tools.rules.InMemoryRulesProvider
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TZApplication : Application() {

    @Inject
    lateinit var rulesProvider: InMemoryRulesProvider

    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
        rulesProvider.register("yugioh", YuGiOhRules.configs)
        rulesProvider.register("swu", SwuRules.configs)
        rulesProvider.register("fab", FaBRules.configs)
    }
}
