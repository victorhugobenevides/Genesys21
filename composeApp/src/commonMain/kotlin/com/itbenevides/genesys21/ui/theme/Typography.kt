package com.itbenevides.genesys21.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.itbenevides.genesys21.domain.model.TypographySet
import com.itbenevides.genesys21.ui.util.GenesysWindowSizeClass

/**
 * Material Design 3 Typography Scale.
 * Provides a full range of 15 styles for consistent information hierarchy.
 */
fun getTypography(set: TypographySet = TypographySet.DEFAULT): Typography {
    val fontFamily =
        when (set) {
            TypographySet.DEFAULT, TypographySet.MODERN_SANS -> FontFamily.SansSerif
            TypographySet.CLASSIC_SERIF -> FontFamily.Serif
            TypographySet.MINIMAL_MONO -> FontFamily.Monospace
            TypographySet.PLAYFUL_ROUNDED -> FontFamily.SansSerif
        }

    return Typography(
        // Display: Large titles for hero sections
        displayLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 57.sp,
            lineHeight = 64.sp,
            letterSpacing = (-0.25).sp
        ),
        displayMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 45.sp,
            lineHeight = 52.sp,
            letterSpacing = 0.sp
        ),
        displaySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 36.sp,
            lineHeight = 44.sp,
            letterSpacing = 0.sp
        ),

        // Headline: Prominent headers
        headlineLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = 0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = 0.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.sp
        ),

        // Title: Subsection headers
        titleLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        ),
        titleMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.15.sp
        ),
        titleSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),

        // Body: Main content text
        bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp
        ),
        bodySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp
        ),

        // Label: Small metadata and button text
        labelLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        ),
        labelSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        )
    )
}

fun getGenesysTypography(
    set: TypographySet = TypographySet.DEFAULT,
    windowSizeClass: GenesysWindowSizeClass = GenesysWindowSizeClass.COMPACT
): GenesysTypography {
    val m3 = getTypography(set)

    // Escalonamento Dinâmico: Aumentamos fontes no Desktop para impacto visual
    val scaleFactor = when(windowSizeClass) {
        GenesysWindowSizeClass.COMPACT -> 0.sp
        GenesysWindowSizeClass.MEDIUM -> 1.sp
        GenesysWindowSizeClass.EXPANDED -> 2.sp
    }

    return GenesysTypography(
        display = m3.headlineLarge.copy(fontSize = (m3.headlineLarge.fontSize.value + scaleFactor.value + 2).sp),
        headline = m3.headlineSmall.copy(fontSize = (m3.headlineSmall.fontSize.value + scaleFactor.value).sp),
        title = m3.titleLarge,
        body = m3.bodyLarge,
        bodySmall = m3.bodySmall,
        label = m3.labelSmall,
        action = m3.labelLarge
    )
}

val AppTypography = getTypography(TypographySet.DEFAULT)
