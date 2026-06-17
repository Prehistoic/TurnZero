package com.turnzero.core.tools.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

interface Tool {
    val id: String
    val name: String
    val icon: ImageVector
    val supportedGames: Set<String>

    @Composable
    fun Screen(onBack: () -> Unit)
}
