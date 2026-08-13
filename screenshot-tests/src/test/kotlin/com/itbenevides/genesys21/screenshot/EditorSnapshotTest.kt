package com.itbenevides.genesys21.screenshot

import com.itbenevides.genesys21.domain.model.CustomThemeConfig
import com.itbenevides.genesys21.domain.model.PageThemeConfig
import com.itbenevides.genesys21.domain.model.TypographySet
import com.itbenevides.genesys21.presentation.screens.editor.ThemeLabDialog
import com.itbenevides.genesys21.presentation.screens.editor.ThemeSelectorBottomSheet
import com.itbenevides.genesys21.screenshot.util.createGenesysPaparazzi
import com.itbenevides.genesys21.screenshot.util.genesysResponsiveSnapshot
import org.junit.Rule
import org.junit.Test

class EditorSnapshotTest {
    @get:Rule
    val paparazzi = createGenesysPaparazzi()

    @Test
    fun testThemeLabDialogResponsive() {
        paparazzi.genesysResponsiveSnapshot {
            ThemeLabDialog(
                initialConfig =
                    CustomThemeConfig(
                        primaryColor = "#2CB1FF",
                        cornerRadius = 24,
                        glassIntensity = 0.3f,
                        typographySet = TypographySet.MODERN_SANS,
                    ),
                onSave = {},
                onDismiss = {},
            )
        }
    }

    @Test
    fun testThemeSelectorBottomSheetResponsive() {
        paparazzi.genesysResponsiveSnapshot {
            ThemeSelectorBottomSheet(
                currentTheme = PageThemeConfig.ELEGANCE,
                onThemeSelected = {},
                onDismiss = {}
            )
        }
    }
}
