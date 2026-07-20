package com.turnzero.di

import com.turnzero.core.tools.api.Game
import com.turnzero.core.tools.api.Tool
import com.turnzero.core.tools.api.ToolRegistry
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ToolRegistryModule {

    @Provides
    @Singleton
    fun provideToolRegistry(
        games: Set<@JvmSuppressWildcards Game>,
        tools: Set<@JvmSuppressWildcards Tool>,
    ): ToolRegistry = ToolRegistry(games, tools)
}
