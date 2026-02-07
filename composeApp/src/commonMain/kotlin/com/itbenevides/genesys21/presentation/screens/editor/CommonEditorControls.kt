package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.ui.components.button.GenesysIconButton
import com.itbenevides.genesys21.ui.components.input.GenesysSlider
import com.itbenevides.genesys21.ui.components.layout.*
import com.itbenevides.genesys21.ui.components.text.*
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.GenesysStrings

@Composable
fun TextFormatControls(
    fontSize: Float,
    onFontSizeChange: (Float) -> Unit,
    textAlign: String,
    onTextAlignChange: (String) -> Unit,
    fontWeight: String,
    onFontWeightChange: (String) -> Unit,
    isUppercase: Boolean,
    onUppercaseChange: (Boolean) -> Unit,
    fontSizeRange: ClosedFloatingPointRange<Float> = 12f..60f
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    GenesysColumn(usePadding = false) {
        GenesysRow(modifier = Modifier.fillMaxWidth()) {
            GenesysWeightBox(1f) {
                GenesysColumn(usePadding = false) {
                    GenesysText(text = GenesysStrings.Alignment, style = GenesysTextStyle.Label)
                    Row {
                        listOf("LEFT", "CENTER", "RIGHT").forEach { align ->
                            val isSelected = textAlign == align
                            GenesysIconButton(
                                icon = when(align) {
                                    "LEFT" -> GenesysIcons.AlignLeft
                                    "RIGHT" -> GenesysIcons.AlignRight
                                    else -> GenesysIcons.AlignCenter
                                },
                                onClick = { onTextAlignChange(align) },
                                tint = if (isSelected) primaryColor else Color.Unspecified
                            )
                        }
                    }
                }
            }
            
            GenesysWeightBox(1f) {
                GenesysColumn(usePadding = false) {
                    GenesysText(text = "Estilo", style = GenesysTextStyle.Label)
                    Row {
                        GenesysIconButton(
                            icon = GenesysIcons.Bold,
                            onClick = { onFontWeightChange(if (fontWeight == "BOLD") "NORMAL" else "BOLD") },
                            tint = if (fontWeight == "BOLD") primaryColor else Color.Unspecified
                        )
                        GenesysSpacer(GenesysSpacing.Small)
                        Switch(checked = isUppercase, onCheckedChange = onUppercaseChange)
                    }
                }
            }
        }

        GenesysSpacer(GenesysSpacing.Medium)
        GenesysSlider(
            value = fontSize, 
            onValueChange = onFontSizeChange, 
            label = GenesysStrings.FontSize, 
            valueRange = fontSizeRange
        )
    }
}
