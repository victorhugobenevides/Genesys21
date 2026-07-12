package com.itbenevides.genesys21.screenshot

import app.cash.paparazzi.DeviceConfig
import com.itbenevides.genesys21.presentation.screens.editor.DesignSystemShowcaseScreen
import com.itbenevides.genesys21.screenshot.util.createGenesysPaparazzi
import org.junit.Rule
import org.junit.Test

class DesignSystemSnapshotTest {
    @get:Rule
    val paparazzi = createGenesysPaparazzi(deviceConfig = DeviceConfig.PIXEL_5)

    @Test
    fun testShowcaseFullPage() {
        paparazzi.snapshot {
            DesignSystemShowcaseScreen(onBack = {}, onOpenEditorShowcase = {})
        }
    }
}
