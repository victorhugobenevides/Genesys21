package com.itbenevides.genesys21.ui.components.text

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Estilos de texto semânticos para a Presentation.
 */
enum class GenesysTextStyle {
    Headline,
    Title,
    Body,
    Label,
    Error
}

/**
 * Pesos de fonte semânticos para a Presentation.
 */
enum class GenesysFontWeight {
    Normal,
    Bold,
    ExtraBold
}

/**
 * Alinhamentos de texto semânticos para a Presentation.
 */
enum class GenesysTextAlign {
    Start,
    Center,
    End,
    Justify
}

/**
 * Componente de texto base do Design System.
 */
@Composable
fun GenesysText(
    text: String,
    style: GenesysTextStyle = GenesysTextStyle.Body,
    color: Color = Color.Unspecified,
    textAlign: GenesysTextAlign? = null,
    fontWeight: GenesysFontWeight? = null,
    fontSize: TextUnit = TextUnit.Unspecified, // ADICIONADO: Suporte a tamanho customizado
    modifier: Modifier = Modifier
) {
    GenesysTextContent(
        text = text,
        style = style,
        color = color,
        textAlign = textAlign,
        fontWeight = fontWeight,
        fontSize = fontSize,
        modifier = modifier
    )
}

/**
 * Implementação interna estável para evitar recursão infinita no compilador WasmJs.
 */
@Composable
internal fun GenesysTextContent(
    text: String,
    style: GenesysTextStyle = GenesysTextStyle.Body,
    color: Color = Color.Unspecified,
    textAlign: GenesysTextAlign? = null,
    fontWeight: GenesysFontWeight? = null,
    fontSize: TextUnit = TextUnit.Unspecified,
    modifier: Modifier = Modifier
) {
    val textStyle = when (style) {
        GenesysTextStyle.Headline -> MaterialTheme.typography.headlineLarge
        GenesysTextStyle.Title -> MaterialTheme.typography.titleLarge
        GenesysTextStyle.Body -> MaterialTheme.typography.bodyLarge
        GenesysTextStyle.Label -> MaterialTheme.typography.labelSmall
        GenesysTextStyle.Error -> MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error)
    }

    val composeFontWeight = when (fontWeight) {
        GenesysFontWeight.Normal -> FontWeight.Normal
        GenesysFontWeight.Bold -> FontWeight.Bold
        GenesysFontWeight.ExtraBold -> FontWeight.ExtraBold
        null -> textStyle.fontWeight
    }

    val composeTextAlign = when (textAlign) {
        GenesysTextAlign.Start -> TextAlign.Start
        GenesysTextAlign.Center -> TextAlign.Center
        GenesysTextAlign.End -> TextAlign.End
        GenesysTextAlign.Justify -> TextAlign.Justify
        null -> null
    }

    Text(
        text = text,
        style = textStyle,
        color = color,
        modifier = modifier,
        textAlign = composeTextAlign,
        fontWeight = composeFontWeight,
        fontSize = fontSize // APLICA O TAMANHO CUSTOMIZADO
    )
}

/**
 * Extensão para RowScope.
 */
@Composable
fun RowScope.GenesysText(
    text: String,
    style: GenesysTextStyle = GenesysTextStyle.Body,
    color: Color = Color.Unspecified,
    textAlign: GenesysTextAlign? = null,
    fontWeight: GenesysFontWeight? = null,
    fontSize: TextUnit = TextUnit.Unspecified,
    weightValue: Float = 0f,
    modifier: Modifier = Modifier
) {
    val finalModifier = if (weightValue > 0f) Modifier.weight(weightValue).then(modifier) else modifier
    GenesysTextContent(
        text = text,
        style = style,
        color = color,
        textAlign = textAlign,
        fontWeight = fontWeight,
        fontSize = fontSize,
        modifier = finalModifier
    )
}

/**
 * Extensão para ColumnScope.
 */
@Composable
fun ColumnScope.GenesysText(
    text: String,
    style: GenesysTextStyle = GenesysTextStyle.Body,
    color: Color = Color.Unspecified,
    textAlign: GenesysTextAlign? = null,
    fontWeight: GenesysFontWeight? = null,
    fontSize: TextUnit = TextUnit.Unspecified,
    weightValue: Float = 0f,
    modifier: Modifier = Modifier
) {
    val finalModifier = if (weightValue > 0f) Modifier.weight(weightValue).then(modifier) else modifier
    GenesysTextContent(
        text = text,
        style = style,
        color = color,
        textAlign = textAlign,
        fontWeight = fontWeight,
        fontSize = fontSize,
        modifier = finalModifier
    )
}
