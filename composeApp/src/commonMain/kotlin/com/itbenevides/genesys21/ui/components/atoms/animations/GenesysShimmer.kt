package com.itbenevides.genesys21.ui.components.atoms.animations

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.itbenevides.genesys21.ui.theme.GenesysTheme

/**
 * Modificador Elite Shimmer: Aplica um efeito de brilho animado (Skeleton Loading).
 */
fun Modifier.shimmer(
    baseColor: Color? = null,
    highlightColor: Color? = null,
    durationMillis: Int = 1200
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")

    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val shimmerColors = listOf(
        (baseColor ?: GenesysTheme.colors.surfaceVariant.copy(alpha = 0.4f)),
        (highlightColor ?: GenesysTheme.colors.surfaceVariant.copy(alpha = 0.1f)),
        (baseColor ?: GenesysTheme.colors.surfaceVariant.copy(alpha = 0.4f)),
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )

    background(brush)
}
