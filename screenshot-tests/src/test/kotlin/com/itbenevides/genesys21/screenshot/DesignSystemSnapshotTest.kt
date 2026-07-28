package com.itbenevides.genesys21.screenshot

import com.itbenevides.genesys21.presentation.screens.editor.DesignSystemShowcaseScreen
import com.itbenevides.genesys21.presentation.screens.editor.TemplateShowcaseScreen
import com.itbenevides.genesys21.screenshot.util.createGenesysPaparazzi
import com.itbenevides.genesys21.screenshot.util.genesysResponsiveSnapshot
import org.junit.Rule
import org.junit.Test

class DesignSystemSnapshotTest {
    @get:Rule
    val paparazzi = createGenesysPaparazzi()

    @Test
    fun testShowcaseArchitectureResponsive() {
        paparazzi.genesysResponsiveSnapshot {
            DesignSystemShowcaseScreen(onBack = {}, onOpenEditorShowcase = {}, onOpenTemplateShowcase = {}, initialTab = 0)
        }
    }

    @Test
    fun testShowcaseFoundationResponsive() {
        paparazzi.genesysResponsiveSnapshot {
            DesignSystemShowcaseScreen(onBack = {}, onOpenEditorShowcase = {}, onOpenTemplateShowcase = {}, initialTab = 1)
        }
    }

    @Test
    fun testShowcaseTemplatesResponsive() {
        paparazzi.genesysResponsiveSnapshot {
            DesignSystemShowcaseScreen(onBack = {}, onOpenEditorShowcase = {}, onOpenTemplateShowcase = {}, initialTab = 2)
        }
    }

    @Test
    fun testShowcaseInputsResponsive() {
        paparazzi.genesysResponsiveSnapshot {
            DesignSystemShowcaseScreen(onBack = {}, onOpenEditorShowcase = {}, onOpenTemplateShowcase = {}, initialTab = 3)
        }
    }

    @Test
    fun testShowcaseActionNavResponsive() {
        paparazzi.genesysResponsiveSnapshot {
            DesignSystemShowcaseScreen(onBack = {}, onOpenEditorShowcase = {}, onOpenTemplateShowcase = {}, initialTab = 4)
        }
    }

    @Test
    fun testShowcaseDisplayResponsive() {
        paparazzi.genesysResponsiveSnapshot {
            DesignSystemShowcaseScreen(onBack = {}, onOpenEditorShowcase = {}, onOpenTemplateShowcase = {}, initialTab = 5)
        }
    }

    @Test
    fun testShowcaseFeedbackResponsive() {
        paparazzi.genesysResponsiveSnapshot {
            DesignSystemShowcaseScreen(onBack = {}, onOpenEditorShowcase = {}, onOpenTemplateShowcase = {}, initialTab = 6)
        }
    }

    @Test
    fun testShowcaseBookingResponsive() {
        paparazzi.genesysResponsiveSnapshot {
            DesignSystemShowcaseScreen(onBack = {}, onOpenEditorShowcase = {}, onOpenTemplateShowcase = {}, initialTab = 7)
        }
    }

    @Test
    fun testShowcaseQualityResponsive() {
        paparazzi.genesysResponsiveSnapshot {
            DesignSystemShowcaseScreen(onBack = {}, onOpenEditorShowcase = {}, onOpenTemplateShowcase = {}, initialTab = 8)
        }
    }

    @Test
    fun testTemplateShowcaseResponsive() {
        paparazzi.genesysResponsiveSnapshot {
            TemplateShowcaseScreen(onBack = {})
        }
    }
}
