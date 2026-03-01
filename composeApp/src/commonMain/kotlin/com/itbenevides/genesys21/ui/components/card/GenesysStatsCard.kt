package com.itbenevides.genesys21.ui.components.card

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.ui.components.text.GenesysText
import com.itbenevides.genesys21.ui.components.text.GenesysTextStyle
import com.itbenevides.genesys21.ui.components.text.GenesysFontWeight
import com.itbenevides.genesys21.ui.components.layout.GenesysSpacer
import com.itbenevides.genesys21.ui.components.layout.GenesysSpacing

/**
 * Card de estatísticas do Design System seguindo Material 3.
 * Utiliza Tonal Elevation para profundidade e hierarquia tipográfica clara.
 * 
 * @param label Texto descritivo do valor (ex: "Total de Vendas")
 * @param value Valor a ser destacado (ex: "R$ 1.234,56")
 * @param color Cor de destaque para o valor
 * @param modifier Modifier adicional
 * @param contentPadding Padding interno do card (padrão: 16.dp)
 */
@Composable
fun GenesysStatsCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
    contentPadding: androidx.compose.ui.unit.Dp = 16.dp
) {
    // Usamos GenesysCard mas customizamos para o estilo de Dashboard
    GenesysCard(
        modifier = modifier,
        backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        elevation = 2.dp, // Tonal elevation leve do M3
        contentPadding = contentPadding
    ) {
        Column {
            GenesysText(
                text = label.uppercase(), 
                style = GenesysTextStyle.Label, 
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = GenesysFontWeight.ExtraBold
            )
            
            GenesysSpacer(GenesysSpacing.Small)
            
            GenesysText(
                text = value, 
                style = GenesysTextStyle.Headline, // Aumentado para dar destaque ao dado
                fontWeight = GenesysFontWeight.ExtraBold, 
                color = color // Mantemos a cor apenas no valor para foco visual
            )
        }
    }
}
