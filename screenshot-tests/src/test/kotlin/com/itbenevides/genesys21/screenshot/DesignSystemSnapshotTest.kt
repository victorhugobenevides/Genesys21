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
    fun testShowcaseArchitecture() {
        paparazzi.genesysSnapshot {
            DesignSystemShowcaseScreen(onBack = {}, onOpenEditorShowcase = {}, onOpenTemplateShowcase = {}, initialTab = 0)
        }
    }

    @Test
    fun testShowcaseFoundation() {
        paparazzi.genesysSnapshot {
            DesignSystemShowcaseScreen(onBack = {}, onOpenEditorShowcase = {}, onOpenTemplateShowcase = {}, initialTab = 1)
        }
    }

    @Test
    fun testShowcaseInputs() {
        paparazzi.genesysSnapshot {
            DesignSystemShowcaseScreen(onBack = {}, onOpenEditorShowcase = {}, onOpenTemplateShowcase = {}, initialTab = 2)
        }
    }

    @Test
    fun testShowcaseActionNav() {
        paparazzi.genesysSnapshot {
            DesignSystemShowcaseScreen(onBack = {}, onOpenEditorShowcase = {}, onOpenTemplateShowcase = {}, initialTab = 3)
        }
    }

    @Test
    fun testShowcaseDisplay() {
        paparazzi.genesysSnapshot {
            DesignSystemShowcaseScreen(onBack = {}, onOpenEditorShowcase = {}, onOpenTemplateShowcase = {}, initialTab = 4)
        }
    }

    @Test
    fun testShowcaseFeedback() {
        paparazzi.genesysSnapshot {
            DesignSystemShowcaseScreen(onBack = {}, onOpenEditorShowcase = {}, onOpenTemplateShowcase = {}, initialTab = 5)
        }
    }

    @Test
    fun testShowcaseBooking() {
        paparazzi.genesysSnapshot {
            DesignSystemShowcaseScreen(onBack = {}, onOpenEditorShowcase = {}, onOpenTemplateShowcase = {}, initialTab = 6)
        }
    }

    @Test
    fun testShowcaseQuality() {
        paparazzi.genesysSnapshot {
            DesignSystemShowcaseScreen(onBack = {}, onOpenEditorShowcase = {}, onOpenTemplateShowcase = {}, initialTab = 7)
        }
    }

    @Test
    fun testTemplateShowcase() {
        paparazzi.genesysSnapshot {
            TemplateShowcaseScreen(onBack = {})
        }
    }
}
