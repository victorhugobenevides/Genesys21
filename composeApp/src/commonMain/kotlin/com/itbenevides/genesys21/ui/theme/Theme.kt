package com.itbenevides.genesys21.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.itbenevides.genesys21.domain.model.PageThemeConfig

// 1. ROYAL (Elegante e Profissional)
private val RoyalColorScheme = lightColorScheme(
    primary = Color(0xFF2D3142),
    onPrimary = Color.White,
    background = Color(0xFFF8F9FA),
    surface = Color.White,
    onSurface = Color(0xFF2D3142),
    surfaceVariant = Color(0xFFEFEEF5),
    onSurfaceVariant = Color(0xFF4F5D75),
    outline = Color(0xFFBFC0C0),
    error = Color(0xFFD64933)
)

// 2. SOFT OCEAN (Calmo e Sereno)
private val SoftOceanColorScheme = lightColorScheme(
    primary = Color(0xFF4A90E2),
    onPrimary = Color.White,
    background = Color(0xFFF0F4F8),
    surface = Color.White,
    onSurface = Color(0xFF102A43),
    surfaceVariant = Color(0xFFD9E2EC),
    onSurfaceVariant = Color(0xFF486581),
    outline = Color(0xFFBCCCDC),
    error = Color(0xFFEF4E4E)
)

// 3. ECO FOREST (Natural e Orgânico)
private val EcoForestColorScheme = lightColorScheme(
    primary = Color(0xFF386641),
    onPrimary = Color.White,
    background = Color(0xFFF2F4F3),
    surface = Color.White,
    onSurface = Color(0xFF1B4332),
    surfaceVariant = Color(0xFFE0E7E1),
    onSurfaceVariant = Color(0xFF6A994E),
    outline = Color(0xFFA7C957),
    error = Color(0xFFBC4749)
)

// 4. ROSE GOLD (Premium e Moderno)
private val RoseGoldColorScheme = lightColorScheme(
    primary = Color(0xFFB08968),
    onPrimary = Color.White,
    background = Color(0xFFFFF9F5),
    surface = Color.White,
    onSurface = Color(0xFF7F5539),
    surfaceVariant = Color(0xFFEDE0D4),
    onSurfaceVariant = Color(0xFF9C6644),
    outline = Color(0xFFDDB892),
    error = Color(0xFFE63946)
)

// 5. MIDNIGHT (Focado e Minimalista)
private val MidnightColorScheme = darkColorScheme(
    primary = Color(0xFFE0E1DD),
    onPrimary = Color(0xFF0D1B2A),
    background = Color(0xFF0D1B2A),
    surface = Color(0xFF1B263B),
    onSurface = Color(0xFFE0E1DD),
    surfaceVariant = Color(0xFF415A77),
    onSurfaceVariant = Color(0xFF778DA9),
    outline = Color(0xFF415A77),
    error = Color(0xFFE63946)
)

// 6. SUNSET (Energético e Quente)
private val SunsetColorScheme = lightColorScheme(
    primary = Color(0xFFF4A261), // Orange
    onPrimary = Color.White,
    background = Color(0xFFFEF1E6),
    surface = Color.White,
    onSurface = Color(0xFF264653),
    surfaceVariant = Color(0xFFFADCC1),
    onSurfaceVariant = Color(0xFFE76F51),
    outline = Color(0xFFE9C46A),
    error = Color(0xFFE63946)
)

// 7. BERRY (Sofisticado e Vibrante)
private val BerryColorScheme = lightColorScheme(
    primary = Color(0xFF9D0208), // Wine
    onPrimary = Color.White,
    background = Color(0xFFFFF0F3),
    surface = Color.White,
    onSurface = Color(0xFF370617),
    surfaceVariant = Color(0xFFFFCCD5),
    onSurfaceVariant = Color(0xFF6A040F),
    outline = Color(0xFFDC2F02),
    error = Color(0xFFD00000)
)

// 8. MINIMAL (Clean e Focado)
private val MinimalColorScheme = lightColorScheme(
    primary = Color(0xFF000000),
    onPrimary = Color.White,
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFAFAFA),
    onSurface = Color(0xFF000000),
    surfaceVariant = Color(0xFFF0F0F0),
    onSurfaceVariant = Color(0xFF666666),
    outline = Color(0xFFE0E0E0),
    error = Color(0xFFFF3B30)
)

// 9. VINTAGE (Nostálgico e Aconchegante)
private val VintageColorScheme = lightColorScheme(
    primary = Color(0xFF6D597A), // Muted Purple
    onPrimary = Color.White,
    background = Color(0xFFEAE2B7), // Cream/Yellow
    surface = Color(0xFFFCF6BD),
    onSurface = Color(0xFF003049),
    surfaceVariant = Color(0xFFD62828),
    onSurfaceVariant = Color(0xFFF77F00),
    outline = Color(0xFFFCBF49),
    error = Color(0xFFD62828)
)

// 10. NEON (Digital e Ousado)
private val NeonColorScheme = darkColorScheme(
    primary = Color(0xFF39FF14), // Neon Green
    onPrimary = Color.Black,
    background = Color(0xFF000000),
    surface = Color(0xFF121212),
    onSurface = Color(0xFF39FF14),
    surfaceVariant = Color(0xFF1F1F1F),
    onSurfaceVariant = Color(0xFF00D1FF), // Cyan
    outline = Color(0xFFBC13FE), // Purple
    error = Color(0xFFFF003F)
)

@Composable
fun AppTheme(
    themeConfig: PageThemeConfig = PageThemeConfig.DEFAULT,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeConfig) {
        PageThemeConfig.DEFAULT -> RoyalColorScheme
        PageThemeConfig.OCEAN -> SoftOceanColorScheme
        PageThemeConfig.FOREST -> EcoForestColorScheme
        PageThemeConfig.CANDY -> RoseGoldColorScheme
        PageThemeConfig.DARK -> MidnightColorScheme
        PageThemeConfig.SUNSET -> SunsetColorScheme
        PageThemeConfig.BERRY -> BerryColorScheme
        PageThemeConfig.MINIMAL -> MinimalColorScheme
        PageThemeConfig.VINTAGE -> VintageColorScheme
        PageThemeConfig.NEON -> NeonColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
