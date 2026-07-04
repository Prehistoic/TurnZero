package com.turnzero.di

import com.turnzero.games.yugioh.tools.lifecounter.DataStoreLifeCounterRepo
import com.turnzero.games.yugioh.tools.lifecounter.LifeCounterRepo
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LifeCounterModule {
    @Binds
    @Singleton
    abstract fun bindLifeCounterRepo(impl: DataStoreLifeCounterRepo): LifeCounterRepo
}
