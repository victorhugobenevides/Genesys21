package com.itbenevides.genesys21.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle

/**
 * Estilos de texto semânticos para a Presentation.
 */
enum class GenesysTextStyle {
    Headline,
    Title,
    Body,
    Label,
    Error,
}

/**
 * Pesos de fonte semânticos para a Presentation.
 */
enum class GenesysFontWeight {
    Normal,
    Bold,
    ExtraBold,
}

/**
 * Alinhamentos de texto semânticos para a Presentation.
 */
enum class GenesysTextAlign {
    Start,
    Center,
    End,
    Justify,
}

/**
 * Design Tokens: Escala Tipográfica do Genesys21.
 * Define a hierarquia de informação do sistema.
 */
@Immutable
data class GenesysTypography(
    val display: TextStyle,
    val headline: TextStyle,
    val title: TextStyle,
    val body: TextStyle,
    val bodySmall: TextStyle,
    val label: TextStyle,
    val action: TextStyle
)

val LocalGenesysTypography = staticCompositionLocalOf<GenesysTypography> {
    error("No GenesysTypography provided")
}
