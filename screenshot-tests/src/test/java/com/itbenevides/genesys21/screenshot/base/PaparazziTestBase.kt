package com.itbenevides.genesys21.screenshot.base

import app.cash.paparazzi.Paparazzi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.PageThemeConfig
import com.itbenevides.genesys21.ui.theme.AppTheme
import org.junit.Rule

/**
 * Classe base para todos os testes de screenshot com Paparazzi.
 * Fornece configuração comum e utilitários para testes.
 */
abstract class PaparazziTestBase {

    @get:Rule
    open val paparazzi = Paparazzi()

    /**
     * Wrapper padrão para snapshots com tema e padding consistentes.
     */
    fun snapshot(
        name: String,
        theme: PageThemeConfig = PageThemeConfig.ROYAL,
        padding: Int = 16,
        content: @Composable () -> Unit
    ) {
        paparazzi.snapshot(name = name) {
            AppTheme(themeConfig = theme) {
                Surface(
                    modifier = Modifier.padding(padding.dp),
                    color = MaterialTheme.colorScheme.background
                ) {
                    content()
                }
            }
        }
    }

    /**
     * Wrapper para snapshots sem padding (para testes de layout responsivo).
     */
    fun snapshotFullBleed(
        name: String,
        theme: PageThemeConfig = PageThemeConfig.ROYAL,
        content: @Composable () -> Unit
    ) {
        paparazzi.snapshot(name = name) {
            AppTheme(themeConfig = theme) {
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    content()
                }
            }
        }
    }

    /**
     * Gera múltiplos snapshots para diferentes temas.
     */
    fun snapshotAllThemes(
        baseName: String,
        themes: List<PageThemeConfig> = listOf(
            PageThemeConfig.ROYAL,
            PageThemeConfig.OCEAN,
            PageThemeConfig.FOREST,
            PageThemeConfig.DARK_MODE
        ),
        content: @Composable (PageThemeConfig) -> Unit
    ) {
        themes.forEach { theme ->
            snapshot(
                name = "${baseName}_theme_${theme.name.lowercase()}",
                theme = theme
            ) {
                content(theme)
            }
        }
    }

    companion object {
        /**
         * URL de imagem placeholder para testes (mock local).
         */
        val PLACEHOLDER_IMAGE = TestImageProvider.mockImageUrl()
        val PLACEHOLDER_IMAGE_LARGE = TestImageProvider.mockImageUrl()
        val PLACEHOLDER_AVATAR = TestImageProvider.mockImageUrl()
    }
}
