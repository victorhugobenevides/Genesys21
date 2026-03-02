package com.genesys.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * Genesys Material3 Theme
 * Automatically switches between light/dark based on system settings
 * 
 * Features:
 * - Material You color scheme
 * - Automatic dark mode
 * - Custom typography scale
 * - Rounded shapes
 * 
 * Usage:
 * ```kotlin
 * GenesysTheme {
 *     // Your app content
 * }
 * ```
 */
@Composable
fun GenesysTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = GenesysTypography,
        shapes = GenesysShapes,
        content = content
    )
}