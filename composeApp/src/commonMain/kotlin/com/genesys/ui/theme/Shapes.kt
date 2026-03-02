package com.genesys.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

/**
 * Material Design 3 Shape Scale
 * Defines corner radius for different components
 */
val GenesysShapes = Shapes(
    extraSmall = RoundedCornerShape(Dimensions.corner_radius_small),
    small = RoundedCornerShape(Dimensions.corner_radius_medium),
    medium = RoundedCornerShape(Dimensions.corner_radius_large),
    large = RoundedCornerShape(Dimensions.corner_radius_xlarge),
    extraLarge = RoundedCornerShape(Dimensions.corner_radius_xlarge)
)