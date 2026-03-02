package com.genesys.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Genesys Material You Color Palette
 * Following Material Design 3 guidelines
 */
object GenesysColors {
    // Primary colors
    val Primary = Color(0xFF6750A4)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFFEADDFF)
    val OnPrimaryContainer = Color(0xFF21005D)
    
    // Secondary colors
    val Secondary = Color(0xFF625B71)
    val OnSecondary = Color(0xFFFFFFFF)
    val SecondaryContainer = Color(0xFFE8DEF8)
    val OnSecondaryContainer = Color(0xFF1D192B)
    
    // Tertiary colors
    val Tertiary = Color(0xFF7D5260)
    val OnTertiary = Color(0xFFFFFFFF)
    val TertiaryContainer = Color(0xFFFFD8E4)
    val OnTertiaryContainer = Color(0xFF31111D)
    
    // Error colors
    val Error = Color(0xFFB3261E)
    val OnError = Color(0xFFFFFFFF)
    val ErrorContainer = Color(0xFFF9DEDC)
    val OnErrorContainer = Color(0xFF410E0B)
    
    // Background colors
    val Background = Color(0xFFFFFBFE)
    val OnBackground = Color(0xFF1C1B1F)
    
    // Surface colors
    val Surface = Color(0xFFFFFBFE)
    val OnSurface = Color(0xFF1C1B1F)
    val SurfaceVariant = Color(0xFFE7E0EC)
    val OnSurfaceVariant = Color(0xFF49454F)
    
    // Outline
    val Outline = Color(0xFF79747E)
    val OutlineVariant = Color(0xFFCAC4D0)
    
    // Dark theme colors
    val DarkPrimary = Color(0xFFD0BCFF)
    val DarkOnPrimary = Color(0xFF381E72)
    val DarkPrimaryContainer = Color(0xFF4F378B)
    val DarkOnPrimaryContainer = Color(0xFFEADDFF)
    
    val DarkSecondary = Color(0xFFCCC2DC)
    val DarkOnSecondary = Color(0xFF332D41)
    val DarkSecondaryContainer = Color(0xFF4A4458)
    val DarkOnSecondaryContainer = Color(0xFFE8DEF8)
    
    val DarkTertiary = Color(0xFFEFB8C8)
    val DarkOnTertiary = Color(0xFF492532)
    val DarkTertiaryContainer = Color(0xFF633B48)
    val DarkOnTertiaryContainer = Color(0xFFFFD8E4)
    
    val DarkError = Color(0xFFF2B8B5)
    val DarkOnError = Color(0xFF601410)
    val DarkErrorContainer = Color(0xFF8C1D18)
    val DarkOnErrorContainer = Color(0xFFF9DEDC)
    
    val DarkBackground = Color(0xFF1C1B1F)
    val DarkOnBackground = Color(0xFFE6E1E5)
    
    val DarkSurface = Color(0xFF1C1B1F)
    val DarkOnSurface = Color(0xFFE6E1E5)
    val DarkSurfaceVariant = Color(0xFF49454F)
    val DarkOnSurfaceVariant = Color(0xFFCAC4D0)
    
    val DarkOutline = Color(0xFF938F99)
    val DarkOutlineVariant = Color(0xFF49454F)
}

/**
 * Light color scheme for Material3
 */
val LightColorScheme = lightColorScheme(
    primary = GenesysColors.Primary,
    onPrimary = GenesysColors.OnPrimary,
    primaryContainer = GenesysColors.PrimaryContainer,
    onPrimaryContainer = GenesysColors.OnPrimaryContainer,
    secondary = GenesysColors.Secondary,
    onSecondary = GenesysColors.OnSecondary,
    secondaryContainer = GenesysColors.SecondaryContainer,
    onSecondaryContainer = GenesysColors.OnSecondaryContainer,
    tertiary = GenesysColors.Tertiary,
    onTertiary = GenesysColors.OnTertiary,
    tertiaryContainer = GenesysColors.TertiaryContainer,
    onTertiaryContainer = GenesysColors.OnTertiaryContainer,
    error = GenesysColors.Error,
    onError = GenesysColors.OnError,
    errorContainer = GenesysColors.ErrorContainer,
    onErrorContainer = GenesysColors.OnErrorContainer,
    background = GenesysColors.Background,
    onBackground = GenesysColors.OnBackground,
    surface = GenesysColors.Surface,
    onSurface = GenesysColors.OnSurface,
    surfaceVariant = GenesysColors.SurfaceVariant,
    onSurfaceVariant = GenesysColors.OnSurfaceVariant,
    outline = GenesysColors.Outline,
    outlineVariant = GenesysColors.OutlineVariant
)

/**
 * Dark color scheme for Material3
 */
val DarkColorScheme = darkColorScheme(
    primary = GenesysColors.DarkPrimary,
    onPrimary = GenesysColors.DarkOnPrimary,
    primaryContainer = GenesysColors.DarkPrimaryContainer,
    onPrimaryContainer = GenesysColors.DarkOnPrimaryContainer,
    secondary = GenesysColors.DarkSecondary,
    onSecondary = GenesysColors.DarkOnSecondary,
    secondaryContainer = GenesysColors.DarkSecondaryContainer,
    onSecondaryContainer = GenesysColors.DarkOnSecondaryContainer,
    tertiary = GenesysColors.DarkTertiary,
    onTertiary = GenesysColors.DarkOnTertiary,
    tertiaryContainer = GenesysColors.DarkTertiaryContainer,
    onTertiaryContainer = GenesysColors.DarkOnTertiaryContainer,
    error = GenesysColors.DarkError,
    onError = GenesysColors.DarkOnError,
    errorContainer = GenesysColors.DarkErrorContainer,
    onErrorContainer = GenesysColors.DarkOnErrorContainer,
    background = GenesysColors.DarkBackground,
    onBackground = GenesysColors.DarkOnBackground,
    surface = GenesysColors.DarkSurface,
    onSurface = GenesysColors.DarkOnSurface,
    surfaceVariant = GenesysColors.DarkSurfaceVariant,
    onSurfaceVariant = GenesysColors.DarkOnSurfaceVariant,
    outline = GenesysColors.DarkOutline,
    outlineVariant = GenesysColors.DarkOutlineVariant
)