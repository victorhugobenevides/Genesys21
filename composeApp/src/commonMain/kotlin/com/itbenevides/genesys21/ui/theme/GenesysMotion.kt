package com.itbenevides.genesys21.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.graphics.Color

object GenesysMotion {
    // Standard spring for interactive elements (Scaling, Color morphing)
    val interactiveSpring =
        spring<Float>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        )

    val colorSpring =
        spring<Color>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium,
        )

    const val staggeredDelay = 50L // ms per item
}
