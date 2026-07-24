package com.turnzero.di

import com.turnzero.tools.lifecounter.DataStoreLpCounterRepo
import com.turnzero.tools.lifecounter.LpCounterRepo
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LpCounterModule {
    @Binds
    @Singleton
    abstract fun bindLpCounterRepo(impl: DataStoreLpCounterRepo): LpCounterRepo
}
