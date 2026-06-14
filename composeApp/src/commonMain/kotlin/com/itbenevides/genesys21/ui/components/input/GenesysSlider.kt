package com.itbenevides.genesys21.ui.components.input

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.itbenevides.genesys21.ui.components.text.GenesysText
import com.itbenevides.genesys21.ui.components.text.GenesysTextStyle

/**
 * Componente de Slider (Scroll Horizontal) padronizado para o DS Genesys21.
 */
@Composable
fun GenesysSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    label: String,
    valueRange: ClosedFloatingPointRange<Float> = 50f..500f, // CORREÇÃO: Tipo ajustado para ClosedFloatingPointRange
    modifier: Modifier = Modifier,
    steps: Int = 0,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        GenesysText(
            text = "$label: ${value.toInt()}px",
            style = GenesysTextStyle.Label,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.fillMaxWidth(),
            colors =
                SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
        )
    }
}
