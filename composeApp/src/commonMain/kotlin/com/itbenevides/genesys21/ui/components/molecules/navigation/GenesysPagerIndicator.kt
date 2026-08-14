package com.itbenevides.genesys21.ui.components.molecules.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.itbenevides.genesys21.ui.theme.GenesysMotion
import com.itbenevides.genesys21.ui.theme.GenesysTheme

@Composable
fun GenesysPagerIndicator(
    count: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(count) { iteration ->
            val isSelected = currentPage == iteration

            val color by animateColorAsState(
                targetValue =
                    if (isSelected) {
                        GenesysTheme.colors.brand
                    } else {
                        GenesysTheme.colors.onSurface.copy(alpha = 0.2f)
                    },
                animationSpec = GenesysMotion.colorSpring,
                label = "indicatorColor",
            )

            val size by animateDpAsState(
                targetValue = if (isSelected) GenesysTheme.spacing.s else GenesysTheme.spacing.xxs,
                animationSpec = spring(dampingRatio = 0.8f),
                label = "indicatorSize",
            )

            Box(
                modifier =
                    Modifier
                        .padding(horizontal = GenesysTheme.spacing.xxs)
                        .clip(CircleShape)
                        .background(color)
                        .size(size),
            )
        }
    }
}
