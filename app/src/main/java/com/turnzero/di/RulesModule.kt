package com.turnzero.di

import com.turnzero.tools.rules.InMemoryRulesProvider
import com.turnzero.tools.rules.RulesProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RulesModule {

    @Binds
    @Singleton
    abstract fun bindRulesProvider(impl: InMemoryRulesProvider): RulesProvider
}
