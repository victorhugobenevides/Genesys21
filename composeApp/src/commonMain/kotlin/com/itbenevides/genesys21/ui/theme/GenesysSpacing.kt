package com.itbenevides.genesys21.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Design Tokens: Escala de Espaçamento do Genesys21.
 * Elimina o uso de valores "mágicos" no layout.
 */
@Immutable
data class GenesysSpacing(
    val none: Dp = 0.dp,
    val xxxs: Dp = 2.dp,
    val xxs: Dp = 4.dp,
    val xs: Dp = 8.dp,
    val s: Dp = 12.dp,
    val m: Dp = 16.dp,
    val l: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val xxl: Dp = 48.dp,
    val huge: Dp = 64.dp
)

val LocalGenesysSpacing = staticCompositionLocalOf { GenesysSpacing() }
