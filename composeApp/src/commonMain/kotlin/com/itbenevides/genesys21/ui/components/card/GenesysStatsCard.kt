package com.itbenevides.genesys21.ui.components.card

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.itbenevides.genesys21.ui.components.text.GenesysFontWeight
import com.itbenevides.genesys21.ui.components.text.GenesysText
import com.itbenevides.genesys21.ui.components.text.GenesysTextStyle

/**
 * Card de estatísticas do Design System.
 * Focado puramente em conteúdo semântico.
 */
@Composable
fun GenesysStatsCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    GenesysCard(
        modifier = modifier,
        backgroundColor = color.copy(alpha = 0.1f),
    ) {
        GenesysText(
            text = label,
            style = GenesysTextStyle.Label,
            color = color,
            fontWeight = GenesysFontWeight.Bold,
        )
        GenesysText(
            text = value,
            style = GenesysTextStyle.Title,
            fontWeight = GenesysFontWeight.ExtraBold,
            color = color,
        )
    }
}
