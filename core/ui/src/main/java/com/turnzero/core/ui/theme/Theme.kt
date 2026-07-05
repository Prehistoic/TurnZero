package com.turnzero.core.ui.theme

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowInsetsController
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView

@Composable
fun TZTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorTheme: ColorTheme? = null,
    content: @Composable () -> Unit
) {
    val colorScheme = if (colorTheme != null) {
        colorTheme.toColorScheme(darkTheme)
    } else {
        val fallback = colorThemes.first()
        if (darkTheme) fallback.toColorScheme(darkTheme = true) else fallback.toColorScheme(darkTheme = false)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.setSystemBarsAppearance(
                    if (darkTheme) 0 else WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = if (darkTheme) {
                    window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                } else {
                    window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TZTypography,
        content = content
    )
}
