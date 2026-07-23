package com.turnzero.tools.lifecounter

interface LpCounterRepo {
    suspend fun getConfig(): LpCounterConfig
    suspend fun saveConfig(config: LpCounterConfig)
}
