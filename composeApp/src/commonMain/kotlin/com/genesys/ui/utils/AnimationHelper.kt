package com.genesys.ui.utils

import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Animation utilities and presets
 * 
 * Usage:
 * ```kotlin
 * Box(modifier = Modifier.animatePulse())
 * ```
 */
object AnimationHelper {
    
    /**
     * Standard animation duration
     */
    const val DURATION_SHORT = 200
    const val DURATION_MEDIUM = 300
    const val DURATION_LONG = 500
    
    /**
     * Creates a spring animation spec
     */
    fun <T> springSpec(
        dampingRatio: Float = Spring.DampingRatioMediumBouncy,
        stiffness: Float = Spring.StiffnessMedium
    ): SpringSpec<T> {
        return spring(
            dampingRatio = dampingRatio,
            stiffness = stiffness
        )
    }
    
    /**
     * Creates a tween animation spec
     */
    fun <T> tweenSpec(
        durationMillis: Int = DURATION_MEDIUM,
        easing: Easing = FastOutSlowInEasing
    ): TweenSpec<T> {
        return tween(
            durationMillis = durationMillis,
            easing = easing
        )
    }
}

/**
 * Pulse animation modifier
 */
fun Modifier.animatePulse(): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    this.scale(scale)
}

/**
 * Bounce animation modifier
 */
fun Modifier.animateBounce(): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition()
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    this.graphicsLayer {
        translationY = offsetY
    }
}

/**
 * Shake animation modifier
 */
fun Modifier.animateShake(): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition()
    val offsetX by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    this.graphicsLayer {
        translationX = offsetX
    }
}