package com.itbenevides.genesys21.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CustomThemeConfig(
    val primaryColor: String? = null, // Hex string
    val onPrimaryColor: String? = null,
    val secondaryColor: String? = null,
    val backgroundColor: String? = null,
    val surfaceColor: String? = null,
    val onSurfaceColor: String? = null,
    val cornerRadius: Int = 16, // Default radius in dp
    val glassIntensity: Float = 0.1f, // Transparency for glassmorphism
    val typographySet: TypographySet = TypographySet.DEFAULT,
)
