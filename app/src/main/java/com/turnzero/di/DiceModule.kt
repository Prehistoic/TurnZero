package com.turnzero.di

import com.turnzero.tools.dice.DataStoreDiceRepo
import com.turnzero.tools.dice.DiceRepo
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DiceModule {
    @Binds
    @Singleton
    abstract fun bindDiceRepo(impl: DataStoreDiceRepo): DiceRepo
}
