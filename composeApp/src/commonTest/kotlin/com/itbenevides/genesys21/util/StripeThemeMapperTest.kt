package com.itbenevides.genesys21.util

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.itbenevides.genesys21.domain.model.PageThemeConfig
import kotlinx.serialization.json.*
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class StripeThemeMapperTest {

    @Test
    fun testMapToAppearance_ColorsAreCorrect() {
        val colorScheme = lightColorScheme(
            primary = Color(0xFF123456),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF000000),
            error = Color(0xFFFF0000)
        )

        val jsonString = StripeThemeMapper.mapToAppearance(PageThemeConfig.ELEGANCE, colorScheme)
        val json = Json.parseToJsonElement(jsonString).jsonObject

        val variables = json["variables"]?.jsonObject ?: error("Variables not found")

        assertEquals("#123456", variables["colorPrimary"]?.jsonPrimitive?.content)
        assertEquals("#FFFFFF", variables["colorBackground"]?.jsonPrimitive?.content)
        assertEquals("#000000", variables["colorText"]?.jsonPrimitive?.content)
        assertEquals("#FF0000", variables["colorDanger"]?.jsonPrimitive?.content)
    }

    @Test
    fun testMapToAppearance_RulesArePresent() {
        val colorScheme = lightColorScheme()
        val jsonString = StripeThemeMapper.mapToAppearance(PageThemeConfig.VIBRANT, colorScheme)
        val json = Json.parseToJsonElement(jsonString).jsonObject

        assertTrue(json.containsKey("rules"))
        val rules = json["rules"]?.jsonObject ?: error("Rules not found")
        assertTrue(rules.containsKey(".Input"))
    }
}
