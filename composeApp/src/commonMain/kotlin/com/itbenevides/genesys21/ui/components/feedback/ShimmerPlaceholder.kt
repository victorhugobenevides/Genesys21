package com.itbenevides.genesys21.ui.components.feedback

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Efeito shimmer para placeholders de carregamento.
 * Cria uma animação de gradiente horizontal que se move continuamente,
 * indicando que o conteúdo está sendo carregado.
 */
@Composable
fun ShimmerPlaceholder(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
    shimmerColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val shimmerColors = listOf(
        shimmerColor.copy(alpha = 0.3f),
        shimmerColor.copy(alpha = 0.5f),
        shimmerColor.copy(alpha = 0.3f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
    )
}

/**
 * Placeholder shimmer circular (para avatares).
 */
@Composable
fun ShimmerCirclePlaceholder(
    size: Dp = 64.dp,
    shimmerColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    ShimmerPlaceholder(
        modifier = Modifier.size(size),
        shape = CircleShape,
        shimmerColor = shimmerColor
    )
}

/**
 * Placeholder shimmer retangular (para cards e imagens).
 */
@Composable
fun ShimmerRectPlaceholder(
    width: Dp = Dp.Unspecified,
    height: Dp = 120.dp,
    shimmerColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    ShimmerPlaceholder(
        modifier = Modifier
            .then(if (width != Dp.Unspecified) Modifier.width(width) else Modifier.fillMaxWidth())
            .height(height),
        shape = RoundedCornerShape(8.dp),
        shimmerColor = shimmerColor
    )
}

/**
 * Linha de texto shimmer (para títulos e descrições).
 */
@Composable
fun ShimmerTextPlaceholder(
    width: Float = 0.7f,
    height: Dp = 16.dp,
    shimmerColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    ShimmerPlaceholder(
        modifier = Modifier
            .fillMaxWidth(width)
            .height(height),
        shape = RoundedCornerShape(4.dp),
        shimmerColor = shimmerColor
    )
}

/**
 * Lista de linhas shimmer (para listas de carregamento).
 */
@Composable
fun ShimmerListPlaceholder(
    itemCount: Int = 3,
    shimmerColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        repeat(itemCount) { index ->
            ShimmerTextPlaceholder(
                width = 0.9f - (index * 0.1f),
                shimmerColor = shimmerColor
            )
        }
    }
}