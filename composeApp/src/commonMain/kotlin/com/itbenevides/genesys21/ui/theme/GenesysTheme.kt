package com.itbenevides.genesys21.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

/**
 * Ponto de acesso global aos Design Tokens do Genesys21.
 * Ex: GenesysTheme.colors.brand
 */
object GenesysTheme {
    val colors: GenesysColors
        @Composable
        @ReadOnlyComposable
        get() = LocalGenesysColors.current

    val typography: GenesysTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalGenesysTypography.current

    val spacing: GenesysSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalGenesysSpacing.current

    val config: GenesysThemeConfig
        @Composable
        @ReadOnlyComposable
        get() = LocalGenesysThemeConfig.current
}
