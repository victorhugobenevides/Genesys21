package com.genesys.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.genesys.ui.theme.Dimensions

@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    shimmerColors: List<Color> = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    )
) {
    val transition = rememberInfiniteTransition()
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim.value - 1000f, 0f),
        end = Offset(translateAnim.value, 0f)
    )
    
    Box(
        modifier = modifier.background(brush)
    )
}

@Composable
fun ShimmerCardPlaceholder(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Dimensions.spacing_lg)
    ) {
        ShimmerEffect(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(24.dp)
                .clip(RoundedCornerShape(Dimensions.corner_radius_small))
        )
        
        Spacer(modifier = Modifier.height(Dimensions.spacing_sm))
        
        ShimmerEffect(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(16.dp)
                .clip(RoundedCornerShape(Dimensions.corner_radius_small))
        )
        
        Spacer(modifier = Modifier.height(Dimensions.spacing_lg))
        
        repeat(3) {
            ShimmerEffect(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(Dimensions.corner_radius_small))
            )
            Spacer(modifier = Modifier.height(Dimensions.spacing_sm))
        }
    }
}

@Composable
fun ShimmerListItemPlaceholder(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimensions.list_item_height)
            .padding(Dimensions.spacing_lg)
    ) {
        ShimmerEffect(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(Dimensions.corner_radius_full))
        )
        
        Spacer(modifier = Modifier.width(Dimensions.spacing_lg))
        
        Column(modifier = Modifier.weight(1f)) {
            ShimmerEffect(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(Dimensions.corner_radius_small))
            )
            
            Spacer(modifier = Modifier.height(Dimensions.spacing_xs))
            
            ShimmerEffect(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(Dimensions.corner_radius_small))
            )
        }
    }
}