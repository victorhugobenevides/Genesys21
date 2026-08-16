package com.itbenevides.genesys21.util

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import com.itbenevides.genesys21.domain.model.PageThemeConfig
import kotlinx.serialization.json.*

object StripeThemeMapper {

    fun mapToAppearance(theme: PageThemeConfig, colorScheme: ColorScheme): String {
        val variables = buildJsonObject {
            put("colorPrimary", colorToHex(colorScheme.primary))
            put("colorBackground", colorToHex(colorScheme.surface))
            put("colorText", colorToHex(colorScheme.onSurface))
            put("colorDanger", colorToHex(colorScheme.error))
            put("borderRadius", "12px")
        }

        return buildJsonObject {
            put("theme", "flat")
            put("variables", variables)
            putJsonObject("rules") {
                putJsonObject(".Input") {
                    put("border", "1px solid ${colorToHex(colorScheme.outline.copy(alpha = 0.2f))}")
                    put("boxShadow", "none")
                }
            }
        }.toString()
    }

    private fun colorToHex(color: Color): String {
        val r = (color.red * 255).toInt()
        val g = (color.green * 255).toInt()
        val b = (color.blue * 255).toInt()
        return "#${r.toString(16).padStart(2, '0')}${g.toString(16).padStart(2, '0')}${b.toString(16).padStart(2, '0')}".uppercase()
    }
}
