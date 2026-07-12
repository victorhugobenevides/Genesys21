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
    return "#${r}${g}${b}".uppercase()
}

/**
 * Lista de cores predefinidas para sugestão rápida.
 */
val GenesysBrandPresets = listOf(
    "#14213D", // Royal Navy
    "#FCA311", // Gold
    "#00ADB5", // Ocean Teal
    "#283618", // Forest Green
    "#D81159", // Candy Pink
    "#E76F51", // Sunset Orange
    "#6A0572", // Berry Purple
    "#000000", // Black
    "#FFFFFF", // White
    "#2CB1FF", // Radarani Blue
    "#FF5722", // Pro Orange
    "#4CAF50", // Success Green
    "#F44336", // Error Red
    "#9C27B0", // Purple
    "#3F51B5"  // Indigo
)
