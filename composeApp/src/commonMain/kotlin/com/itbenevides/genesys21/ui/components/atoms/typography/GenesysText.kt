package com.itbenevides.genesys21.ui.components.atoms.typography

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.itbenevides.genesys21.ui.theme.*

/**
 * Componente de texto base do Design System.
 * Totalmente integrado aos Design Tokens semânticos.
 */
@Composable
fun GenesysText(
    text: String,
    style: GenesysTextStyle = GenesysTextStyle.Body,
    color: Color = Color.Unspecified,
    textAlign: GenesysTextAlign? = null,
    fontWeight: GenesysFontWeight? = null,
    fontSize: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    isSelectable: Boolean = false,
    modifier: Modifier = Modifier,
) {
    GenesysTextContent(
        text = text,
        style = style,
        color = color,
        textAlign = textAlign,
        fontWeight = fontWeight,
        fontSize = fontSize,
        maxLines = maxLines,
        overflow = overflow,
        isSelectable = isSelectable,
        modifier = modifier,
    )
}

@Composable
internal fun GenesysTextContent(
    text: String,
    style: GenesysTextStyle = GenesysTextStyle.Body,
    color: Color = Color.Unspecified,
    textAlign: GenesysTextAlign? = null,
    fontWeight: GenesysFontWeight? = null,
    fontSize: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    isSelectable: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val tokens = GenesysTheme.typography
    val textStyle = when (style) {
        GenesysTextStyle.Headline -> tokens.headline
        GenesysTextStyle.Title -> tokens.title
        GenesysTextStyle.Body -> tokens.body
        GenesysTextStyle.Label -> tokens.label
        GenesysTextStyle.Error -> tokens.bodySmall.copy(color = GenesysTheme.colors.error)
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

    val finalColor = if (color == Color.Unspecified) {
        when (style) {
            GenesysTextStyle.Label -> GenesysTheme.colors.onSurfaceVariant
            GenesysTextStyle.Error -> GenesysTheme.colors.error
            else -> GenesysTheme.colors.onSurface
        }
    } else color

    val content = @Composable {
        Text(
            text = text,
            style = textStyle,
            color = finalColor,
            modifier = modifier,
            textAlign = composeTextAlign,
            fontWeight = composeFontWeight,
            fontSize = fontSize,
            maxLines = maxLines,
            overflow = overflow,
        )
    }

    if (isSelectable) {
        SelectionContainer { content() }
    } else {
        content()
    }
}

@Composable
fun RowScope.GenesysRowText(
    text: String,
    style: GenesysTextStyle = GenesysTextStyle.Body,
    color: Color = Color.Unspecified,
    textAlign: GenesysTextAlign? = null,
    fontWeight: GenesysFontWeight? = null,
    fontSize: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    isSelectable: Boolean = false,
    weightValue: Float = 0f,
    modifier: Modifier = Modifier,
) {
    val finalModifier = if (weightValue > 0f) Modifier.weight(weightValue).then(modifier) else modifier
    GenesysTextContent(
        text = text,
        style = style,
        color = color,
        textAlign = textAlign,
        fontWeight = fontWeight,
        fontSize = fontSize,
        maxLines = maxLines,
        overflow = overflow,
        isSelectable = isSelectable,
        modifier = finalModifier,
    )
}

@Composable
fun ColumnScope.GenesysColumnText(
    text: String,
    style: GenesysTextStyle = GenesysTextStyle.Body,
    color: Color = Color.Unspecified,
    textAlign: GenesysTextAlign? = null,
    fontWeight: GenesysFontWeight? = null,
    fontSize: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    isSelectable: Boolean = false,
    weightValue: Float = 0f,
    modifier: Modifier = Modifier,
) {
    val finalModifier = if (weightValue > 0f) Modifier.weight(weightValue).then(modifier) else modifier
    GenesysTextContent(
        text = text,
        style = style,
        color = color,
        textAlign = textAlign,
        fontWeight = fontWeight,
        fontSize = fontSize,
        maxLines = maxLines,
        overflow = overflow,
        isSelectable = isSelectable,
        modifier = finalModifier,
    )
}
