package com.turnzero.core.tools.api

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

interface Tool {
    val id: String
    val name: String
    val icon: ImageVector
    val supportedGames: Set<String>
    val showBottomBar: Boolean get() = true
    val providesOwnTopBar: Boolean get() = false

    @Composable
    fun Screen(onBack: () -> Unit)

    @Composable
    fun OptionsSheet(onDismiss: () -> Unit) {}

    @Composable
    fun TopBarActions(): @Composable RowScope.() -> Unit = {}
}
