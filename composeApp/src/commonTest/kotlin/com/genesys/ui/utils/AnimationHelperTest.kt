package com.genesys.ui.utils

import androidx.compose.animation.core.Spring
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnimationHelperTest {

    @Test
    fun testDurationConstants() {
        assertEquals(200, AnimationHelper.DURATION_SHORT)
        assertEquals(300, AnimationHelper.DURATION_MEDIUM)
        assertEquals(500, AnimationHelper.DURATION_LONG)
    }

    @Test
    fun testSpringSpec_defaultValues() {
        val spec = AnimationHelper.springSpec<Float>()
        // Just verify it creates without error
        assertTrue(spec.dampingRatio >= 0f)
        assertTrue(spec.stiffness > 0f)
    }

    @Test
    fun testSpringSpec_customValues() {
        val spec = AnimationHelper.springSpec<Float>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        )
        assertEquals(Spring.DampingRatioNoBouncy, spec.dampingRatio)
        assertEquals(Spring.StiffnessHigh, spec.stiffness)
    }

    @Test
    fun testTweenSpec_defaultDuration() {
        val spec = AnimationHelper.tweenSpec<Float>()
        assertEquals(AnimationHelper.DURATION_MEDIUM, spec.durationMillis)
    }

    @Test
    fun testTweenSpec_customDuration() {
        val spec = AnimationHelper.tweenSpec<Float>(durationMillis = 1000)
        assertEquals(1000, spec.durationMillis)
    }
}