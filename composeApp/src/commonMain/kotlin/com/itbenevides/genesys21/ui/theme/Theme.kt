package com.itbenevides.genesys21.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.itbenevides.genesys21.domain.model.CustomThemeConfig
import com.itbenevides.genesys21.domain.model.PageThemeConfig
import com.itbenevides.genesys21.domain.model.TypographySet

// Helper para converter Hex para Color
private fun String?.toColor(fallback: Color): Color {
    if (this == null) return fallback
    return try {
        val hex = this.removePrefix("#")
        val longColor = hex.toLong(16)
        if (hex.length == 6) {
            Color(longColor or 0xFF000000)
        } else {
            Color(longColor)
        }
    } catch (e: Exception) {
        fallback
    }
}

// 1. ROYAL (Navy & Gold)
private val RoyalColorScheme =
    lightColorScheme(
        primary = Color(0xFF14213D),
        onPrimary = Color(0xFFFCA311),
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFF8F9FA),
        onSurface = Color(0xFF14213D),
        surfaceVariant = Color(0xFFE5E5E5),
        outline = Color(0xFFFCA311),
    )

// 2. OCEAN (Teal & Mint)
private val OceanColorScheme =
    lightColorScheme(
        primary = Color(0xFF00ADB5),
        onPrimary = Color.White,
        background = Color(0xFFEEEEEE),
        surface = Color.White,
        onSurface = Color(0xFF222831),
        outline = Color(0xFF00ADB5),
    )

// 3. FOREST (Deep Green & Earth)
private val ForestColorScheme =
    lightColorScheme(
        primary = Color(0xFF283618),
        onPrimary = Color(0xFFFEFAE0),
        background = Color(0xFFFEFAE0),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF283618),
        surfaceVariant = Color(0xFFDDA15E),
    )

// 4. CANDY (Pink & Yellow)
private val CandyColorScheme =
    lightColorScheme(
        primary = Color(0xFFD81159),
        onPrimary = Color.White,
        background = Color(0xFFFFF9FA),
        surface = Color.White,
        onSurface = Color(0xFF210124),
        surfaceVariant = Color(0xFFFFBC42),
    )

// 5. SUNSET (Orange & Teal)
private val SunsetColorScheme =
    lightColorScheme(
        primary = Color(0xFFE76F51),
        onPrimary = Color.White,
        background = Color(0xFFFAF9F6),
        onSurface = Color(0xFF264653),
        surfaceVariant = Color(0xFFF4A261),
    )

// 6. BERRY (Purple & Wine)
private val BerryColorScheme =
    lightColorScheme(
        primary = Color(0xFF6A0572),
        onPrimary = Color.White,
        background = Color(0xFFF8F4F9),
        onSurface = Color(0xFF350139),
        surfaceVariant = Color(0xFFAB83A1),
    )

// 7. MINIMAL (Pure B&W)
private val MinimalColorScheme =
    lightColorScheme(
        primary = Color(0xFF000000),
        onPrimary = Color.White,
        background = Color(0xFFFFFFFF),
        onSurface = Color(0xFF000000),
        surfaceVariant = Color(0xFFE0E0E0),
    )

// 8. VINTAGE (Sepia & Coffee)
private val VintageColorScheme =
    lightColorScheme(
        primary = Color(0xFF8B5E3C),
        onPrimary = Color(0xFFF5F1ED),
        background = Color(0xFFF5F1ED),
        onSurface = Color(0xFF432818),
        surfaceVariant = Color(0xFFBC8A5F),
    )

// 9. NORDIC (Ice Blue & Slate)
private val NordicColorScheme =
    lightColorScheme(
        primary = Color(0xFF4A90E2),
        onPrimary = Color.White,
        background = Color(0xFFF0F4F8),
        onSurface = Color(0xFF243B53),
        surfaceVariant = Color(0xFFD9E2EC),
    )

// 10. COFFEE (Latte & Mocha)
private val CoffeeColorScheme =
    lightColorScheme(
        primary = Color(0xFF6F4E37),
        onPrimary = Color.White,
        background = Color(0xFFECB390),
        onSurface = Color(0xFF3C2A21),
        surfaceVariant = Color(0xFFDFBB9D),
    )

// 11. SOFT LAVENDER
private val SoftLavenderColorScheme =
    lightColorScheme(
        primary = Color(0xFF967BB6),
        onPrimary = Color.White,
        background = Color(0xFFF3E5F5),
        onSurface = Color(0xFF4A148C),
    )

// 12. SKY BLUE
private val SkyBlueColorScheme =
    lightColorScheme(
        primary = Color(0xFF039BE5),
        onPrimary = Color.White,
        background = Color(0xFFE1F5FE),
        onSurface = Color(0xFF01579B),
    )

// 13. MINT GREEN
private val MintGreenColorScheme =
    lightColorScheme(
        primary = Color(0xFF00C853),
        onPrimary = Color.White,
        background = Color(0xFFE8F5E9),
        onSurface = Color(0xFF1B5E20),
    )

// 14. PEACH
private val PeachColorScheme =
    lightColorScheme(
        primary = Color(0xFFFF8A65),
        onPrimary = Color.White,
        background = Color(0xFFFFF3E0),
        onSurface = Color(0xFFBF360C),
    )

// 15. LEMON
private val LemonColorScheme =
    lightColorScheme(
        primary = Color(0xFFFBC02D),
        onPrimary = Color.Black,
        background = Color(0xFFFFFDE7),
        onSurface = Color(0xFFF57F17),
    )

// 21. RADARANI (Baseado no Azul Vibrante da Imagem)
private val RadaraniColorScheme =
    lightColorScheme(
        primary = Color(0xFF2CB1FF),
        onPrimary = Color.White,
        background = Color(0xFFF0F9FF),
        surface = Color.White,
        onSurface = Color(0xFF003366),
        surfaceVariant = Color(0xFFCCEEFF),
        outline = Color(0xFF2CB1FF),
    )

// DARK THEMES

// 16. DARK MODE (Classic)
private val DarkModeColorScheme =
    darkColorScheme(
        primary = Color(0xFFBB86FC),
        onPrimary = Color.Black,
        background = Color(0xFF121212),
        surface = Color(0xFF1E1E1E),
        onSurface = Color.White,
    )

// 17. MIDNIGHT (Blue Dark)
private val MidnightColorScheme =
    darkColorScheme(
        primary = Color(0xFFE94560),
        onPrimary = Color.White,
        background = Color(0xFF1A1A2E),
        surface = Color(0xFF16213E),
        onSurface = Color(0xFFE94560),
    )

// 18. NEON (Cyberpunk)
private val NeonColorScheme =
    darkColorScheme(
        primary = Color(0xFF39FF14),
        onPrimary = Color.Black,
        background = Color(0xFF000000),
        surface = Color(0xFF121212),
        onSurface = Color(0xFF39FF14),
        outline = Color(0xFFBC13FE),
    )

// 19. DEEP SPACE
private val DeepSpaceColorScheme =
    darkColorScheme(
        primary = Color(0xFF00D1FF),
        onPrimary = Color.Black,
        background = Color(0xFF0B0E14),
        surface = Color(0xFF151921),
        onSurface = Color(0xFF00D1FF),
    )

// 20. LUXURY GOLD
private val LuxuryGoldColorScheme =
    darkColorScheme(
        primary = Color(0xFFD4AF37),
        onPrimary = Color.Black,
        background = Color(0xFF1A1A1A),
        surface = Color(0xFF2D2D2D),
        onSurface = Color(0xFFD4AF37),
    )

@Composable
fun AppTheme(
    themeConfig: PageThemeConfig = PageThemeConfig.ROYAL,
    customTheme: CustomThemeConfig? = null,
    content: @Composable () -> Unit,
) {
    val baseColorScheme =
        when (themeConfig) {
            PageThemeConfig.ROYAL -> RoyalColorScheme
            PageThemeConfig.OCEAN -> OceanColorScheme
            PageThemeConfig.FOREST -> ForestColorScheme
            PageThemeConfig.CANDY -> CandyColorScheme
            PageThemeConfig.SUNSET -> SunsetColorScheme
            PageThemeConfig.BERRY -> BerryColorScheme
            PageThemeConfig.MINIMAL -> MinimalColorScheme
            PageThemeConfig.VINTAGE -> VintageColorScheme
            PageThemeConfig.NORDIC -> NordicColorScheme
            PageThemeConfig.COFFEE -> CoffeeColorScheme
            PageThemeConfig.SOFT_LAVENDER -> SoftLavenderColorScheme
            PageThemeConfig.SKY_BLUE -> SkyBlueColorScheme
            PageThemeConfig.MINT_GREEN -> MintGreenColorScheme
            PageThemeConfig.PEACH -> PeachColorScheme
            PageThemeConfig.LEMON -> LemonColorScheme
            PageThemeConfig.DARK_MODE -> DarkModeColorScheme
            PageThemeConfig.MIDNIGHT -> MidnightColorScheme
            PageThemeConfig.NEON -> NeonColorScheme
            PageThemeConfig.DEEP_SPACE -> DeepSpaceColorScheme
            PageThemeConfig.LUXURY_GOLD -> LuxuryGoldColorScheme
            PageThemeConfig.RADARANI -> RadaraniColorScheme
            PageThemeConfig.CLEAN -> MinimalColorScheme
            PageThemeConfig.MODERN -> OceanColorScheme
            PageThemeConfig.DEFAULT -> RoyalColorScheme
        }

    val colorScheme =
        if (customTheme != null) {
            baseColorScheme.copy(
                primary = customTheme.primaryColor.toColor(baseColorScheme.primary),
                onPrimary = customTheme.onPrimaryColor.toColor(baseColorScheme.onPrimary),
                secondary = customTheme.secondaryColor.toColor(baseColorScheme.secondary),
                background = customTheme.backgroundColor.toColor(baseColorScheme.background),
                surface = customTheme.surfaceColor.toColor(baseColorScheme.surface),
                onSurface = customTheme.onSurfaceColor.toColor(baseColorScheme.onSurface),
            )
        } else {
            baseColorScheme
        }

    CompositionLocalProvider(
        LocalGenesysThemeConfig provides
            GenesysThemeConfig(
                cornerRadius = customTheme?.cornerRadius ?: 16,
                glassIntensity = customTheme?.glassIntensity ?: 0.1f,
            ),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = getTypography(customTheme?.typographySet ?: TypographySet.DEFAULT),
            content = content,
        )
    }
}
