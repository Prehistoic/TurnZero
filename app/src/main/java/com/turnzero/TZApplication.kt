package com.turnzero

import android.app.Application
import com.turnzero.games.yugioh.YuGiOhRules
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
    }
}
