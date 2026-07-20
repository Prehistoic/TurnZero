package com.turnzero.di

import com.turnzero.core.tools.api.Tool
import com.turnzero.games.yugioh.tools.lifecounter.YuGiOhLifeCounter
import com.turnzero.games.yugioh.tools.rulingssearcher.YuGiOhRulingsSearcher
import com.turnzero.tools.coin.CoinTool
import com.turnzero.tools.dice.DiceTool
import com.turnzero.tools.rules.RulesTool
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ToolModule {

    @Provides
    @Singleton
    @IntoSet
    fun provideDiceTool(): Tool = DiceTool()

    @Provides
    @Singleton
    @IntoSet
    fun provideCoinTool(): Tool = CoinTool()

    @Provides
    @Singleton
    @IntoSet
    fun provideRulesTool(): Tool = RulesTool()

    @Provides
    @Singleton
    @IntoSet
    fun provideYuGiOhLifeCounter(): Tool = YuGiOhLifeCounter()

    @Provides
    @Singleton
    @IntoSet
    fun provideYuGiOhRulingsSearcher(): Tool = YuGiOhRulingsSearcher()
}
