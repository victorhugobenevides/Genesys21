package com.itbenevides.genesys21.screenshot

import app.cash.paparazzi.DeviceConfig
import com.itbenevides.genesys21.presentation.screens.editor.DesignSystemShowcaseScreen
import com.itbenevides.genesys21.presentation.screens.editor.TemplateShowcaseScreen
import com.itbenevides.genesys21.screenshot.util.createGenesysPaparazzi
import com.itbenevides.genesys21.screenshot.util.genesysSnapshot
import org.junit.Rule
import org.junit.Test

class DesignSystemSnapshotTest {
    @get:Rule
    val paparazzi = createGenesysPaparazzi(deviceConfig = app.cash.paparazzi.DeviceConfig.PIXEL_5)

    @Test
    fun testShowcaseFullPage() {
        paparazzi.genesysSnapshot {
            DesignSystemShowcaseScreen(onBack = {}, onOpenEditorShowcase = {}, onOpenTemplateShowcase = {})
        }
    }

    @Test
    fun testTemplateShowcase() {
        paparazzi.genesysSnapshot {
            TemplateShowcaseScreen(onBack = {})
        }
    }
}
