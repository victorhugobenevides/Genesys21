package com.itbenevides.genesys21.screenshot.base

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.PageThemeConfig
import com.itbenevides.genesys21.ui.theme.AppTheme

/**
 * Base class aprimorada com suporte a acessibilidade e múltiplas configurações.
 */
abstract class EnhancedPaparazziTestBase : PaparazziTestBase() {

    /**
     * Snapshot com escala de fonte customizada para testes de acessibilidade.
     */
    fun snapshotWithFontScale(
        name: String,
        fontScale: Float,
        theme: PageThemeConfig = PageThemeConfig.ROYAL,
        content: @Composable () -> Unit
    ) {
        paparazzi.snapshot(name = name) {
            CompositionLocalProvider(
                LocalDensity provides Density(
                    density = LocalDensity.current.density,
                    fontScale = fontScale
                )
            ) {
                AppTheme(themeConfig = theme) {
                    Surface(
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        content()
                    }
                }
            }
        }
    }

    /**
     * Gera snapshots em todas as escalas de fonte comuns.
     */
    fun snapshotAllFontScales(
        baseName: String,
        theme: PageThemeConfig = PageThemeConfig.ROYAL,
        content: @Composable () -> Unit
    ) {
        listOf(
            0.85f to "small",
            1.0f to "default",
            1.15f to "large",
            1.3f to "xlarge"
        ).forEach { (scale, label) ->
            snapshotWithFontScale(
                name = "${baseName}_font_${label}",
                fontScale = scale,
                theme = theme,
                content = content
            )
        }
    }

    /**
     * Testa um componente em múltiplos estados (enabled/disabled, etc).
     */
    fun snapshotStates(
        baseName: String,
        states: Map<String, @Composable () -> Unit>
    ) {
        states.forEach { (stateName, content) ->
            snapshot(
                name = "${baseName}_state_${stateName}",
                content = content
            )
        }
    }
}
