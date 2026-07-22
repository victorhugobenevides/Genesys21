package com.itbenevides.genesys21.ui.util

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Renders a shifting radial gradient background based on theme colors.
 */
@Composable
fun AnimatedGradientBackground(
    modifier: Modifier = Modifier,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.secondaryContainer,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "background")

    val phaseState = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec =
            infiniteRepeatable(
                animation = tween(10000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "phase",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val phase = phaseState.value

        // Calculate dynamic centers for two overlapping gradients
        val centerX1 = width / 2f + (width / 4f) * cos(phase)
        val centerY1 = height / 2f + (height / 4f) * sin(phase)

        val centerX2 = width / 2f + (width / 4f) * sin(phase + PI.toFloat())
        val centerY2 = height / 2f + (height / 4f) * cos(phase + PI.toFloat())

        // Draw primary blob
        drawRect(
            brush =
                Brush.radialGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(centerX1, centerY1),
                    radius = width.coerceAtLeast(height) * 0.8f,
                ),
        )

        // Draw secondary blob
        drawRect(
            brush =
                Brush.radialGradient(
                    colors = listOf(secondaryColor.copy(alpha = 0.2f), Color.Transparent),
                    center = Offset(centerX2, centerY2),
                    radius = width.coerceAtLeast(height) * 0.7f,
                ),
        )
    }
}
