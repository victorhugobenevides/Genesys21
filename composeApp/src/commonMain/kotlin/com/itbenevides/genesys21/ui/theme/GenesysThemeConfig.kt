package com.itbenevides.genesys21.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import com.itbenevides.genesys21.domain.model.CustomThemeConfig

data class GenesysThemeConfig(
    val cornerRadius: Int = 16,
    val glassIntensity: Float = 0.1f,
)

val LocalGenesysThemeConfig = staticCompositionLocalOf { GenesysThemeConfig() }
