package com.itbenevides.genesys21.screenshot

import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.PageThemeConfig
import com.itbenevides.genesys21.presentation.screens.viewer.PageViewerContent
import com.itbenevides.genesys21.presentation.screens.viewer.PageViewerScreenState
import com.itbenevides.genesys21.screenshot.util.createGenesysPaparazzi
import com.itbenevides.genesys21.screenshot.util.genesysResponsiveSnapshot
import com.itbenevides.genesys21.ui.util.GenesysWindowSizeClass
import com.itbenevides.genesys21.ui.util.LocalWindowSizeClass
import org.junit.Rule
import org.junit.Test

class ThemesSnapshotTest {
    @get:Rule
    val paparazzi = createGenesysPaparazzi()

    @Test
    fun testAllThemesVisualIntegrity() {
        val themes = PageThemeConfig.entries.filter { it != PageThemeConfig.DEFAULT }

        val basePage = Page(
            id = "theme-test",
            storeId = "test-store",
            title = "Teste de Tema",
            theme = PageThemeConfig.ELEGANCE,
            components = emptyList()
        )

        themes.forEach { theme ->
            genesysResponsiveSnapshot(paparazzi, "Theme_${theme.name}") {
                val windowSizeClass = LocalWindowSizeClass.current
                val isCompact = windowSizeClass == GenesysWindowSizeClass.COMPACT

                com.itbenevides.genesys21.ui.theme.AppTheme(themeConfig = theme) {
                    PageViewerContent(
                        state = PageViewerScreenState(page = basePage.copy(theme = theme)),
                        currentFilterQuery = "",
                        isCompact = isCompact,
                        onEvent = {}
                    )
                }
            }
        }
    }
}
