package com.itbenevides.genesys21.ui.components.molecules.input

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.util.GenesysBrandPresets
import com.itbenevides.genesys21.util.toColor

/**
 * Campo de entrada de cor que combina um TextField com uma paleta de sugestões.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GenesysColorField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    var showPalette by remember { mutableStateOf(value = false) }

    Column(modifier = modifier.fillMaxWidth()) {
        GenesysTextField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            placeholder = "#000000",
            icon = GenesysIcons.Palette,
            trailingIcon = {
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(value.toColor())
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            shape = CircleShape,
                        )
                        .clickable { showPalette = !showPalette }
                )
            }
        )

        if (showPalette) {
            Surface(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Paleta de Sugestões",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GenesysBrandPresets.forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(preset.toColor())
                                    .border(
                                        width = if (value.equals(preset, ignoreCase = true)) 3.dp else 1.dp,
                                        color = if (value.equals(preset, ignoreCase = true))
                                            MaterialTheme.colorScheme.primary
                                        else
                                            Color.White.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        onValueChange(preset)
                                        showPalette = false
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}
