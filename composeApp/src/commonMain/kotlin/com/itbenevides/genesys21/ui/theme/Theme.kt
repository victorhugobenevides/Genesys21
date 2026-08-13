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

// 1. ELEGANCE (Slate & Gold)
private val EleganceColorScheme =
    lightColorScheme(
        primary = ElegancePrimary,
        onPrimary = EleganceGold,
        background = EleganceBg,
        surface = Color.White,
        onSurface = ElegancePrimary,
        surfaceVariant = Color(0xFFE5E5EA),
        outline = EleganceGold,
    )

// 2. VIBRANT (Tech Blue)
private val VibrantColorScheme =
    lightColorScheme(
        primary = VibrantBlue,
        onPrimary = Color.White,
        background = VibrantBg,
        surface = Color.White,
        onSurface = Color(0xFF1C1C1E),
        outline = VibrantBlue,
    )

// 3. NATURE (Green & Earth)
private val NatureColorScheme =
    lightColorScheme(
        primary = NatureGreen,
        onPrimary = Color.White,
        background = NatureBg,
        surface = Color.White,
        onSurface = NatureGreen,
        surfaceVariant = Color(0xFFE8EBDD),
    )

// 4. MONO (Architectural B&W)
private val MonoColorScheme =
    lightColorScheme(
        primary = MonoBlack,
        onPrimary = Color.White,
        background = MonoWhite,
        surface = MonoWhite,
        onSurface = MonoBlack,
        surfaceVariant = Color(0xFFF2F2F7),
    )

// 5. MIDNIGHT (OLED Dark)
private val MidnightColorScheme =
    darkColorScheme(
        primary = MidnightRed,
        onPrimary = Color.White,
        background = MidnightBlack,
        surface = Color(0xFF1C1C1E),
        onSurface = Color.White,
        surfaceVariant = Color(0xFF2C2C2E),
    )

// 6. CANDY (Pastel Pop)
private val CandyColorScheme =
    lightColorScheme(
        primary = CandyPink,
        onPrimary = Color.White,
        background = CandyBg,
        surface = Color.White,
        onSurface = Color(0xFF2B0116),
        surfaceVariant = Color(0xFFFFD1DC),
    )

@Composable
fun AppTheme(
    themeConfig: PageThemeConfig = PageThemeConfig.ELEGANCE,
    customTheme: CustomThemeConfig? = null,
    content: @Composable () -> Unit,
) {
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

    val colorScheme =
        if (customTheme != null) {
            val customBg = customTheme.backgroundColor.toColor(baseColorScheme.background)
            val customSurface = customTheme.surfaceColor.toColor(baseColorScheme.surface)
            val customPrimary = customTheme.primaryColor.toColor(baseColorScheme.primary)

            baseColorScheme.copy(
                primary = customPrimary,
                onPrimary = customTheme.onPrimaryColor.toColor(getContentColor(customPrimary)),
                secondary = customTheme.secondaryColor.toColor(baseColorScheme.secondary),
                onSecondary = customTheme.onPrimaryColor.toColor(getContentColor(customPrimary)),
                background = customBg,
                surface = customSurface,
                onSurface = customTheme.onSurfaceColor.toColor(getContentColor(customSurface)),
                onBackground = customTheme.onSurfaceColor.toColor(getContentColor(customBg)),
                surfaceVariant = customSurface.copy(alpha = 0.7f),
            )
        } else {
            baseColorScheme
        }

    val radius = customTheme?.cornerRadius ?: 16
    val shapes =
        Shapes(
            small = RoundedCornerShape(radius.dp / 4),
            medium = RoundedCornerShape(radius.dp / 2),
            large = RoundedCornerShape(radius.dp),
            extraLarge = RoundedCornerShape(radius.dp * 1.5f),
        )

    CompositionLocalProvider(
        LocalGenesysThemeConfig provides
            GenesysThemeConfig(
                cornerRadius = radius,
                glassIntensity = customTheme?.glassIntensity ?: 0.1f,
            ),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = getTypography(customTheme?.typographySet ?: TypographySet.DEFAULT),
            shapes = shapes,
            content = content,
        )
    }
}
