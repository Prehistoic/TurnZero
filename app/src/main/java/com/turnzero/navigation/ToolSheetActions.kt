package com.turnzero.navigation

import androidx.compose.runtime.staticCompositionLocalOf

data class ToolSheetActions(
    val onOpenTools: () -> Unit,
    val onOpenOptions: () -> Unit
)

val LocalToolSheetActions = staticCompositionLocalOf<ToolSheetActions?> { null }
