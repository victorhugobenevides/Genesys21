package com.itbenevides.genesys21.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class GenesysWindowSizeClass {
    COMPACT, // Phone (up to 600dp)
    MEDIUM,  // Tablet/Small Laptop (600dp to 840dp)
    EXPANDED // Desktop/Large Tablet (840dp+)
}

val LocalWindowSizeClass = compositionLocalOf { GenesysWindowSizeClass.COMPACT }

/**
 * Local que indica se o App está rodando em ambiente de teste (como Paparazzi ou Testes de UI).
 * Útil para desabilitar componentes que dependem de APIs do sistema que crasham em teste.
 */
val LocalTestMode = compositionLocalOf { false }

@Composable
fun ProvideWindowSizeClass(width: Dp, content: @Composable () -> Unit) {
    val sizeClass = when {
        width < 600.dp -> GenesysWindowSizeClass.COMPACT
        width < 840.dp -> GenesysWindowSizeClass.MEDIUM
        else -> GenesysWindowSizeClass.EXPANDED
    }

    CompositionLocalProvider(LocalWindowSizeClass provides sizeClass) {
        content()
    }
}
