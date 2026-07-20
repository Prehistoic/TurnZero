package com.turnzero.di

import com.turnzero.core.tools.api.Game
import com.turnzero.games.digimon.DigimonGame
import com.turnzero.games.fab.FabGame
import com.turnzero.games.mtg.MtgGame
import com.turnzero.games.onepiece.OnePieceGame
import com.turnzero.games.pokemon.PokemonGame
import com.turnzero.games.swu.SwuGame
import com.turnzero.games.vanguard.VanguardGame
import com.turnzero.games.yugioh.YuGiOhGame
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GameModule {

    @Provides
    @Singleton
    @IntoSet
    fun provideYuGiOhGame(): Game = YuGiOhGame

    @Provides
    @Singleton
    @IntoSet
    fun provideMtgGame(): Game = MtgGame

    @Provides
    @Singleton
    @IntoSet
    fun provideSwuGame(): Game = SwuGame

    @Provides
    @Singleton
    @IntoSet
    fun provideDigimonGame(): Game = DigimonGame

    @Provides
    @Singleton
    @IntoSet
    fun provideFabGame(): Game = FabGame

    @Provides
    @Singleton
    @IntoSet
    fun provideVanguardGame(): Game = VanguardGame

    @Provides
    @Singleton
    @IntoSet
    fun providePokemonGame(): Game = PokemonGame

    @Provides
    @Singleton
    @IntoSet
    fun provideOnePieceGame(): Game = OnePieceGame
}
