package com.turnzero.di

import com.turnzero.tools.rules.AndroidRulesAssetSource
import com.turnzero.tools.rules.AssetRulesRepository
import com.turnzero.tools.rules.InMemoryRulesProvider
import com.turnzero.tools.rules.RulesAssetSource
import com.turnzero.tools.rules.RulesProvider
import com.turnzero.tools.rules.RulesRepository
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

    @Binds
    @Singleton
    abstract fun bindRulesAssetSource(impl: AndroidRulesAssetSource): RulesAssetSource

    @Binds
    @Singleton
    abstract fun bindRulesRepository(impl: AssetRulesRepository): RulesRepository
}
