package com.turnzero.di

import com.turnzero.tools.coin.CoinRepo
import com.turnzero.tools.coin.DataStoreCoinRepo
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CoinModule {
    @Binds
    @Singleton
    abstract fun bindCoinRepo(impl: DataStoreCoinRepo): CoinRepo
}
