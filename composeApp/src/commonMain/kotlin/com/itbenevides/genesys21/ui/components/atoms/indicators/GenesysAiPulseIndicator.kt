package com.itbenevides.genesys21.ui.components.atoms.indicators

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.ui.theme.GenesysTheme

/**
 * GenesysAiPulseIndicator: A modern, pulsating dot indicator for AI tasks.
 * Uses rememberInfiniteTransition for a smooth, continuous effect.
 */
@Composable
fun GenesysAiPulseIndicator(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "AiPulse")

    val dotCount = 3
    val dots = List(dotCount) { index ->
        transition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
                initialStartOffset = StartOffset(index * 150)
            ),
            label = "DotScale_$index"
        )
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(GenesysTheme.spacing.xxs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        dots.forEach { scale ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .scale(scale.value)
                    .background(GenesysTheme.colors.accent, CircleShape)
            )
        }
    }
}
