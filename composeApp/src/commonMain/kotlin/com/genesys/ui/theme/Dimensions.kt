package com.genesys.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Design Tokens - Spacing System
 * Based on 4dp grid system
 * 
 * Usage:
 * ```kotlin
 * Box(modifier = Modifier.padding(Dimensions.spacing_md))
 * ```
 */
object Dimensions {
    // Spacing scale (4dp grid)
    val spacing_xxs: Dp = 2.dp
    val spacing_xs: Dp = 4.dp
    val spacing_sm: Dp = 8.dp
    val spacing_md: Dp = 12.dp
    val spacing_lg: Dp = 16.dp
    val spacing_xl: Dp = 24.dp
    val spacing_xxl: Dp = 32.dp
    val spacing_xxxl: Dp = 48.dp
    val spacing_huge: Dp = 64.dp
    
    // Component sizes
    val button_height: Dp = 48.dp
    val button_height_small: Dp = 36.dp
    val button_height_large: Dp = 56.dp
    
    val icon_size_small: Dp = 16.dp
    val icon_size_medium: Dp = 24.dp
    val icon_size_large: Dp = 32.dp
    val icon_size_xlarge: Dp = 48.dp
    
    val card_elevation: Dp = 2.dp
    val card_elevation_pressed: Dp = 8.dp
    
    // Minimum touch target (accessibility)
    val touch_target_min: Dp = 48.dp
    
    // Corner radius
    val corner_radius_small: Dp = 4.dp
    val corner_radius_medium: Dp = 8.dp
    val corner_radius_large: Dp = 16.dp
    val corner_radius_xlarge: Dp = 24.dp
    val corner_radius_full: Dp = 999.dp // Pill shape
    
    // Divider
    val divider_thickness: Dp = 1.dp
    
    // Card/Container
    val card_padding: Dp = spacing_lg
    val screen_padding: Dp = spacing_lg
    
    // List items
    val list_item_height: Dp = 72.dp
    val list_item_height_compact: Dp = 56.dp
    val list_item_height_expanded: Dp = 88.dp
}