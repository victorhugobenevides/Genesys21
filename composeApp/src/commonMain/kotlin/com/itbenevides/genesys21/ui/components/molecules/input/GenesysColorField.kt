package com.itbenevides.genesys21.ui.components.molecules.input

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.theme.GenesysTheme
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
                    modifier =
                        Modifier
                            .padding(end = GenesysTheme.spacing.xs)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(value.toColor())
                            .border(
                                width = GenesysTheme.spacing.xxxs,
                                color = GenesysTheme.colors.outline.copy(alpha = 0.5f),
                                shape = CircleShape,
                            )
                            .clickable { showPalette = !showPalette },
                )
            },
        )

        if (showPalette) {
            Surface(
                modifier =
                    Modifier
                        .padding(top = GenesysTheme.spacing.xs)
                        .fillMaxWidth(),
                shape = RoundedCornerShape(GenesysTheme.config.cornerRadius),
                color = GenesysTheme.colors.surfaceVariant.copy(alpha = 0.3f),
                border =
                    androidx.compose.foundation.BorderStroke(
                        width = GenesysTheme.spacing.xxxs,
                        color = GenesysTheme.colors.outline,
                    ),
            ) {
                Column(modifier = Modifier.padding(GenesysTheme.spacing.m)) {
                    Text(
                        text = "Paleta de Sugestões",
                        style = GenesysTheme.typography.label,
                        color = GenesysTheme.colors.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(GenesysTheme.spacing.s))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(GenesysTheme.spacing.s),
                        verticalArrangement = Arrangement.spacedBy(GenesysTheme.spacing.s),
                    ) {
                        GenesysBrandPresets.forEach { preset ->
                            Box(
                                modifier =
                                    Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(preset.toColor())
                                        .border(
                                            width = if (value.equals(preset, ignoreCase = true)) 3.dp else GenesysTheme.spacing.xxxs,
                                            color =
                                                if (value.equals(preset, ignoreCase = true)) {
                                                    GenesysTheme.colors.brand
                                                } else {
                                                    Color.White.copy(alpha = 0.5f)
                                                },
                                            shape = CircleShape,
                                        )
                                        .clickable {
                                            onValueChange(preset)
                                            showPalette = false
                                        },
                            )
                        }
                    }
                }
            }
        }
    }
}
