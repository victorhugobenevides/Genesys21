package com.itbenevides.genesys21.util

import androidx.compose.ui.graphics.Color

/**
 * Converte uma string Hex (ex: #FFFFFF ou #FF000000) para um objeto Color.
 */
fun String?.toColor(fallback: Color = Color.Black): Color {
    if (this == null || this.isBlank()) return fallback
    return try {
        val hex = this.trim().removePrefix("#")
        val longColor = hex.toLong(16)
        if (hex.length <= 6) {
            Color(longColor or 0xFF000000)
        } else {
            Color(longColor)
        }
    } catch (e: Exception) {
        fallback
    }
}

/**
 * Converte um objeto Color para uma string Hex formatada (#RRGGBB).
 */
fun Color.toHex(): String {
    val r = (red * 255).toInt().toString(16).padStart(2, '0')
    val g = (green * 255).toInt().toString(16).padStart(2, '0')
    val b = (blue * 255).toInt().toString(16).padStart(2, '0')
    return "#${r}${g}$b".uppercase()
}

/**
 * Lista de cores predefinidas para sugestão rápida.
 */
val GenesysBrandPresets =
    listOf(
        // Royal Navy
        "#14213D",
        // Gold
        "#FCA311",
        // Ocean Teal
        "#00ADB5",
        // Forest Green
        "#283618",
        // Candy Pink
        "#D81159",
        // Sunset Orange
        "#E76F51",
        // Berry Purple
        "#6A0572",
        // Black
        "#000000",
        // White
        "#FFFFFF",
        // Radarani Blue
        "#2CB1FF",
        // Pro Orange
        "#FF5722",
        // Success Green
        "#4CAF50",
        // Error Red
        "#F44336",
        // Purple
        "#9C27B0",
        // Indigo
        "#3F51B5",
    )
