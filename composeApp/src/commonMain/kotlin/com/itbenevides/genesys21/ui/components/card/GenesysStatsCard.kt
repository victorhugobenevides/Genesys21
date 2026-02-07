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
 */
@Composable
fun GenesysStatsCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    // Usamos GenesysCard mas customizamos para o estilo de Dashboard
    GenesysCard(
        modifier = modifier,
        backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        elevation = 2.dp // Tonal elevation leve do M3
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
