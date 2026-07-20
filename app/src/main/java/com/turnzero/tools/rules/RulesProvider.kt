package com.turnzero.tools.rules

import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryRulesProvider @Inject constructor() : RulesProvider {

    private val registry = ConcurrentHashMap<String, List<RulesConfig>>()

    fun register(gameId: String, configs: List<RulesConfig>) {
        registry[gameId] = configs
    }

    override fun getConfigs(gameId: String): List<RulesConfig> {
        return registry[gameId] ?: emptyList()
    }
}
