package com.itbenevides.genesys21.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Design Tokens: Cores Semânticas do Genesys21.
 * Abstrai a implementação do Material 3 para uma linguagem de marca proprietária.
 */
@Immutable
data class GenesysColors(
    val brand: Color,
    val onBrand: Color,
    val brandContainer: Color,
    val onBrandContainer: Color,
    val accent: Color,
    val onAccent: Color,
    val background: Color,
    val onBackground: Color,
    val backgroundSecondary: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val success: Color,
    val onError: Color,
    val error: Color,
    val errorContainer: Color,
    val isDark: Boolean
)

val LocalGenesysColors = staticCompositionLocalOf<GenesysColors> {
    error("No GenesysColors provided")
}
