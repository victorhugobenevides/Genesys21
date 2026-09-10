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
        genesysResponsiveSnapshot(paparazzi) {
            DesignSystemShowcaseScreen(onBack = {}, onOpenEditorShowcase = {}, onOpenTemplateShowcase = {}, initialTab = 0)
        }
    }

    @Test
    fun testShowcaseFoundationResponsive() {
        genesysResponsiveSnapshot(paparazzi) {
            DesignSystemShowcaseScreen(onBack = {}, onOpenEditorShowcase = {}, onOpenTemplateShowcase = {}, initialTab = 1)
        }
    }

    @Test
    fun testShowcaseTemplatesResponsive() {
        genesysResponsiveSnapshot(paparazzi) {
            DesignSystemShowcaseScreen(onBack = {}, onOpenEditorShowcase = {}, onOpenTemplateShowcase = {}, initialTab = 2)
        }
    }

    @Test
    fun testShowcaseInputsResponsive() {
        genesysResponsiveSnapshot(paparazzi) {
            DesignSystemShowcaseScreen(onBack = {}, onOpenEditorShowcase = {}, onOpenTemplateShowcase = {}, initialTab = 3)
        }
    }

    @Test
    fun testShowcaseActionNavResponsive() {
        genesysResponsiveSnapshot(paparazzi) {
            DesignSystemShowcaseScreen(onBack = {}, onOpenEditorShowcase = {}, onOpenTemplateShowcase = {}, initialTab = 4)
        }
    }

    @Test
    fun testShowcaseDisplayResponsive() {
        genesysResponsiveSnapshot(paparazzi) {
            DesignSystemShowcaseScreen(onBack = {}, onOpenEditorShowcase = {}, onOpenTemplateShowcase = {}, initialTab = 5)
        }
    }

    @Test
    fun testShowcaseFeedbackResponsive() {
        genesysResponsiveSnapshot(paparazzi) {
            DesignSystemShowcaseScreen(onBack = {}, onOpenEditorShowcase = {}, onOpenTemplateShowcase = {}, initialTab = 6)
        }
    }

    @Test
    fun testShowcaseBookingResponsive() {
        genesysResponsiveSnapshot(paparazzi) {
            DesignSystemShowcaseScreen(onBack = {}, onOpenEditorShowcase = {}, onOpenTemplateShowcase = {}, initialTab = 7)
        }
    }

    @Test
    fun testShowcasePaymentsResponsive() {
        genesysResponsiveSnapshot(paparazzi) {
            DesignSystemShowcaseScreen(onBack = {}, onOpenEditorShowcase = {}, onOpenTemplateShowcase = {}, initialTab = 8)
        }
    }

    @Test
    fun testShowcaseQualityResponsive() {
        genesysResponsiveSnapshot(paparazzi) {
            DesignSystemShowcaseScreen(onBack = {}, onOpenEditorShowcase = {}, onOpenTemplateShowcase = {}, initialTab = 9)
        }
    }

    @Test
    fun testShowcaseToolsResponsive() {
        genesysResponsiveSnapshot(paparazzi) {
            DesignSystemShowcaseScreen(onBack = {}, onOpenEditorShowcase = {}, onOpenTemplateShowcase = {}, initialTab = 10)
        }
    }

    @Test
    fun testTemplateShowcaseResponsive() {
        genesysResponsiveSnapshot(paparazzi) {
            TemplateShowcaseScreen(onBack = {})
        }
    }
}
