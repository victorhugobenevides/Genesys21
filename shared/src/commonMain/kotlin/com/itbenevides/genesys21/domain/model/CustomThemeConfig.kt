package com.itbenevides.genesys21.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CustomThemeConfig(
    /** Hex string */
    val primaryColor: String? = null,
    val onPrimaryColor: String? = null,
    val secondaryColor: String? = null,
    val backgroundColor: String? = null,
    val surfaceColor: String? = null,
    val onSurfaceColor: String? = null,
    /** Default radius in dp */
    val cornerRadius: Int = 16,
    /** Transparency for glassmorphism */
    val glassIntensity: Float = 0.1f,
    val typographySet: TypographySet = TypographySet.DEFAULT,
)
