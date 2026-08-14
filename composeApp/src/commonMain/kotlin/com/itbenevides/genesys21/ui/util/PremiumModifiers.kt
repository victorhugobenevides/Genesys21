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
import com.itbenevides.genesys21.ui.theme.GenesysTheme
import com.itbenevides.genesys21.ui.theme.LocalGenesysThemeConfig

/**
 * Applies a frosted glass effect with a subtle border.
 */
fun Modifier.glassmorphic(
    shape: Shape,
    alpha: Float? = null,
    borderAlpha: Float = GenesysGlass.borderAlpha,
): Modifier =
    this.composed {
        val config = LocalGenesysThemeConfig.current
        val finalAlpha = alpha ?: config.glassIntensity

        this
            .background(GenesysTheme.colors.surface.copy(alpha = finalAlpha), shape)
            .border(
                width = GenesysGlass.borderThickness,
                color = GenesysTheme.colors.onSurface.copy(alpha = borderAlpha),
                shape = shape,
            )
    }

/**
 * Animated Shimmer effect for skeleton loaders.
 * Refined with M3 Standard easing for a more natural flow.
 */
@Composable
fun shimmerBrush(
    showShimmer: Boolean = true,
    targetValue: Float = 1000f,
): Brush {
    return if (showShimmer) {
        val shimmerColors =
            listOf(
                GenesysTheme.colors.surfaceVariant.copy(alpha = 0.6f),
                GenesysTheme.colors.surfaceVariant.copy(alpha = 0.2f),
                GenesysTheme.colors.surfaceVariant.copy(alpha = 0.6f),
            )

        val transition = rememberInfiniteTransition(label = "shimmer")
        val translateAnimation by transition.animateFloat(
            initialValue = 0f,
            targetValue = targetValue,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(1200, easing = GenesysMotion.Standard),
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
                    animation = tween(durationMillis, easing = GenesysMotion.Standard),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "pulseAlpha",
        )

        this.graphicsLayer { this.alpha = alpha }
    }

/**
 * Animated staggered entry for list items.
 * Uses M3 Emphasized curve for a high-quality entrance feel.
 */
@Composable
fun rememberStaggeredEntryState(
    index: Int,
    baseDelay: Long = GenesysMotion.staggeredDelay,
    durationMillis: Int = GenesysMotion.DurationLong1,
): State<Float> {
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * baseDelay)
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis, easing = GenesysMotion.EmphasizedDecelerate),
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
            this.translationY = (1f - alphaState.value) * 40f // Smooth slide up
        }
    }
