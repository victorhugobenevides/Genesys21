package com.itbenevides.genesys21.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf

data class GenesysThemeConfig(
    val cornerRadius: Int = 16,
    val glassIntensity: Float = 0.1f,
)

val LocalGenesysThemeConfig = staticCompositionLocalOf { GenesysThemeConfig() }
