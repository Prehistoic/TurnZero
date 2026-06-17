package com.turnzero.di

import com.turnzero.core.tools.api.Game
import com.turnzero.games.yugioh.YuGiOhGame
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
object GamesModule {

    @Provides
    @IntoSet
    fun provideYuGiOhGame(): Game = YuGiOhGame
}
