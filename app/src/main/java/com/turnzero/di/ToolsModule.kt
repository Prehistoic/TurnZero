package com.turnzero.di

import com.turnzero.core.tools.api.Tool
import com.turnzero.games.yugioh.YuGiOhLifeCounter
import com.turnzero.games.yugioh.YuGiOhRulingsSearcher
import com.turnzero.tools.dice.DiceTool
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class ToolsModule {

    @Binds
    @IntoSet
    abstract fun bindDiceTool(dice: DiceTool): Tool

    @Binds
    @IntoSet
    abstract fun bindYuGiOhLifeCounter(lp: YuGiOhLifeCounter): Tool

    @Binds
    @IntoSet
    abstract fun bindYuGiOhRulingsSearcher(rs: YuGiOhRulingsSearcher): Tool
}
