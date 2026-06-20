package com.turnzero.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

data class ColorTheme(
    val id: String,
    val name: String,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val surface: Color,
    val surfaceContainer: Color,
    val onSurface: Color
) {
    fun toColorScheme(darkTheme: Boolean): ColorScheme {
        return if (darkTheme) {
            darkColorScheme(
                primary = primary,
                onPrimary = Color.White,
                primaryContainer = primary.copy(alpha = 0.3f),
                onPrimaryContainer = primary.copy(alpha = 0.9f),
                secondary = secondary,
                onSecondary = Color.White,
                secondaryContainer = secondary.copy(alpha = 0.3f),
                onSecondaryContainer = secondary.copy(alpha = 0.9f),
                tertiary = tertiary,
                onTertiary = Color.White,
                tertiaryContainer = tertiary.copy(alpha = 0.3f),
                onTertiaryContainer = tertiary.copy(alpha = 0.9f),
                background = Color(0xFF12121A),
                surface = Color(0xFF12121A),
                surfaceContainer = Color(0xFF1E1E2A),
                onBackground = Color(0xFFE6E6F0),
                onSurface = Color(0xFFE6E6F0)
            )
        } else {
            lightColorScheme(
                primary = primary,
                onPrimary = Color.White,
                primaryContainer = primary.copy(alpha = 0.15f),
                onPrimaryContainer = primary,
                secondary = secondary,
                onSecondary = Color.White,
                secondaryContainer = secondary.copy(alpha = 0.15f),
                onSecondaryContainer = secondary,
                tertiary = tertiary,
                onTertiary = Color.White,
                tertiaryContainer = tertiary.copy(alpha = 0.15f),
                onTertiaryContainer = tertiary,
                background = surface,
                surface = surface,
                surfaceContainer = surfaceContainer,
                onBackground = onSurface,
                onSurface = onSurface
            )
        }
    }
}

val colorThemes = listOf(
    ColorTheme(
        id = "purple",
        name = "Purple",
        primary = Color(0xFF6650a4),
        secondary = Color(0xFF625b71),
        tertiary = Color(0xFF7D5260),
        surface = Color(0xFFF5F5FA),
        surfaceContainer = Color(0xFFE8E8F0),
        onSurface = Color(0xFF1C1B2E)
    ),
    ColorTheme(
        id = "ocean",
        name = "Ocean",
        primary = Color(0xFF1565C0),
        secondary = Color(0xFF42A5F5),
        tertiary = Color(0xFF26C6DA),
        surface = Color(0xFFF0F4F8),
        surfaceContainer = Color(0xFFE3ECF5),
        onSurface = Color(0xFF1A2332)
    ),
    ColorTheme(
        id = "forest",
        name = "Forest",
        primary = Color(0xFF2E7D32),
        secondary = Color(0xFF66BB6A),
        tertiary = Color(0xFF81C784),
        surface = Color(0xFFF1F5F0),
        surfaceContainer = Color(0xFFE3EDE2),
        onSurface = Color(0xFF1A2E1A)
    ),
    ColorTheme(
        id = "sunset",
        name = "Sunset",
        primary = Color(0xFFE65100),
        secondary = Color(0xFFFF7043),
        tertiary = Color(0xFFFFAB40),
        surface = Color(0xFFFDF5F0),
        surfaceContainer = Color(0xFFF5E6DC),
        onSurface = Color(0xFF2E1A0A)
    ),
    ColorTheme(
        id = "crimson",
        name = "Crimson",
        primary = Color(0xFFC62828),
        secondary = Color(0xFFEF5350),
        tertiary = Color(0xFFFF8A80),
        surface = Color(0xFFFDF5F5),
        surfaceContainer = Color(0xFFF5E8E8),
        onSurface = Color(0xFF2E1A1A)
    ),
    ColorTheme(
        id = "rose",
        name = "Rose",
        primary = Color(0xFFC2185B),
        secondary = Color(0xFFF06292),
        tertiary = Color(0xFFF48FB1),
        surface = Color(0xFFFDF5F8),
        surfaceContainer = Color(0xFFF5E6EC),
        onSurface = Color(0xFF2E1A22)
    )
)
