package com.itbenevides.genesys21.ui.util

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import com.itbenevides.genesys21.ui.theme.GenesysGlass
import com.itbenevides.genesys21.ui.theme.GenesysMotion

/**
 * Applies a frosted glass effect with a subtle border.
 */
fun Modifier.glassmorphic(
    shape: Shape,
    alpha: Float = GenesysGlass.defaultAlpha,
    borderAlpha: Float = GenesysGlass.borderAlpha,
): Modifier =
    this.composed {
        this
            .background(Color.White.copy(alpha = alpha), shape)
            .border(
                width = GenesysGlass.borderThickness,
                color = Color.White.copy(alpha = borderAlpha),
                shape = shape,
            )
    }

/**
 * Animated Shimmer effect for skeleton loaders.
 */
@Composable
fun shimmerBrush(
    showShimmer: Boolean = true,
    targetValue: Float = 1000f,
): Brush {
    return if (showShimmer) {
        val shimmerColors =
            listOf(
                Color.LightGray.copy(alpha = 0.6f),
                Color.LightGray.copy(alpha = 0.2f),
                Color.LightGray.copy(alpha = 0.6f),
            )

        val transition = rememberInfiniteTransition(label = "shimmer")
        val translateAnimation by transition.animateFloat(
            initialValue = 0f,
            targetValue = targetValue,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(800, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            label = "shimmerTranslate",
        )

        Brush.linearGradient(
            colors = shimmerColors,
            start = Offset.Zero,
            end = Offset(x = translateAnimation, y = translateAnimation),
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color.Transparent, Color.Transparent),
            start = Offset.Zero,
            end = Offset.Zero,
        )
    }
}

/**
 * Animated pulse effect for reorder handles or active states.
 */
fun Modifier.pulse(
    enabled: Boolean = true,
    minAlpha: Float = 0.3f,
    maxAlpha: Float = 0.8f,
    durationMillis: Int = 1500,
): Modifier =
    this.composed {
        if (!enabled) return@composed Modifier

        val transition = rememberInfiniteTransition(label = "pulse")
        val alpha by transition.animateFloat(
            initialValue = minAlpha,
            targetValue = maxAlpha,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(durationMillis, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "pulseAlpha",
        )

        this.graphicsLayer { this.alpha = alpha }
    }

/**
 * Animated staggered entry for list items.
 */
@Composable
fun rememberStaggeredEntryState(
    index: Int,
    baseDelay: Long = GenesysMotion.staggeredDelay,
    durationMillis: Int = 450,
): State<Float> {
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * baseDelay)
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis, easing = FastOutSlowInEasing),
        )
    }
    return alpha.asState()
}

fun Modifier.staggeredEntry(
    index: Int,
    baseDelay: Long = GenesysMotion.staggeredDelay,
): Modifier =
    this.composed {
        val alphaState = rememberStaggeredEntryState(index, baseDelay)
        this.graphicsLayer {
            this.alpha = alphaState.value
            this.translationY = (1f - alphaState.value) * 60f // Smooth slide up
        }
    }
