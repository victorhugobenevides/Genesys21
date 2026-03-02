package com.genesys.ui.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccessibilityHelperTest {

    @Test
    fun testCalculateContrastRatio_blackOnWhite() {
        val black = 0xFF000000.toInt()
        val white = 0xFFFFFFFF.toInt()
        val ratio = AccessibilityHelper.calculateContrastRatio(black, white)
        assertTrue(ratio > 20.0, "Black on white should have high contrast (got $ratio)")
    }

    @Test
    fun testCalculateContrastRatio_whiteOnBlack() {
        val black = 0xFF000000.toInt()
        val white = 0xFFFFFFFF.toInt()
        val ratio = AccessibilityHelper.calculateContrastRatio(white, black)
        assertTrue(ratio > 20.0, "White on black should have high contrast (got $ratio)")
    }

    @Test
    fun testCalculateContrastRatio_sameColor() {
        val color = 0xFF808080.toInt()
        val ratio = AccessibilityHelper.calculateContrastRatio(color, color)
        assertEquals(1.0, ratio, 0.1, "Same color should have ratio of 1")
    }

    @Test
    fun testCheckColorContrast_highContrast_returnsTrue() {
        val black = 0xFF000000.toInt()
        val white = 0xFFFFFFFF.toInt()
        assertTrue(AccessibilityHelper.checkColorContrast(black, white))
    }

    @Test
    fun testCheckColorContrast_lowContrast_returnsFalse() {
        val lightGray = 0xFFCCCCCC.toInt()
        val white = 0xFFFFFFFF.toInt()
        val hasGoodContrast = AccessibilityHelper.checkColorContrast(lightGray, white)
        // Light gray on white typically fails WCAG AA (4.5:1)
        assertTrue(!hasGoodContrast, "Light gray on white should fail contrast check")
    }

    @Test
    fun testMinimumTouchTarget_isAtLeast48dp() {
        val minTarget = AccessibilityHelper.MINIMUM_TOUCH_TARGET
        assertTrue(minTarget.value >= 48f, "Minimum touch target should be at least 48dp")
    }
}