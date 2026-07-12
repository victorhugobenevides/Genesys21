package com.itbenevides.genesys21.screenshot

import app.cash.paparazzi.DeviceConfig
import com.itbenevides.genesys21.domain.model.CustomThemeConfig
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.TypographySet
import com.itbenevides.genesys21.presentation.screens.editor.ThemeLabDialog
import com.itbenevides.genesys21.screenshot.util.createGenesysPaparazzi
import com.itbenevides.genesys21.screenshot.util.genesysSnapshot
import org.junit.Rule
import org.junit.Test

class EditorSnapshotTest {
    @get:Rule
    val paparazzi = createGenesysPaparazzi(deviceConfig = DeviceConfig.PIXEL_5)

    @Test
    fun testThemeLabDialog() {
        paparazzi.genesysSnapshot {
            ThemeLabDialog(
                initialConfig = CustomThemeConfig(
                    primaryColor = "#2CB1FF",
                    cornerRadius = 24,
                    glassIntensity = 0.3f,
                    typographySet = TypographySet.MODERN_SANS
                ),
                onSave = {},
                onDismiss = {}
            )
        }
    }
}
