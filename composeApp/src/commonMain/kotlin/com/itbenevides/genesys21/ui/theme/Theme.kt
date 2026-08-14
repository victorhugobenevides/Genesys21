package com.itbenevides.genesys21.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.CustomThemeConfig
import com.itbenevides.genesys21.domain.model.PageThemeConfig
import com.itbenevides.genesys21.domain.model.TypographySet
import com.itbenevides.genesys21.util.toColor

@Composable
private fun getContentColor(backgroundColor: Color): Color {
    return if (backgroundColor.luminance() > 0.5f) Color.Black else Color.White
}

@Composable
expect fun getDynamicColorScheme(darkTheme: Boolean): ColorScheme?

private fun ColorScheme.toGenesysColors(isDark: Boolean): GenesysColors {
    return GenesysColors(
        brand = primary,
        onBrand = onPrimary,
        brandContainer = primaryContainer,
        onBrandContainer = onPrimaryContainer,
        accent = tertiary,
        onAccent = onTertiary,
        background = background,
        onBackground = onBackground,
        backgroundSecondary = surfaceVariant.copy(alpha = 0.5f),
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        outline = outline,
        success = Success, // Token fixo para status
        error = error,
        onError = onError,
        errorContainer = errorContainer,
        isDark = isDark
    )
}

// 1. ELEGANCE (Slate & Gold)
private val EleganceColorScheme =
    lightColorScheme(
        primary = ElegancePrimary,
        onPrimary = Color.White,
        primaryContainer = ElegancePrimary.copy(alpha = 0.1f),
        onPrimaryContainer = ElegancePrimary,
        secondary = EleganceGold,
        onSecondary = Color.White,
        secondaryContainer = EleganceGold.copy(alpha = 0.1f),
        onSecondaryContainer = EleganceGold,
        tertiary = Color(0xFF5856D6), // Indigo accent
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFF5856D6).copy(alpha = 0.1f),
        onTertiaryContainer = Color(0xFF5856D6),
        background = EleganceBg,
        onBackground = ElegancePrimary,
        surface = Color.White,
        onSurface = ElegancePrimary,
        surfaceVariant = Color(0xFFF2F2F7),
        onSurfaceVariant = Color(0xFF8E8E93),
        outline = EleganceGold,
        error = Error,
        onError = Color.White,
        errorContainer = Error.copy(alpha = 0.1f),
        onErrorContainer = Error
    )

// 2. VIBRANT (Tech Blue)
private val VibrantColorScheme =
    lightColorScheme(
        primary = VibrantBlue,
        onPrimary = Color.White,
        primaryContainer = VibrantBlue.copy(alpha = 0.1f),
        onPrimaryContainer = VibrantBlue,
        secondary = Color(0xFF34C759), // Green accent
        onSecondary = Color.White,
        secondaryContainer = Color(0xFF34C759).copy(alpha = 0.1f),
        onSecondaryContainer = Color(0xFF34C759),
        tertiary = Color(0xFFFF2D55), // Pink accent
        onTertiary = Color.White,
        background = VibrantBg,
        onBackground = Color(0xFF1C1C1E),
        surface = Color.White,
        onSurface = Color(0xFF1C1C1E),
        surfaceVariant = Color(0xFFE8F1FF),
        onSurfaceVariant = Color(0xFF555E67),
        outline = VibrantBlue,
        error = Error,
        onError = Color.White
    )

// 3. NATURE (Green & Earth)
private val NatureColorScheme =
    lightColorScheme(
        primary = NatureGreen,
        onPrimary = Color.White,
        primaryContainer = NatureGreen.copy(alpha = 0.12f),
        onPrimaryContainer = NatureGreen,
        secondary = Color(0xFFDDA15E), // Earthy tone
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFDDA15E).copy(alpha = 0.12f),
        onSecondaryContainer = Color(0xFFDDA15E),
        tertiary = Color(0xFF606C38),
        onTertiary = Color.White,
        background = NatureBg,
        onBackground = NatureGreen,
        surface = Color.White,
        onSurface = NatureGreen,
        surfaceVariant = Color(0xFFE8EBDD),
        onSurfaceVariant = Color(0xFF4A4E40),
        outline = NatureGreen,
    )

// 4. MONO (Architectural B&W)
private val MonoColorScheme =
    lightColorScheme(
        primary = MonoBlack,
        onPrimary = Color.White,
        primaryContainer = Color(0xFFF2F2F7),
        onPrimaryContainer = MonoBlack,
        secondary = Color(0xFF8E8E93),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFF2F2F7),
        onSecondaryContainer = Color(0xFF3A3A3C),
        tertiary = Color(0xFF000000),
        onTertiary = Color.White,
        background = MonoWhite,
        onBackground = MonoBlack,
        surface = MonoWhite,
        onSurface = MonoBlack,
        surfaceVariant = Color(0xFFF2F2F7),
        onSurfaceVariant = Color(0xFF3A3A3C),
        outline = MonoBlack,
    )

// 5. MIDNIGHT (OLED Dark)
private val MidnightColorScheme =
    darkColorScheme(
        primary = MidnightRed,
        onPrimary = Color.White,
        primaryContainer = MidnightRed.copy(alpha = 0.2f),
        onPrimaryContainer = MidnightRed,
        secondary = Color(0xFF5856D6), // Deep Indigo
        onSecondary = Color.White,
        secondaryContainer = Color(0xFF5856D6).copy(alpha = 0.2f),
        onSecondaryContainer = Color(0xFF5856D6),
        tertiary = Color(0xFF0A84FF), // Tech Blue
        onTertiary = Color.White,
        background = MidnightBlack,
        onBackground = Color.White,
        surface = Color(0xFF1C1C1E),
        onSurface = Color.White,
        surfaceVariant = Color(0xFF2C2C2E),
        onSurfaceVariant = Color(0xFF8E8E93),
        outline = MidnightRed,
    )

// 6. CANDY (Pastel Pop)
private val CandyColorScheme =
    lightColorScheme(
        primary = CandyPink,
        onPrimary = Color.White,
        primaryContainer = CandyPink.copy(alpha = 0.1f),
        onPrimaryContainer = CandyPink,
        secondary = Color(0xFFFF9500), // Orange
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFFF9500).copy(alpha = 0.1f),
        onSecondaryContainer = Color(0xFFFF9500),
        tertiary = Color(0xFF5AC8FA), // Sky Blue
        onTertiary = Color.White,
        background = CandyBg,
        onBackground = Color(0xFF2B0116),
        surface = Color.White,
        onSurface = Color(0xFF2B0116),
        surfaceVariant = Color(0xFFFFD1DC),
        onSurfaceVariant = Color(0xFF7D5260),
        outline = CandyPink,
    )

@Composable
fun AppTheme(
    themeConfig: PageThemeConfig = PageThemeConfig.ELEGANCE,
    customTheme: CustomThemeConfig? = null,
    useDynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val isDark = themeConfig == PageThemeConfig.MIDNIGHT
    val baseColorScheme =
        when (themeConfig) {
            PageThemeConfig.ELEGANCE -> EleganceColorScheme
            PageThemeConfig.VIBRANT -> VibrantColorScheme
            PageThemeConfig.NATURE -> NatureColorScheme
            PageThemeConfig.MONO -> MonoColorScheme
            PageThemeConfig.MIDNIGHT -> MidnightColorScheme
            PageThemeConfig.CANDY -> CandyColorScheme
            else -> EleganceColorScheme
        }

    val dynamicColorScheme = if (useDynamicColor) {
        getDynamicColorScheme(darkTheme = isDark)
    } else null

    val colorScheme =
        if (customTheme != null) {
            val customBg = customTheme.backgroundColor.toColor(baseColorScheme.background)
            val customSurface = customTheme.surfaceColor.toColor(baseColorScheme.surface)
            val customPrimary = customTheme.primaryColor.toColor(baseColorScheme.primary)

            baseColorScheme.copy(
                primary = customPrimary,
                onPrimary = customTheme.onPrimaryColor.toColor(getContentColor(customPrimary)),
                primaryContainer = customPrimary.copy(alpha = 0.1f),
                onPrimaryContainer = customPrimary,
                secondary = customTheme.secondaryColor.toColor(baseColorScheme.secondary),
                onSecondary = customTheme.onPrimaryColor.toColor(getContentColor(customPrimary)),
                background = customBg,
                surface = customSurface,
                onSurface = customTheme.onSurfaceColor.toColor(getContentColor(customSurface)),
                onBackground = customTheme.onSurfaceColor.toColor(getContentColor(customBg)),
                surfaceVariant = customSurface.copy(alpha = 0.7f),
            )
        } else {
            dynamicColorScheme ?: baseColorScheme
        }

    val radius = customTheme?.cornerRadius ?: 16
    val shapes =
        Shapes(
            extraSmall = RoundedCornerShape(radius.dp / 8),
            small = RoundedCornerShape(radius.dp / 4),
            medium = RoundedCornerShape(radius.dp / 2),
            large = RoundedCornerShape(radius.dp),
            extraLarge = RoundedCornerShape(radius.dp * 1.5f),
        )

    val typography = getTypography(customTheme?.typographySet ?: TypographySet.DEFAULT)
    val genesysTypography = getGenesysTypography(customTheme?.typographySet ?: TypographySet.DEFAULT)
    val genesysColors = colorScheme.toGenesysColors(isDark)

    CompositionLocalProvider(
        LocalGenesysColors provides genesysColors,
        LocalGenesysTypography provides genesysTypography,
        LocalGenesysSpacing provides GenesysSpacing(),
        LocalGenesysThemeConfig provides
            GenesysThemeConfig(
                cornerRadius = radius,
                glassIntensity = customTheme?.glassIntensity ?: 0.1f,
            ),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = shapes,
            content = content,
        )
    }
}
