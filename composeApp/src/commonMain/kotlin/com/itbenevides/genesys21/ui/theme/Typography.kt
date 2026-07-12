package com.itbenevides.genesys21.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.itbenevides.genesys21.domain.model.TypographySet

fun getTypography(set: TypographySet = TypographySet.DEFAULT): Typography {
    val fontFamily =
        when (set) {
            TypographySet.DEFAULT, TypographySet.MODERN_SANS -> FontFamily.SansSerif
            TypographySet.CLASSIC_SERIF -> FontFamily.Serif
            TypographySet.MINIMAL_MONO -> FontFamily.Monospace
            TypographySet.PLAYFUL_ROUNDED -> FontFamily.SansSerif
        }

    val baseLetterSpacing =
        when (set) {
            TypographySet.MODERN_SANS -> -0.02
            TypographySet.CLASSIC_SERIF -> 0.0
            TypographySet.MINIMAL_MONO -> 0.02
            else -> 0.0
        }

    return Typography(
        headlineLarge =
            TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 34.sp,
                lineHeight = 41.sp,
                letterSpacing = (0.37 + baseLetterSpacing).sp,
                fontFamily = fontFamily,
            ),
        headlineMedium =
            TextStyle(
                fontWeight = FontWeight.SemiBold,
                fontSize = 28.sp,
                lineHeight = 34.sp,
                letterSpacing = (0.36 + baseLetterSpacing).sp,
                fontFamily = fontFamily,
            ),
        titleLarge =
            TextStyle(
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                letterSpacing = (0.35 + baseLetterSpacing).sp,
                fontFamily = fontFamily,
            ),
        titleMedium =
            TextStyle(
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                lineHeight = 22.sp,
                letterSpacing = (-0.41 + baseLetterSpacing).sp,
                fontFamily = fontFamily,
            ),
        bodyLarge =
            TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 17.sp,
                lineHeight = 24.sp,
                letterSpacing = (-0.41 + baseLetterSpacing).sp,
                fontFamily = fontFamily,
            ),
        bodyMedium =
            TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                lineHeight = 20.sp,
                letterSpacing = (-0.24 + baseLetterSpacing).sp,
                fontFamily = fontFamily,
            ),
        labelSmall =
            TextStyle(
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 13.sp,
                letterSpacing = (0.06 + baseLetterSpacing).sp,
                fontFamily = fontFamily,
            ),
    )
}

// iOS-like Typography (Approximating San Francisco)
val AppTypography = getTypography(TypographySet.DEFAULT)
